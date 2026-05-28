package auction.server.network;

import auction.common.exception.AuctionException;
import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionManager;
import auction.common.model.auction.BidTransaction;
import auction.common.model.item.Art;
import auction.common.model.item.Item;
import auction.common.model.network.AuctionView;
import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListResponse;
import auction.common.support.SampleDataFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {
    private static final int PORT = 8080; // Cổng kết nối mà Server sẽ lắng nghe
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
                if (!auctionManager.closeExpiredAuctions().isEmpty()) {
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

        SampleDataFactory.createSampleArts().forEach(item -> {
            Auction auction = new Auction(item, LocalDateTime.now().plusHours(8));
            try {
                auction.startAuction();
            } catch (AuctionException e) {
                throw new IllegalStateException("Khong khoi tao duoc phien dau gia mau.", e);
            }
            auctionManager.addAuction(auction);
        });
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

    private String formatBidHistory(BidTransaction transaction) {
        return String.format(
                "[%s] %s đặt %.2f USD",
                transaction.getCreatedAt(),
                transaction.getBidderUsername(),
                transaction.getAmount()
        );
    }
}