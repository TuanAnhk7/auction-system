package auction.common.model.auction;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.InvalidBidException;
import auction.common.model.item.Art;
import auction.common.model.item.Item;
import auction.common.model.user.Bidder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class AuctionManager {
    private static volatile AuctionManager instance;

    private final ConcurrentMap<String, Auction> activeAuctions;

    private AuctionManager() {
        this.activeAuctions = new ConcurrentHashMap<>();
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

    public Auction createAuction(String type, String name, double startingPrice, LocalDateTime endTime, Object... extraArgs) {
        Item item = null;
        if ("art".equalsIgnoreCase(type) && extraArgs.length >= 2) {
            String artist = (String) extraArgs[0];
            int year = (int) extraArgs[1];
            String description = "";
            item = new Art(name, description,  startingPrice, artist, year);
        }
        if (item != null) {
            Auction auction = new Auction(item, endTime);
            addAuction(auction);
            return auction;
        }
        return null;
    }

    public void addAuction(Auction auction) {
        activeAuctions.put(auction.getId(), auction);
    }

    public List<Auction> getActiveAuctions() {
        return Collections.unmodifiableList(new ArrayList<>(activeAuctions.values()));
    }

    public List<Item> getActiveItems() {
        return getActiveAuctions().stream()
                .map(Auction::getItem)
                .collect(Collectors.toList());
    }

    public Optional<Auction> findById(String auctionId) {
        return Optional.ofNullable(activeAuctions.get(auctionId));
    }

    public Optional<Auction> findByItemId(String itemId) {
        return activeAuctions.values().stream()
                .filter(auction -> auction.getItem().getId().equals(itemId))
                .findFirst();
    }

    public BidTransaction placeBid(String auctionId, Bidder bidder, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Auction not found.");
        }
        return auction.placeBid(bidder, bidAmount);
    }

    public BidTransaction placeBidByItemId(String itemId, Bidder bidder, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = findByItemId(itemId)
                .orElseThrow(() -> new InvalidBidException("Item not found in any active auction."));
        return auction.placeBid(bidder, bidAmount);
    }

    public void removeAuction(String auctionId) {
        activeAuctions.remove(auctionId);
    }
}
