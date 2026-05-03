package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainClient extends Application {
    private static Stage stg; // Biến tĩnh để giữ cửa sổ chính phục vụ việc chuyển cảnh

    @Override
    public void start(Stage stage) throws IOException {
        stg = stage;
        stage.setResizable(false);

        // Màn hình đầu tiên hiện lên sẽ là Login
        showLogin();
    }

    // Hàm hiển thị màn hình Đăng nhập
    public void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/views/login-view.fxml"));
        Parent root = loader.load();
        stg.setTitle("UET Auction - Đăng nhập");
        stg.setScene(new Scene(root, 400, 300));
        stg.show();
    }

    // Hàm chuyển sang màn hình Danh sách đấu giá (Sẽ được gọi từ LoginController)
    public void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource("/client/views/" + fxml));
        stg.getScene().setRoot(pane);
        stg.sizeToScene(); // Tự động căn chỉnh lại kích thước cửa sổ cho khớp màn hình mới
        stg.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}

