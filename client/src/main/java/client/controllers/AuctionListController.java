package client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;

public class AuctionListController {

    @FXML
    private TableView<Object> auctionTable; // Thay Object bằng class Model của bạn (ví dụ: Item)

    @FXML
    private TableColumn<Object, String> itemNameColumn;

    @FXML
    private TableColumn<Object, Double> priceColumn;

    @FXML
    public void initialize() {
        // 1. Kết nối các cột với thuộc tính của đối tượng dữ liệu
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        // 2. Gọi lệnh lấy dữ liệu từ Server ở đây
        System.out.println("Đang tải danh sách vật phẩm từ Server...");
    }

    @FXML
    private void handleBid() {
        // Xử lý khi người dùng nhấn nút Đấu giá
        System.out.println("Đã gửi yêu cầu đấu giá!");
    }
}