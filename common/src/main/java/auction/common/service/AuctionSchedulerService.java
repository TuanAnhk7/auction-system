package auction.common.service;

import java.time.LocalDateTime;
import auction.common.exception.DataAccessException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionSchedulerService {//tự động quét hệ thống để đóng cửa phòng hết giờ và chốt ng chiến thắng

    private final AuctionService auctionService;
    private final ScheduledExecutorService scheduler;
    private final long initialDelay;
    private final long period;

    public AuctionSchedulerService(AuctionService auctionService, long initialDelay, long period) {
        this.auctionService = auctionService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.initialDelay = initialDelay;
        this.period = period;
    }

    public void startScheduler() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Scheduler: Processing expired auctions at " + LocalDateTime.now());
                auctionService.processExpiredAuctions();
            } catch (Exception e) {
                System.err.println("Scheduler: Error during auction processing: " + e.getMessage());
                e.printStackTrace();
            }
        }, initialDelay, period, TimeUnit.SECONDS);
    }

    public void stopScheduler() {
        scheduler.shutdown();
        System.out.println("Scheduler: Shutting down.");
    }
}