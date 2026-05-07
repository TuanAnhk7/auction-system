package auction.common.model.auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuctionManager {
    private static volatile AuctionManager instance;

    private final List<Auction> activeAuctions;

    private AuctionManager() {
        this.activeAuctions = Collections.synchronizedList(new ArrayList<>());
    }

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

    public void addAuction(Auction auction) {
        activeAuctions.add(auction);
    }

    public List<Auction> getActiveAuctions() {
        synchronized (activeAuctions) {
            return List.copyOf(activeAuctions);
        }
    }

    public Optional<Auction> findById(String auctionId) {
        synchronized (activeAuctions) {
            return activeAuctions.stream()
                    .filter(auction -> auction.getId().equals(auctionId))
                    .findFirst();
        }
    }
}
