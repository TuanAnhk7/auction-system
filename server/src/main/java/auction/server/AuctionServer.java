package auction.server;

public final class AuctionServer {
    private AuctionServer() {
    }

    public static void main(String[] args) {
        auction.server.network.AuctionServer.main(args);
    }
}
