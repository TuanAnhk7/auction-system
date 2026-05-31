package auction.server.network;

import auction.common.exception.AuctionException;
import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionManager;
import auction.common.model.auction.BidTransaction;
import auction.common.model.item.Antique;
import auction.common.model.item.Art;
import auction.common.model.item.Electronics;
import auction.common.model.item.Item;
import auction.common.model.network.AuctionExtendedResponse;
import auction.common.model.network.AuctionView;
import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListResponse;
import auction.common.support.SampleDataFactory;
import auction.server.db.PersistenceManager;
import auction.server.db.entity.AuctionEntity;
import auction.server.db.entity.BidTransactionEntity;
import auction.server.db.entity.ItemEntity;
import auction.server.db.repository.AuctionRepositoryImpl;
import auction.server.db.repository.BidTransactionRepository;
import auction.server.db.repository.ItemRepository;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {
    private static final int PORT = 8080; // Cổng kết nối mà Server sẽ lắng nghe
    private static final DateTimeFormatter DB_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ExecutorService clientPool = Executors.newFixedThreadPool(8);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final AuctionManager auctionManager = AuctionManager.getInstance(auction.server.auth.UserManager.getInstance());

    public static void main(String[] args) {
        new AuctionServer().start();
    }

    public void start() {
        bootstrapSampleAuctions();
        startAuctionExpiryMonitor();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("====== HỆ THỐNG ĐẤU GIÁ (SERVER) ======");
            System.out.println("Server đang chạy và lắng nghe trên port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(">> Có Client mới kết nối từ địa chỉ: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this, auctionManager);
                clients.add(handler);
                clientPool.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động Server: " + e.getMessage());
        } finally {
            scheduler.shutdownNow();
            clientPool.shutdownNow();
        }
    }

    public void broadcast(BidResponse response) {
        for (ClientHandler client : clients) {
            client.send(response);
        }
    }

    public void broadcast(AuctionExtendedResponse response) {
        for (ClientHandler client : clients) {
            client.send(response);
        }
    }

    public void broadcastAuctionList(GetAuctionListResponse response) {
        for (ClientHandler client : clients) {
            client.sendAuctionList(response);
        }
    }

    public void unregister(ClientHandler handler) {
        clients.remove(handler);
    }

    private void startAuctionExpiryMonitor() {
        // Background task chạy định kỳ để server tự đóng phiên khi hết giờ.
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<Auction> changedAuctions = auctionManager.closeExpiredAuctions();
                if (!changedAuctions.isEmpty()) {
                    changedAuctions.forEach(this::persistAuctionSnapshot);
                    changedAuctions.forEach(this::persistAuctionBidHistory);
                    broadcastAuctionList(buildAuctionListResponse());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tự động đóng/bắt đầu phiên đấu giá: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void bootstrapSampleAuctions() {
        if (!auctionManager.getActiveAuctions().isEmpty()) {
            return;
        }

        List<Art> sampleArts = SampleDataFactory.createSampleArts();
        List<Auction> sampleAuctions = new ArrayList<>();

        sampleArts.forEach(item -> {
            String auctionId = buildSampleAuctionId(item.getId());
            Auction auction = new Auction(auctionId, item, LocalDateTime.now().plusHours(8));
            try {
                auction.startAuction();
            } catch (AuctionException e) {
                throw new IllegalStateException("Khong khoi tao duoc phien dau gia mau.", e);
            }
            sampleAuctions.add(auction);
        });

        persistSampleData(sampleArts, sampleAuctions);
        sampleAuctions.forEach(auctionManager::addAuction);
    }

    private void persistSampleData(List<Art> sampleArts, List<Auction> sampleAuctions) {
        EntityManager em = PersistenceManager.getInstance().createEntityManager();
        try {
            em.getTransaction().begin();
            deleteExistingSampleData(em, sampleArts, sampleAuctions);
            for (Art art : sampleArts) {
                em.persist(toItemEntity(art));
            }
            for (Auction auction : sampleAuctions) {
                em.persist(toAuctionEntity(auction));
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException("Không thể ghi dữ liệu mẫu vào DB.", e);
        } finally {
            em.close();
        }
    }

    private void deleteExistingSampleData(EntityManager em, List<Art> sampleArts, List<Auction> sampleAuctions) {
        List<String> auctionIds = sampleAuctions.stream().map(Auction::getId).toList();
        List<String> itemIds = sampleArts.stream().map(Art::getId).toList();

        em.createQuery("DELETE FROM BidTransactionEntity b WHERE b.auctionId IN :auctionIds")
                .setParameter("auctionIds", auctionIds)
                .executeUpdate();
        em.createQuery("DELETE FROM AuctionEntity a WHERE a.id IN :auctionIds")
                .setParameter("auctionIds", auctionIds)
                .executeUpdate();
        em.createQuery("DELETE FROM ItemEntity i WHERE i.id IN :itemIds")
                .setParameter("itemIds", itemIds)
                .executeUpdate();
    }

    private ItemEntity toItemEntity(Art art) {
        return new ItemEntity(
                art.getId(),
                art.getName(),
                art.getDescription(),
                art.getCategory(),
                art.getStartingPrice(),
                art.getCurrentPrice(),
                art.getSellerId(),
                art.getDisplayCreator(),
                art.getClass().getSimpleName(),
                art.getArtist(),
                (double) art.getYearCreated(),
                null
        );
    }

    private ItemEntity toItemEntity(Item item) {
        if (item instanceof Art art) {
            return toItemEntity(art);
        }
        if (item instanceof Antique antique) {
            return new ItemEntity(
                    antique.getId(),
                    antique.getName(),
                    antique.getDescription(),
                    antique.getCategory(),
                    antique.getStartingPrice(),
                    antique.getCurrentPrice(),
                    antique.getSellerUsername(),
                    antique.getDisplayCreator(),
                    antique.getClass().getSimpleName(),
                    antique.getOrigin(),
                    (double) antique.getEstimatedAge(),
                    null
            );
        }
        if (item instanceof Electronics electronics) {
            return new ItemEntity(
                    electronics.getId(),
                    electronics.getName(),
                    electronics.getDescription(),
                    electronics.getCategory(),
                    electronics.getStartingPrice(),
                    electronics.getCurrentPrice(),
                    electronics.getSellerUsername(),
                    electronics.getDisplayCreator(),
                    electronics.getClass().getSimpleName(),
                    electronics.getBrand(),
                    (double) electronics.getWarrantyMonths(),
                    null
            );
        }
        return new ItemEntity(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getCurrentPrice(),
                item.getSellerUsername(),
                item.getDisplayCreator(),
                item.getClass().getSimpleName(),
                item.getDisplayCreator(),
                null,
                null
        );
    }

    private AuctionEntity toAuctionEntity(Auction auction) {
        return new AuctionEntity(
                auction.getId(),
                auction.getItem().getId(),
                auction.getSellerUsername(),
                auction.getStartTime().toString(),
                auction.getEndTime().toString(),
                auction.getCurrentHighestBid(),
                auction.getHighestBidder() == null ? null : auction.getHighestBidder().getUsername(),
                auction.getStatus().name(),
                null,
                null
        );
    }

    private String buildSampleAuctionId(String itemId) {
        return "auction-" + itemId;
    }

    public synchronized void persistAuctionBidHistory(Auction auction) {
        if (auction == null || auction.getBidHistory().isEmpty()) {
            return;
        }

        try {
            BidTransactionRepository bidTransactionRepository = PersistenceManager.getInstance().getBidTransactionRepository();
            for (BidTransaction transaction : auction.getBidHistory()) {
                bidTransactionRepository.save(toBidTransactionEntity(auction.getId(), transaction));
            }
        } catch (Exception e) {
            System.err.println("Không thể lưu lịch sử đấu giá cho phiên " + auction.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void persistAuctionSnapshot(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            return;
        }

        try {
            ItemRepository itemRepository = PersistenceManager.getInstance().getItemRepository();
            AuctionRepositoryImpl auctionRepository = PersistenceManager.getInstance().getAuctionRepository();
            itemRepository.save(toItemEntity(auction.getItem()));
            auctionRepository.save(toAuctionEntity(auction));
        } catch (Exception e) {
            System.err.println("Không thể lưu snapshot đấu giá cho phiên " + auction.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void deleteAuctionSnapshot(Auction auction) {
        if (auction == null || auction.getItem() == null) {
            return;
        }

        try {
            BidTransactionRepository bidTransactionRepository = PersistenceManager.getInstance().getBidTransactionRepository();
            ItemRepository itemRepository = PersistenceManager.getInstance().getItemRepository();
            AuctionRepositoryImpl auctionRepository = PersistenceManager.getInstance().getAuctionRepository();
            bidTransactionRepository.deleteByAuctionId(auction.getId());
            auctionRepository.delete(auction.getId());
            itemRepository.delete(auction.getItem().getId());
        } catch (Exception e) {
            System.err.println("Không thể xóa snapshot đấu giá cho phiên " + auction.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private GetAuctionListResponse buildAuctionListResponse() {
        return new GetAuctionListResponse(
                auctionManager.getActiveAuctions().stream()
                        .map(this::toAuctionView)
                        .toList()
        );
    }

    private AuctionView toAuctionView(Auction auction) {
        Item item = auction.getItem();
        String creatorName = item.getDisplayCreator();
        if (creatorName == null || creatorName.isBlank()) {
            creatorName = item.getSellerUsername();
        }
        if (creatorName == null || creatorName.isBlank()) {
            creatorName = "Không rõ";
        }

        // 1. Chuyển đổi lịch sử đặt giá cũ từ stream sang ArrayList để có thể add thêm phần tử
        java.util.List<String> historyList = new java.util.ArrayList<>(
                auction.getBidHistory().stream()
                        .map(this::formatBidHistory)
                        .toList()
        );

        // 2. Kiểm tra trạng thái phiên để tự động công bố kết quả vào Live Stream
        String statusStr = auction.getStatus().name();
        if ("FINISHED".equalsIgnoreCase(statusStr)) {
            if (auction.getHighestBidder() != null && auction.getHighestBidder().getUsername() != null) {
                historyList.add(String.format(
                        "🏆 [HỆ THỐNG] Phiên đấu giá kết thúc! Người chiến thắng: %s với mức giá %.2f USD",
                        auction.getHighestBidder().getUsername(),
                        auction.getCurrentHighestBid()
                ));
            } else {
                historyList.add("❌ [HỆ THỐNG] Phiên đấu giá kết thúc mà không có người tham gia đặt giá.");
            }
        } else if ("CANCELED".equalsIgnoreCase(statusStr)) {
            historyList.add("🚫 [HỆ THỐNG] Phiên đấu giá này đã bị hủy bỏ bởi Ban quản trị.");
        }

        // 3. Trả về đối tượng AuctionView chứa lịch sử mới đã được chèn thông báo hệ thống
        return new AuctionView(
                auction.getId(),
                item.getId(),
                item.getName(),
                item.getDescription(),
                creatorName,
                item.getSellerUsername(),
                item.getCategory(),
                item.getStartingPrice(),
                item.getCurrentPrice(),
                auction.getHighestBidder() == null ? null : auction.getHighestBidder().getUsername(),
                auction.getEndTime(),
                statusStr,
                historyList
        );
    }

    private BidTransactionEntity toBidTransactionEntity(String auctionId, BidTransaction transaction) {
        String createdAt = transaction.getCreatedAt() == null
                ? null
                : transaction.getCreatedAt().atZone(ZoneId.systemDefault()).format(DB_TIMESTAMP_FORMAT);
        return new BidTransactionEntity(
                transaction.getId(),
                auctionId,
                transaction.getBidderUsername(),
                transaction.getAmount(),
                createdAt
        );
    }

    private String formatBidHistory(BidTransaction transaction) {
        return String.format(
                "[%s] %s đặt %.2f USD",
                transaction.getCreatedAt(),
                transaction.getBidderUsername(),
                transaction.getAmount()
        );
    }
}
