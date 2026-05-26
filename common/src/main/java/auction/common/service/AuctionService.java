package auction.common.service;

import auction.common.exception.AuctionClosedException;
import auction.common.exception.AuctionException;
import auction.common.exception.InvalidBidException;
import auction.common.exception.DataAccessException;
import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionStatus;
import auction.common.model.auction.BidTransaction;
import auction.common.model.item.Item;
import auction.common.model.user.Bidder;
import auction.common.model.user.Seller;
import auction.common.model.user.User;
import auction.common.repository.AuctionRepository;
import auction.common.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AuctionService(AuctionRepository auctionRepository, UserRepository userRepository, NotificationService notificationService) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private static final int MAX_RETRIES = 3; // số lần thử lại max
    private static final long RETRY_DELAY_MS = 100; // Độ trễ giữa các lần thử lại

    public Optional<Auction> createAuction(Item item, LocalDateTime startTime, LocalDateTime endTime, String sellerId) {
        if (item == null) throw new IllegalArgumentException("Item cannot be null for auction creation.");
        if (startTime == null) throw new IllegalArgumentException("Start time cannot be null for auction creation.");
        if (endTime == null) throw new IllegalArgumentException("End time cannot be null for auction creation.");
        if (sellerId == null || sellerId.trim().isEmpty()) throw new IllegalArgumentException("Seller ID cannot be null or empty for auction creation.");
        if (endTime.isBefore(startTime)) throw new IllegalArgumentException("End time must be after start time.");
        if (endTime.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("End time must be in the future.");

        Optional<Seller> sellerOptional = userRepository.findById(sellerId)
                .filter(user -> user instanceof Seller)
                .map(user -> (Seller) user);
        if (sellerOptional.isEmpty()) {
            throw new IllegalArgumentException("Seller with ID " + sellerId + " not found.");
        }

        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Auction auction = new Auction(item, startTime, endTime, sellerOptional.get().getId());
                return Optional.of(auctionRepository.save(auction));
            } catch (DataAccessException e) {
                if (i < MAX_RETRIES - 1) {
                    System.err.println("Data access error during auction creation, retrying... (" + (i + 1) + "/" + MAX_RETRIES + ")");
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new IllegalArgumentException("Auction creation interrupted.", ie); }
                } else {
                    throw new IllegalArgumentException("Failed to create auction after " + MAX_RETRIES + " retries.", e);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<BidTransaction> placeBid(String auctionId, String bidderId, double bidAmount)
            throws AuctionClosedException, InvalidBidException, AuctionException {
        Optional<Auction> auctionOptional = retryDataAccessException(() -> auctionRepository.findById(auctionId), "fetching auction for bid");
        if (auctionOptional.isEmpty()) {
            throw new AuctionException("Auction with ID " + auctionId + " not found.");
        }

        Auction auction = auctionOptional.get();
        auction.updateStatusIfExpired();

        if (!auction.canAcceptBids()) {
            throw new AuctionClosedException("Auction is not accepting bids. Current status: " + auction.getStatus());
        }

        Optional<Bidder> bidderOptional = retryDataAccessException(() -> userRepository.findById(bidderId), "fetching bidder for bid")
                .filter(user -> user instanceof Bidder)
                .map(user -> (Bidder) user);

        if (bidderOptional.isEmpty()) {
            throw new AuctionException("Bidder with ID " + bidderId + " not found.");
        }

        Bidder bidder = bidderOptional.get();
        BidTransaction transaction = auction.placeBid(bidder, bidAmount);
        retryDataAccessException(() -> auctionRepository.save(auction), "saving auction after bid");
        return Optional.of(transaction);
    }

    public Optional<Auction> getAuctionDetails(String auctionId) {
        return retryDataAccessException(() -> auctionRepository.findById(auctionId), "fetching auction details");
    }

    public List<Auction> getActiveAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.RUNNING);
    }

    public List<BidTransaction> getBidHistory(String auctionId) throws AuctionException {
        Optional<Auction> auctionOptional = retryDataAccessException(() -> auctionRepository.findById(auctionId), "fetching bid history");
        if (auctionOptional.isEmpty()) {
            throw new AuctionException("Auction with ID " + auctionId + " not found.");
        }
        return auctionOptional.get().getBidHistory();
    }

    public void startAuction(String auctionId) throws AuctionException {
        Optional<Auction> auctionOptional = retryDataAccessException(() -> auctionRepository.findById(auctionId), "starting auction");
        if (auctionOptional.isEmpty()) {
            throw new AuctionException("Auction with ID " + auctionId + " not found.");
        }
        auctionOptional.get().startAuction();
        retryDataAccessException(() -> auctionRepository.save(auctionOptional.get()), "saving auction after start");
    }

    public void finalizeAuction(String auctionId) throws AuctionException {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        if (auctionOptional.isEmpty()) {
            throw new AuctionException("Auction with ID " + auctionId + " not found.");
        }

        Auction auction = auctionOptional.get();
        auction.finishAuction();

        Bidder winner = auction.getHighestBidder();
        double finalBidAmount = auction.getCurrentHighestBid();
        String sellerId = auction.getItem().getSellerId();

        if (winner != null) {
            Optional<User> winnerUserOptional = userRepository.findById(winner.getId());
            if (winnerUserOptional.isPresent() && winnerUserOptional.get() instanceof Bidder) {
                Bidder actualWinner = (Bidder) winnerUserOptional.get();
                if (actualWinner.getAccountBalance() >= finalBidAmount) {
                    actualWinner.setAccountBalance(actualWinner.getAccountBalance() - finalBidAmount);
                    userRepository.save(actualWinner);
                } else {
                }
            }

            Optional<User> sellerUserOptional = userRepository.findById(sellerId);
            if (sellerUserOptional.isPresent() && sellerUserOptional.get() instanceof Seller) {
                Seller actualSeller = (Seller) sellerUserOptional.get();
                actualSeller.setRating(actualSeller.getRating() + 0.1);
                userRepository.save(actualSeller);
            }
        } else {
            System.out.println("Auction " + auctionId + " finished with no bids.");
        }

        notificationService.notifyAuctionWinner(auction, winner);
        userRepository.findById(sellerId)
                .filter(user -> user instanceof Seller)
                .map(user -> (Seller) user)
                .ifPresent(seller -> notificationService.notifySellerAuctionEnded(auction, seller));

        retryDataAccessException(() -> auctionRepository.save(auction), "saving finalized auction");
    }

    public void processExpiredAuctions() {
        List<Auction> auctionsToProcess = retryDataAccessException(() -> auctionRepository.findAll(), "fetching auctions for processing");
        for (Auction auction : auctionsToProcess) {
            boolean statusChanged = auction.updateStatusIfExpired();
            if (statusChanged) {
                retryDataAccessException(() -> auctionRepository.save(auction), "saving auction after status update");
                if (auction.getStatus() == AuctionStatus.FINISHED) {
                    try {
                        finalizeAuction(auction.getId());
                    } catch (AuctionException e) {
                        System.err.println("Error finalizing auction " + auction.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private <T> T retryDataAccessException(DataSupplier<T> supplier, String operationName) throws DataAccessException {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return supplier.get();
            } catch (DataAccessException e) {
                if (i < MAX_RETRIES - 1) {
                    System.err.println("Data access error during " + operationName + ", retrying... (" + (i + 1) + "/" + MAX_RETRIES + ")");
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new DataAccessException(operationName + " interrupted.", ie); }
                } else {
                    throw new DataAccessException("Failed to " + operationName + " after " + MAX_RETRIES + " retries.", e);
                }
            }
        }
        throw new DataAccessException("Unexpected error during " + operationName + ".");
    }

    @FunctionalInterface
    private interface DataSupplier<T> {
        T get() throws DataAccessException;
    }
}