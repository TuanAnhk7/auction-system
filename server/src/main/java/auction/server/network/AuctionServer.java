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
    private final AuctionManager auctionManager = AuctionManager.getInstance();

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
                auction.getStatus().name(),
                auction.getBidHistory().stream()
                        .map(this::formatBidHistory)
                        .toList()
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