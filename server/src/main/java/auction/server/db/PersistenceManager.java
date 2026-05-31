package auction.server.db;

import auction.server.db.repository.ItemRepository;
import auction.server.db.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PersistenceManager {
    private static final String SQLITE_PATH_PROPERTY = "auction.sqlite.path";
    private static final String SQLITE_FILE_NAME = "auction.db";
    private static volatile PersistenceManager instance;
    private final EntityManagerFactory emf;

    private final UserRepositoryImpl userRepositoryImpl;
    private final AuctionRepositoryImpl auctionRepositoryImpl;
    private final ItemRepository itemRepository;
    private final BidTransactionRepository bidTransactionRepository;

    private PersistenceManager() {
        String sqlitePath = resolveSqlitePath();
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + sqlitePath);
        this.emf = Persistence.createEntityManagerFactory("auction-pu", overrides);
        this.userRepositoryImpl = new UserRepositoryImpl(emf);
        this.auctionRepositoryImpl = new AuctionRepositoryImpl(emf);
        this.itemRepository = new ItemRepository(emf);
        this.bidTransactionRepository = new BidTransactionRepository(emf);
    }

    private String resolveSqlitePath() {
        String configuredPath = System.getProperty(SQLITE_PATH_PROPERTY);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = Paths.get(System.getProperty("user.home"), SQLITE_FILE_NAME)
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        } else {
            configuredPath = Paths.get(configuredPath)
                    .toAbsolutePath()
                    .normalize()
                    .toString();
        }

        Path parent = Paths.get(configuredPath).getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (Exception e) {
                throw new IllegalStateException("Không thể tạo thư mục chứa SQLite DB: " + parent, e);
            }
        }

        String normalizedPath = configuredPath.replace("\\", "/");
        System.setProperty(SQLITE_PATH_PROPERTY, normalizedPath);
        return normalizedPath;
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

    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
