package auction.common.model.auction;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.item.Art;
import auction.common.model.user.Bidder;
import auction.common.model.network.UserAccount;
import auction.common.model.network.Role; // Import Enum Role của bạn
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionConcurrencyTest {

    // 🔥 Lớp Fake thủ công khớp chính xác kiểu trả về UserAccount và Enum Role
    static class FakeUserManager implements IUserManager {
        @Override
        public UserAccount findByUsername(String username) {
            // Xác định Role phù hợp từ Enum của bạn (Thử thay thế bằng Role.CLIENT hoặc Role.USER nếu Role.BIDDER không tồn tại)
            Role userRole = Role.BIDDER;

            // Khởi tạo đối tượng UserAccount với cấu trúc: (username, password, role, balance)
            return new UserAccount(username, "pwd", userRole, 10000.0);
        }

        @Override
        public void updateAccountBalance(String username, double balance) {
            // Để trống vì hàm này không ảnh hưởng trực tiếp đến luồng xử lý bid đồng thời
        }
    }

    @Test
    void concurrentBidsKeepHighestPriceConsistent() throws Exception {
        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(10, ChronoUnit.MINUTES);

        Art testArt = new Art("Test Art", "Concurrent bid test", 100.0, startTime, endTime, "testSeller", "Artist", 2020);

        LocalDateTime startLdt = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        LocalDateTime endLdt = LocalDateTime.ofInstant(endTime, ZoneId.systemDefault());
        Auction auction = new Auction(testArt, startLdt, endLdt, "testSeller");
        auction.startAuction();

        List<Bidder> bidders = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            bidders.add(new Bidder("bidder" + i, "pwd", "Bidder " + i, 10_000.0));
        }

        ExecutorService executor = Executors.newFixedThreadPool(bidders.size());
        CountDownLatch startGate = new CountDownLatch(1);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < bidders.size(); i++) {
            Bidder bidder = bidders.get(i);
            double bidAmount = 200.0 + (i * 50.0);
            tasks.add(() -> tryBid(auction, bidder, bidAmount, startGate));
        }

        List<Future<Boolean>> results = new ArrayList<>();
        for (Callable<Boolean> task : tasks) {
            results.add(executor.submit(task));
        }

        startGate.countDown();

        int successCount = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successCount++;
            }
        }
        executor.shutdown();

        assertTrue(successCount >= 1);
        assertEquals(550.0, auction.getCurrentHighestBid());
        assertEquals(550.0, auction.getItem().getCurrentPrice());
        assertEquals(successCount, auction.getBidHistory().size());
        assertEquals("bidder7", auction.getHighestBidder().getUsername());
    }

    @Test
    void auctionManagerRoutesConcurrentBidsToSingleAuctionSafely() throws Exception {
        IUserManager notificationService = new FakeUserManager();
        AuctionManager manager = AuctionManager.getInstance(notificationService);

        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(10, ChronoUnit.MINUTES);

        Art managedArt = new Art("Managed Art", "Manager concurrency test", 300.0, startTime, endTime, "testSeller", "Artist", 2021);

        LocalDateTime startLdt = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        LocalDateTime endLdt = LocalDateTime.ofInstant(endTime, ZoneId.systemDefault());
        Auction auction = new Auction(managedArt, startLdt, endLdt, "testSeller");
        auction.startAuction();
        manager.addAuction(auction);

        List<Bidder> bidders = List.of(
                new Bidder("a", "pwd", "A", 10_000.0),
                new Bidder("b", "pwd", "B", 10_000.0),
                new Bidder("c", "pwd", "C", 10_000.0)
        );

        ExecutorService executor = Executors.newFixedThreadPool(bidders.size());
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        results.add(executor.submit(() -> tryBid(manager, auction.getId(), bidders.get(0), 350.0, startGate)));
        results.add(executor.submit(() -> tryBid(manager, auction.getId(), bidders.get(1), 400.0, startGate)));
        results.add(executor.submit(() -> tryBid(manager, auction.getId(), bidders.get(2), 450.0, startGate)));

        startGate.countDown();

        assertTrue(results.get(2).get());
        executor.shutdown();

        assertEquals(450.0, auction.getCurrentHighestBid());
        assertEquals("c", auction.getHighestBidder().getUsername());
        manager.removeAuction(auction.getId());

        assertTrue(manager.findById(auction.getId()).isPresent());
        assertFalse(manager.getActiveAuctions().contains(auction));
        assertTrue(manager.getArchivedAuctions().contains(auction));
    }

    private boolean tryBid(Auction auction, Bidder bidder, double bidAmount, CountDownLatch startGate)
            throws InterruptedException {
        startGate.await();
        try {
            auction.placeBid(bidder, bidAmount);
            return true;
        } catch (AuctionClosedException | InvalidBidException e) {
            return false;
        }
    }

    private boolean tryBid(AuctionManager manager, String auctionId, Bidder bidder, double bidAmount, CountDownLatch startGate)
            throws InterruptedException {
        startGate.await();
        try {
            manager.placeBid(auctionId, bidder.getUsername(), bidAmount);
            return true;
        } catch (AuctionClosedException | InvalidBidException e) {
            return false;
        }
    }
}