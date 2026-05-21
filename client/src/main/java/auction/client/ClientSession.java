package auction.client;

public final class ClientSession {
    private static volatile String username = "guest";
    private static volatile String role = "Bidder";

    private ClientSession() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ClientSession.username = username;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String role) {
        ClientSession.role = role;
    }
}
