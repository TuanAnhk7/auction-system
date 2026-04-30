package src.java.uet.models.business;

import java.util.ArrayList;
import java.util.List;

public class AuctionManager {
    // 1. Biến static lưu trữ thể hiện duy nhất của class (Thread-safe)
    private static volatile AuctionManager instance;

    // Danh sách lưu trữ tất cả các phiên đấu giá trên hệ thống
    private List<Auction> activeAuctions;

    // 2. Constructor BẮT BUỘC phải là private để áp dụng Singleton
    private AuctionManager() {
        this.activeAuctions = new ArrayList<>();
    }

    // 3. Hàm static để lấy ra thể hiện duy nhất (Double-checked locking)
    public static AuctionManager getInstance() {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager();
                }
            }
        }
        return instance;
    }

    // Các hàm nghiệp vụ quản lý danh sách đấu giá
    public void addAuction(Auction auction) {
        activeAuctions.add(auction);
    }

    public List<Auction> getActiveAuctions() {
        return activeAuctions;
    }
}