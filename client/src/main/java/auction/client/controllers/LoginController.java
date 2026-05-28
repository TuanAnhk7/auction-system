package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.MainClient;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.model.network.LoginRequest;
import auction.common.model.network.LoginResponse;
import auction.common.model.network.RegisterRequest;
import auction.common.model.network.RegisterResponse;
import auction.common.model.network.Role;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.Optional;

public class LoginController implements Observer {

    // fx:id phải khớp chính xác với ID bạn đặt trong Scene Builder
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void initialize() {
        try {
            AuctionClient.getInstance().connect("127.0.0.1", 8080);
            // Màn hình login lắng nghe trực tiếp phản hồi đăng nhập/đăng ký từ server.
            AuctionClient.getInstance().addObserver(this);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không kết nối được đến server: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", "Tên đăng nhập không được để trống.");
            return;
        }

        if (password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", "Mật khẩu không được để trống.");
            return;
        }

        try {
            AuctionClient.getInstance().sendLoginRequest(new LoginRequest(username, password));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không gửi được yêu cầu đăng nhập đến server.");
        }
    }

    @FXML
    private void handleRegister() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký tài khoản");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField usernameInput = new TextField();
        PasswordField passwordInput = new PasswordField();
        ChoiceBox<Role> roleChoiceBox = new ChoiceBox<>();
        roleChoiceBox.getItems().addAll(Role.BIDDER, Role.SELLER, Role.ADMIN);
        roleChoiceBox.setValue(Role.BIDDER);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.addRow(0, new Label("Tên đăng nhập"), usernameInput);
        gridPane.addRow(1, new Label("Mật khẩu"), passwordInput);
        gridPane.addRow(2, new Label("Vai trò"), roleChoiceBox);
        dialog.getDialogPane().setContent(gridPane);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().getButtonData() != ButtonBar.ButtonData.OK_DONE) {
            return;
        }

        String username = usernameInput.getText().trim();
        String password = passwordInput.getText();
        Role role = roleChoiceBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", "Vui lòng nhập đủ tên đăng nhập, mật khẩu và vai trò.");
            return;
        }

        try {
            AuctionClient.getInstance().sendRegisterRequest(new RegisterRequest(username, password, role));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không gửi được yêu cầu đăng ký đến server.");
        }
    }

    @Override
    public void onBidResponse(auction.common.model.network.BidResponse response) {
        // Trống - Không xử lý tại màn hình Login
    }

    @Override
    public void onAuctionListResponse(auction.common.model.network.GetAuctionListResponse response) {
        // Trống - Không xử lý tại màn hình Login
    }

    @Override
    public void onLoginResponse(LoginResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", response.getMessage());
                return;
            }

            // Gọi Static lưu thông tin vào ClientSession chuẩn chỉ 100%
            ClientSession.setUsername(usernameField.getText().trim());
            ClientSession.setRole(response.getRole());
            ClientSession.setBalance(response.getBalance());
            AuctionClient.getInstance().removeObserver(this);

            try {
                switch (response.getRole()) {
                    case ADMIN -> MainClient.changeScene("admin-dashboard-view.fxml");
                    case SELLER -> MainClient.changeScene("seller-dashboard-view.fxml");
                    case BIDDER -> MainClient.changeScene("auction-list-view.fxml");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi giao diện", "Không mở được màn hình sau đăng nhập.");
            }
        });
    }

    @Override
    public void onRegisterResponse(RegisterResponse response) {
        Platform.runLater(() -> showAlert(
                response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                response.isSuccess() ? "Đăng ký thành công" : "Đăng ký thất bại",
                response.getMessage()
        ));
    }

    // --- Bổ sung thêm các hàm contract bắt buộc của Interface Observer để tránh lỗi gạch đỏ class ---
    @Override
    public void onCreateAuctionResponse(auction.common.model.network.CreateAuctionResponse response) {
        // Trống
    }

    @Override
    public void onUpdateItemResponse(auction.common.model.network.UpdateItemResponse response) {
        // Trống
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
