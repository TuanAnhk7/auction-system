package auction.client.network;

import auction.common.model.network.AdminAuctionActionRequest;
import auction.common.model.network.AdminAuctionActionResponse;
import auction.common.model.network.BidRequest;
import auction.common.model.network.BidResponse;
import auction.common.model.network.CreateAuctionRequest;
import auction.common.model.network.CreateAuctionResponse;
import auction.common.model.network.GetAuctionListRequest;
import auction.common.model.network.GetAuctionListResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuctionClient {
    private static final AuctionClient INSTANCE = new AuctionClient();

    private final List<Observer> observers = new CopyOnWriteArrayList<>();
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;

    private AuctionClient() {
    }

    public static AuctionClient getInstance() {
        return INSTANCE;
    }

    public synchronized void connect(String host, int port) throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        startListener();
    }

    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public synchronized void sendBidRequest(BidRequest request) throws IOException {
        if (out == null) {
            throw new IOException("Chua ket noi den server.");
        }

        out.writeObject(request);
        out.flush();
        out.reset();
    }

    public synchronized void requestAuctionList() throws IOException {
        if (out == null) {
            throw new IOException("Chua ket noi den server.");
        }

        out.writeObject(new GetAuctionListRequest());
        out.flush();
        out.reset();
    }

    public synchronized void sendCreateAuctionRequest(CreateAuctionRequest request) throws IOException {
        if (out == null) {
            throw new IOException("Chua ket noi den server.");
        }

        out.writeObject(request);
        out.flush();
        out.reset();
    }

    public synchronized void sendAdminAuctionActionRequest(AdminAuctionActionRequest request) throws IOException {
        if (out == null) {
            throw new IOException("Chua ket noi den server.");
        }

        out.writeObject(request);
        out.flush();
        out.reset();
    }

    public synchronized void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    Object incoming = in.readObject();
                    if (incoming instanceof BidResponse response) {
                        notifyBidObservers(response);
                    } else if (incoming instanceof AdminAuctionActionResponse response) {
                        notifyAdminAuctionActionObservers(response);
                    } else if (incoming instanceof CreateAuctionResponse response) {
                        notifyCreateAuctionObservers(response);
                    } else if (incoming instanceof GetAuctionListResponse response) {
                        notifyAuctionListObservers(response);
                    }
                }
            } catch (Exception e) {
                System.out.println("Mat ket noi realtime voi server.");
            }
        }, "auction-client-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void notifyBidObservers(BidResponse response) {
        for (Observer observer : observers) {
            observer.onBidResponse(response);
        }
    }

    private void notifyAuctionListObservers(GetAuctionListResponse response) {
        for (Observer observer : observers) {
            observer.onAuctionListResponse(response);
        }
    }

    private void notifyCreateAuctionObservers(CreateAuctionResponse response) {
        for (Observer observer : observers) {
            observer.onCreateAuctionResponse(response);
        }
    }

    private void notifyAdminAuctionActionObservers(AdminAuctionActionResponse response) {
        for (Observer observer : observers) {
            observer.onAdminAuctionActionResponse(response);
        }
    }
}
