package auction.server.network;

import auction.common.exception.AuctionException;
import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionManager;
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

public class AuctionServer {
    private static final int PORT = 8080; // Cổng kết nối mà Server sẽ lắng nghe
    private final ExecutorService clientPool = Executors.newFixedThreadPool(8);
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final AuctionManager auctionManager = AuctionManager.getInstance();

    public static void main(String[] args) {
        new AuctionServer().start();
    }

    public void start() {
        bootstrapSampleAuctions();
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
}
