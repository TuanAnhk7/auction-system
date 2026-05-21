package auction.client;

public final class ClientSession {
    private static volatile String username = "guest";

    private ClientSession() {
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ClientSession.username = username;
    }
}
