package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.MainClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {

    // fx:id phải khớp chính xác với ID bạn đặt trong Scene Builder
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Bidder", "Seller", "Admin"));
        roleComboBox.setValue("Bidder");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            System.out.println("Lỗi: Tên đăng nhập không được để trống!");
            return;
        }

        if (password.isEmpty()) {
            System.out.println("Lỗi: Mật khẩu không được để trống!");
            return;
        }

        System.out.println("Đang đăng nhập cho user: " + username);
        ClientSession.setUsername(username);
        ClientSession.setRole(roleComboBox.getValue());

        try {
            if ("Seller".equalsIgnoreCase(ClientSession.getRole())) {
                MainClient.changeScene("seller-dashboard-view.fxml");
            } else if ("Admin".equalsIgnoreCase(ClientSession.getRole())) {
                MainClient.changeScene("admin-dashboard-view.fxml");
            } else {
                MainClient.changeScene("auction-list-view.fxml");
            }

        } catch (IOException e) {
            System.err.println("Lỗi: Không mở được màn hình sau đăng nhập.");
            e.printStackTrace();
        }
    }
}
