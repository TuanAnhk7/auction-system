package auction.server.db;

import auction.server.db.repository.ItemRepository;
import auction.server.db.repository.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceManager {
    private static volatile PersistenceManager instance;
    private final EntityManagerFactory emf;

    private final UserRepositoryImpl userRepositoryImpl;
    private final AuctionRepositoryImpl auctionRepositoryImpl;
    private final ItemRepository itemRepository;
    private final BidTransactionRepository bidTransactionRepository;

    private PersistenceManager() {
        this.emf = Persistence.createEntityManagerFactory("auction-pu");
        this.userRepositoryImpl = new UserRepositoryImpl(emf);
        this.auctionRepositoryImpl = new AuctionRepositoryImpl(emf);
        this.itemRepository = new ItemRepository(emf);
        this.bidTransactionRepository = new BidTransactionRepository(emf);
    }

    public static PersistenceManager getInstance() {
        if (instance == null) {
            synchronized (PersistenceManager.class) {
                if (instance == null) {
                    instance = new PersistenceManager();
                }
            }
        }
        return instance;
    }

    public UserRepositoryImpl getUserRepository() {
        return userRepositoryImpl;
    }

    public AuctionRepositoryImpl getAuctionRepository() {
        return auctionRepositoryImpl;
    }

    public ItemRepository getItemRepository() {
        return itemRepository;
    }

    public BidTransactionRepository getBidTransactionRepository() {
        return bidTransactionRepository;
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}