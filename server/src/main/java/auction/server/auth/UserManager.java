package auction.server.auth;

import auction.common.model.network.Role;
import auction.common.model.network.UserAccount;
import auction.common.model.auction.IUserManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserManager implements IUserManager{
    private static volatile UserManager instance;

    private final ConcurrentMap<String, UserAccount> users = new ConcurrentHashMap<>();

    private UserManager() {
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
        users.put("admin", new UserAccount("admin", "admin123", Role.ADMIN, 0.0)); // Admin không cần số dư
        users.put("seller", new UserAccount("seller", "seller123", Role.SELLER, 5000.0)); // Seller có số dư ban đầu
        users.put("bidder", new UserAccount("bidder", "bidder123", Role.BIDDER, 10000.0)); // Bidder có số dư ban đầu
    }

    public boolean register(String username, String password, Role role) {
        UserAccount newUser = new UserAccount(username, password, role, 1000.0); // Số dư mặc định cho người dùng mới
        return users.putIfAbsent(username, newUser) == null;
    }

    public UserAccount authenticate(String username, String password) {
        UserAccount user = users.get(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }

    public UserAccount findByUsername(String username) {
        return users.get(username);
    }

    public synchronized void updateAccountBalance(String username, double amount) {
        UserAccount user = users.get(username);
        if (user != null) {
            user.setAccountBalance(user.getAccountBalance() + amount);
        }
    }
}