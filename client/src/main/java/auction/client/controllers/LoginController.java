package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.MainClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
    public void initialize() {
        // Hàm này chạy tự động khi giao diện load lên
        System.out.println("Giao diện Login đã sẵn sàng!");
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

        try {
            MainClient.changeScene("auction-list-view.fxml");

        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file auction-list-view.fxml tại resources/auction/client/views/");
            e.printStackTrace();
        }
    }
}
