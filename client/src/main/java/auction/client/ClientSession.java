package auction.client;

import auction.common.model.network.Role;

public final class ClientSession {
    private static volatile String username = "guest";
    private static volatile Role role = Role.BIDDER;

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
}
