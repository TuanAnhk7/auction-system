package auction.client;

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
        stage.setResizable(true);

        // Màn hình đầu tiên hiện lên sẽ là trang chao
        showMainView();
    }

    public void showMainView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auction/client/views/main-view.fxml"));
        Parent root = loader.load();
        stg.setTitle("UET Auction");
        stg.setScene(new Scene(root, 420, 280));
        stg.show();
    }

    // Hàm chuyển màn hình dùng chung cho cac controller
    public static void changeScene(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(MainClient.class.getResource("/auction/client/views/" + fxml));
        stg.getScene().setRoot(pane);
        stg.sizeToScene(); // Tự động căn chỉnh lại kích thước cửa sổ cho khớp màn hình mới
        stg.centerOnScreen();
    }

    public static void main(String[] args) {
        launch();
    }
}
