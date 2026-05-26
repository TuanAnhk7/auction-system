package auction.common.repository;

import auction.common.model.auction.Auction;
import auction.common.model.auction.AuctionStatus;
import auction.common.exception.DataAccessException;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    Optional<Auction> findById(String id) throws DataAccessException;
    List<Auction> findAll() throws DataAccessException;
    List<Auction> findByStatus(AuctionStatus status) throws DataAccessException;
    Auction save(Auction auction) throws DataAccessException;
    void deleteById(String id) throws DataAccessException;
}