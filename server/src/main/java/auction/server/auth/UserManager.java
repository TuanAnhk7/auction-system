package auction.server.auth;

import auction.common.model.network.Role;
import auction.common.model.network.UserAccount;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserManager {
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
        users.put("admin", new UserAccount("admin", "admin123", Role.ADMIN));
        users.put("seller", new UserAccount("seller", "seller123", Role.SELLER));
        users.put("bidder", new UserAccount("bidder", "bidder123", Role.BIDDER));
    }

    public boolean register(String username, String password, Role role) {
        UserAccount newUser = new UserAccount(username, password, role);
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
}
