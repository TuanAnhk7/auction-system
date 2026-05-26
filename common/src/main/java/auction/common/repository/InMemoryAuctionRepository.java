package auction.common.repository;

import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionStatus;
import auction.common.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryAuctionRepository implements AuctionRepository {// kho lưu trữ dữ liệu quản lí cuộc đấu giá

    private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

    private boolean simulateNetworkError = false;
    private int errorCounter = 0;
    private int errorsBeforeSuccess = 2;

    public void setSimulateNetworkError(boolean simulateNetworkError) {
        this.simulateNetworkError = simulateNetworkError;
        this.errorCounter = 0;
    }

    private void checkAndSimulateError() {
        if (simulateNetworkError) {
            errorCounter++;
            if (errorCounter <= errorsBeforeSuccess) {
                throw new DataAccessException("Simulated network error during data access.");
            }
        }
    }

    @Override
    public Optional<Auction> findById(String id) throws DataAccessException {
        checkAndSimulateError();
        return Optional.ofNullable(auctions.get(id));
    }

    @Override
    public List<Auction> findAll() throws DataAccessException {
        checkAndSimulateError();
        return new ArrayList<>(auctions.values());
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) throws DataAccessException {
        checkAndSimulateError();
        return auctions.values().stream()
                .filter(auction -> auction.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Auction save(Auction auction) throws DataAccessException {
        checkAndSimulateError();
        auctions.put(auction.getId(), auction);
        return auction;
    }

    @Override
    public void deleteById(String id) throws DataAccessException {
        checkAndSimulateError();
        auctions.remove(id);
    }
}