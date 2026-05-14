package auction.common.model.auction;

public interface AuctionObserver {
    // Phương thức được gọi khi có giá mới
    void updatePrice(double newPrice);
    //Nhận transantion để bt ng đặt, tgian, giá mới
    void onNewBid(BidTransaction transaction);
}