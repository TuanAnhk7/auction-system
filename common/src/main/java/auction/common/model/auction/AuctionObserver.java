package auction.common.model.auction;

public interface AuctionObserver {
    // Phương thức được gọi khi có giá mới
    void updatePrice(double newPrice);
}