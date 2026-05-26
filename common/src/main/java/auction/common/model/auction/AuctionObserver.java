package auction.common.model.auction;

public interface AuctionObserver {
    void updatePrice(double newPrice);
    void onNewBid(BidTransaction transaction);
}