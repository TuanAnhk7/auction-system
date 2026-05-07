package auction.client.controllers;

import auction.client.MainClient;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainViewController {

    @FXML
    private void handleStart() {
        try {
            MainClient.changeScene("login-view.fxml");
        } catch (IOException e) {
            System.err.println("Khong mo duoc man hinh dang nhap.");
            e.printStackTrace();
        }
    }
}
