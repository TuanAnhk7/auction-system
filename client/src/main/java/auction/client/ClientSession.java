package auction.client;

import auction.common.model.network.Role;

public final class ClientSession {
    private static volatile String username = "guest";
    private static volatile Role role = Role.BIDDER;
    private static volatile double balance = 0.0;

    private ClientSession() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ClientSession.username = username;
    }

    public static Role getRole() {
        return role;
    }

    public static void setRole(Role role) {
        ClientSession.role = role;
    }

    public static double getBalance() {
        return balance;
    }

    public static void setBalance(double balance) {
        ClientSession.balance = balance;
    }

    public static void reset() {
        username = "guest";
        role = Role.BIDDER;
        balance = 0.0;
    }
}
