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

    public synchronized List<BidTransaction> placeBid(String auctionId, String bidderUsername, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = activeAuctions.get(auctionId);
        if (auction == null) {
            throw new InvalidBidException("Auction not found or is not active.");
        }

        UserAccount bidderAccount = userManager.findByUsername(bidderUsername);
        if (bidderAccount == null) {
            throw new InvalidBidException("Bidder account not found.");
        }

        Bidder initialHighestBidder = auction.getHighestBidder();
        double initialHighestBidAmount = auction.getCurrentHighestBid();

        double availableBalance = bidderAccount.getAccountBalance();
        if (initialHighestBidder != null && bidderUsername.equals(initialHighestBidder.getUsername())) {
            availableBalance += initialHighestBidAmount;
        }

        if (availableBalance < bidAmount) {
            throw new InvalidBidException("Bidder balance is not enough for this bid.");
        }

        Bidder currentBidder = new Bidder(bidderAccount.getUsername(), bidderAccount.getPassword(), bidderAccount.getUsername(), availableBalance);
        
        List<BidTransaction> transactions = auction.placeBid(currentBidder, bidAmount);
        handleBalanceUpdates(auction, initialHighestBidder, initialHighestBidAmount, transactions);
        return transactions;
    }

    private void handleBalanceUpdates(Auction auction, Bidder initialBidder, double initialAmount, List<BidTransaction> transactions) {
        Bidder lastBidder = initialBidder;
        double lastAmount = initialAmount;

        for (BidTransaction tx : transactions) {
            // Refund the previous highest bidder
            if (lastBidder != null && lastBidder.getUsername() != null) {
                userManager.updateAccountBalance(lastBidder.getUsername(), lastAmount);
            }
            // Deduct from the new highest bidder
            userManager.updateAccountBalance(tx.getBidderUsername(), -tx.getAmount());

            // Update for next iteration
            // We need a Bidder object for lastBidder. tx only has username/id.
            // But we only need username and amount for refunding.
            lastBidder = new Bidder(tx.getBidderUsername(), "", tx.getBidderUsername(), 0.0);
            lastAmount = tx.getAmount();
        }
    }

    public List<BidTransaction> placeBidByItemId(String itemId, String bidderUsername, double bidAmount)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = activeAuctions.values().stream()
                .filter(a -> a.getItem().getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new InvalidBidException("Item not found in any active auction."));
        
        return placeBid(auction.getId(), bidderUsername, bidAmount);
    }

    public synchronized void registerAutoBid(String auctionIdOrItemId, String bidderUsername, double maxBid, double increment)
            throws AuctionClosedException, InvalidBidException {
        Auction auction = findById(auctionIdOrItemId)
                .or(() -> findByItemId(auctionIdOrItemId))
                .orElseThrow(() -> new InvalidBidException("Auction or item not found."));
        
        UserAccount bidderAccount = userManager.findByUsername(bidderUsername);
        if (bidderAccount == null) throw new InvalidBidException("Account not found.");
        
        Bidder initialHighestBidder = auction.getHighestBidder();
        double initialHighestBidAmount = auction.getCurrentHighestBid();

        Bidder bidder = new Bidder(bidderAccount.getUsername(), bidderAccount.getPassword(), bidderAccount.getUsername(), bidderAccount.getAccountBalance());
        List<BidTransaction> transactions = auction.registerAutoBid(bidder, maxBid, increment);
        handleBalanceUpdates(auction, initialHighestBidder, initialHighestBidAmount, transactions);
    }

    public void removeAuction(String auctionId) {
        Auction auction = activeAuctions.remove(auctionId);
        if (auction != null) {
            archivedAuctions.put(auctionId, auction);
        }
    }

    public List<Auction> closeExpiredAuctions() {
        List<Auction> changedAuctions = new ArrayList<>();
        for (Auction auction : activeAuctions.values()) {
            if (auction.updateStatusIfExpired()) {
                changedAuctions.add(auction);
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

        Item item = auction.getItem();
        item.setName(newName);
        item.setDescription(newDescription);
        item.setStartingPrice(newPrice);

        if (auction.getBidHistory().isEmpty()) {
            item.setCurrentPrice(newPrice);
            auction.setCurrentHighestBid(newPrice);
        }

        return auction;
    }
}
