package auction.client;

import javafx.application.Application;

public final class MainLauncher {
    private MainLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(MainClient.class, args);
    }
}
