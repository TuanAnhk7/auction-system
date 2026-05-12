package auction.server.network;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.auction.AuctionManager;
import auction.common.model.item.Item;
import auction.common.model.network.BidRequest;
import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListRequest;
import auction.common.model.network.GetAuctionListResponse;
import auction.common.model.user.Bidder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuctionServer server;
    private final AuctionManager auctionManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, AuctionServer server, AuctionManager auctionManager) {
        this.socket = socket;
        this.server = server;
        this.auctionManager = auctionManager;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Object receivedData = in.readObject();
                if (receivedData instanceof BidRequest request) {
                    handleBidRequest(request);
                } else if (receivedData instanceof GetAuctionListRequest) {
                    handleAuctionListRequest();
                }
            }
        } catch (Exception e) {
            System.out.println(">> Client " + socket.getInetAddress() + " đã ngắt kết nối.");
        } finally {
            server.unregister(this);
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void send(BidResponse response) {
        try {
            if (out != null) {
                out.writeObject(response);
                out.flush();
                out.reset();
            }
        } catch (IOException e) {
            System.err.println("Khong gui duoc goi tin realtime den client.");
        }
    }

    private void handleBidRequest(BidRequest request) {
        try {
            // Demo: tao bidder tam theo username gui len.
            Bidder bidder = new Bidder(request.getUsername(), "", request.getUsername(), 100_000.0);
            auctionManager.placeBidByItemId(request.getItemId(), bidder, request.getBidAmount());

            Item updatedItem = auctionManager.findByItemId(request.getItemId())
                    .orElseThrow(() -> new InvalidBidException("Khong tim thay phien dau gia sau khi cap nhat."))
                    .getItem();

            server.broadcast(new BidResponse(true, "Dat gia thanh cong.", updatedItem));
        } catch (AuctionClosedException | InvalidBidException e) {
            send(new BidResponse(false, e.getMessage(), null));
        }
    }

    private void handleAuctionListRequest() {
        try {
            sendAuctionList(new GetAuctionListResponse(auctionManager.getActiveItems()));
        } catch (Exception e) {
            System.err.println("Khong gui duoc danh sach dau gia hien tai.");
        }
    }

    private synchronized void sendAuctionList(GetAuctionListResponse response) throws IOException {
        if (out != null) {
            out.writeObject(response);
            out.flush();
            out.reset();
        }
    }
}
