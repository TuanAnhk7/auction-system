package auction.common.model.auction;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.AuctionException;
import auction.common.exception.InvalidBidException;
import auction.common.model.item.Antique;
import auction.common.model.item.Art;
import auction.common.model.item.Item;
import auction.common.model.user.Bidder;
import auction.common.model.network.UserAccount;


import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
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
    private final ConcurrentMap<String, Auction> archivedAuctions;
    private final IUserManager userManager;

    private AuctionManager(IUserManager userManager) {
        this.activeAuctions = new ConcurrentHashMap<>();
        this.archivedAuctions = new ConcurrentHashMap<>();
        this.userManager = userManager;
    }

    public static AuctionManager getInstance(IUserManager userManager) {
        if (instance == null) {
            synchronized (AuctionManager.class) {
                if (instance == null) {
                    instance = new AuctionManager(userManager);
                }
            }
        }
        return instance;
    }

    public Auction createAuction(
            String sellerUsername,
            String type,
            String name,
            String description,
            double startingPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String specificProp1,
            int specificProp2
    ) {
        Item item = null;
        ZoneId zoneId = ZoneId.systemDefault();
        Instant startInstant = startTime.atZone(zoneId).toInstant();
        Instant endInstant = endTime.atZone(zoneId).toInstant();

        if ("art".equalsIgnoreCase(type)) {
            item = new Art(name, description, startingPrice, startInstant, endInstant, sellerUsername, specificProp1, specificProp2);
        } else if ("antique".equalsIgnoreCase(type) || "antiques".equalsIgnoreCase(type)) {
            item = new Antique(name, description, startingPrice, startInstant, endInstant, sellerUsername, specificProp1, specificProp2);
        }
        if (item != null) {
            Auction auction = new Auction(item, startTime, endTime, sellerUsername);
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

    public List<Auction> getArchivedAuctions() {
        return Collections.unmodifiableList(new ArrayList<>(archivedAuctions.values()));
    }

    public List<Item> getActiveItems() {
        return getActiveAuctions().stream()
                .map(Auction::getItem)
                .collect(Collectors.toList());
    }

    public Optional<Auction> findById(String auctionId) {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            auction = archivedAuctions.get(auctionId);
        }
        return Optional.ofNullable(auction);
    }

    public Optional<Auction> findByItemId(String itemId) {
        Optional<Auction> activeAuction = activeAuctions.values().stream()
                .filter(auction -> auction.getItem().getId().equals(itemId))
                .findFirst();
        if (activeAuction.isPresent()) {
            return activeAuction;
        }
        return archivedAuctions.values().stream()
                .filter(auction -> auction.getItem().getId().equals(itemId))
                .findFirst();
    }

    public BidTransaction placeBid(String auctionId, String bidderUsername, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Auction not found or is not active.");
        }

        UserAccount bidderAccount = userManager.findByUsername(bidderUsername);
        if (bidderAccount == null) {
            throw new InvalidBidException("Bidder account not found.");
        }
        if (bidderAccount.getAccountBalance() < bidAmount) {
            throw new InvalidBidException("Bidder balance is not enough for this bid.");
        }

        Bidder previousHighestBidder = auction.getHighestBidder();
        double previousHighestBidAmount = auction.getCurrentHighestBid();

        Bidder currentBidder = new Bidder(bidderAccount.getUsername(), bidderAccount.getPassword(), bidderAccount.getUsername(), bidderAccount.getAccountBalance());

        BidTransaction transaction = auction.placeBid(currentBidder, bidAmount);

        if (transaction != null) {
            userManager.updateAccountBalance(bidderUsername, -bidAmount);

            if (previousHighestBidder != null && previousHighestBidder.getUsername() != null) {
                userManager.updateAccountBalance(previousHighestBidder.getUsername(), previousHighestBidAmount);
            }
        }
        return transaction;
    }

    public BidTransaction placeBidByItemId(String itemId, String bidderUsername, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = activeAuctions.values().stream()
                .filter(a -> a.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new InvalidBidException("Item not found in any active auction."));
        
        return placeBid(auction.getId(), bidderUsername, bidAmount);
    }

    public void removeAuction(String auctionId) {
        Auction auction = activeAuctions.remove(auctionId);
        if (auction != null) {
            archivedAuctions.put(auctionId, auction);
        }
    }

    public List<Auction> closeExpiredAuctions() {
        List<Auction> changedAuctions = new ArrayList<>();
        List<String> toArchiveIds = new ArrayList<>();

        for (Auction auction : activeAuctions.values()) {
            if (auction.updateStatusIfExpired()) {
                changedAuctions.add(auction);
            }
            if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.CANCELED) {
                toArchiveIds.add(auction.getId());
            }
        }

        for (String auctionId : toArchiveIds) {
            Auction archived = activeAuctions.remove(auctionId);
            if (archived != null) {
                archivedAuctions.put(auctionId, archived);
            }
        }
        return changedAuctions;
    }

    public Auction updateAuctionStatus(String auctionId, String action) throws AuctionException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionException("Auction not found or is not active.");
        }

        switch (action.toUpperCase()) {
            case "START" -> auction.startAuction();
            case "END" -> auction.finishAuction();
            case "CANCEL" -> auction.cancel();
            default -> throw new AuctionException("Unsupported admin action: " + action);
        }
        return auction;
    }

    public Auction updateItem(String auctionId, String newName, double newPrice, String newDescription) throws AuctionException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionException("Auction not found or is not active.");
        }

        if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.PENDING) {
            throw new AuctionException("Cannot update item for an auction that is not OPEN or PENDING.");
        }

        Item item = auction.getItem();
        item.setName(newName);
        item.setDescription(newDescription);
        item.setStartingPrice(newPrice);

        return auction;
    }

    public Auction updateItem(
            String auctionId,
            String name,
            String description,
            double startingPrice,
            String itemType,
            String specificProp1,
            int specificProp2
    ) throws AuctionException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new AuctionException("Auction not found or is not active.");
        }

        if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.PENDING) {
            throw new AuctionException("Cannot update item for an auction that is not OPEN or PENDING.");
        }

        Item item = auction.getItem();
        item.setName(name);
        item.setDescription(description);
        item.setStartingPrice(startingPrice);

        if ("art".equalsIgnoreCase(itemType) && item instanceof Art art) {
            art.setArtist(specificProp1);
            art.setYearCreated(specificProp2);
        } else if (("antique".equalsIgnoreCase(itemType) || "antiques".equalsIgnoreCase(itemType)) && item instanceof Antique antique) {
            antique.setOrigin(specificProp1);
            antique.setEstimatedAge(specificProp2);
        } else if (!item.getCategory().equalsIgnoreCase(itemType)) {
            throw new AuctionException("Item type mismatch or unsupported type for update.");
        }

        return auction;
    }
}