package auction.server.auth;

import auction.common.model.network.Role;
import auction.common.model.network.UserAccount;
import auction.common.model.auction.IUserManager;
import auction.server.db.PersistenceManager;
import auction.server.db.entity.UserEntity;
import auction.server.db.repository.UserRepositoryImpl;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class UserManager implements IUserManager {
    private static volatile UserManager instance;
    private final UserRepositoryImpl userRepositoryImpl;

    private UserManager() {
        this.userRepositoryImpl = PersistenceManager.getInstance().getUserRepository();
        seedDefaultAccounts();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager();
                }
            }
        }
        return instance;
    }

    private void seedDefaultAccounts() {
        // Chỉ seed nếu chưa có
        if (userRepositoryImpl.findByUsername("admin") == null) {
            String adminHash = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
            userRepositoryImpl.save(new UserEntity("admin", adminHash, "ADMIN", 0.0, null));
        }
        if (userRepositoryImpl.findByUsername("seller") == null) {
            String sellerHash = BCrypt.withDefaults().hashToString(12, "seller123".toCharArray());
            userRepositoryImpl.save(new UserEntity("seller", sellerHash, "SELLER", 5000.0, null));
        }
        if (userRepositoryImpl.findByUsername("bidder") == null) {
            String bidderHash = BCrypt.withDefaults().hashToString(12, "bidder123".toCharArray());
            userRepositoryImpl.save(new UserEntity("bidder", bidderHash, "BIDDER", 10000.0, null));
        }
    }

    public boolean register(String username, String password, Role role) {
        if (userRepositoryImpl.findByUsername(username) != null) {
            return false; // User đã tồn tại
        }

        String passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        UserEntity newUser = new UserEntity(username, passwordHash, role.name(), 1000.0, null);
        userRepositoryImpl.save(newUser);
        return true;
    }

    public UserAccount authenticate(String username, String password) {
        UserEntity user = userRepositoryImpl.findByUsername(username);
        if (user == null) {
            return null;
        }

        // So sánh password hash
        if (!BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified) {
            return null;
        }

        return new UserAccount(
                user.getUsername(),
                user.getPassword(),
                Role.valueOf(user.getRole()),
                user.getAccountBalance()
        );
    }

    public UserAccount findByUsername(String username) {
        UserEntity user = userRepositoryImpl.findByUsername(username);
        if (user == null) return null;

        return new UserAccount(
                user.getUsername(),
                user.getPassword(),
                Role.valueOf(user.getRole()),
                user.getAccountBalance()
        );
    }

    public synchronized void updateAccountBalance(String username, double amount) {
        UserEntity user = userRepositoryImpl.findByUsername(username);
        if (user != null) {
            user.setAccountBalance(user.getAccountBalance() + amount);
            userRepositoryImpl.save(user);
        }
    }
}