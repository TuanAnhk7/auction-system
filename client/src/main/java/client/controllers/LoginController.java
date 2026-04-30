package client.controllers;

import client.MainClient;
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

        System.out.println("Đang đăng nhập cho user: " + username);

        try {
            // Khởi tạo đối tượng MainClient để gọi hàm chuyển cảnh
            MainClient mainApp = new MainClient();

            // Chuyển sang màn hình danh sách đấu giá
            // Đảm bảo bạn đã tạo file auction-list-view.fxml trong resources/client/views/
            mainApp.changeScene("auction-list-view.fxml");

        } catch (IOException e) {
            System.err.println("Lỗi: Không tìm thấy file auction-list-view.fxml tại resources/client/views/");
            e.printStackTrace();
        }
    }
}