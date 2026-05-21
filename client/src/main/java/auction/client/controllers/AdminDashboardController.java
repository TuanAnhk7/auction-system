package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.MainClient;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.model.network.AdminAuctionActionRequest;
import auction.common.model.network.AdminAuctionActionResponse;
import auction.common.model.network.AuctionView;
import auction.common.model.network.BidResponse;
import auction.common.model.network.CreateAuctionResponse;
import auction.common.model.network.GetAuctionListResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class AdminDashboardController implements Observer {
    @FXML
    private TableView<AuctionView> auctionTable;
    @FXML
    private TableColumn<AuctionView, String> itemNameColumn;
    @FXML
    private TableColumn<AuctionView, String> sellerColumn;
    @FXML
    private TableColumn<AuctionView, String> categoryColumn;
    @FXML
    private TableColumn<AuctionView, Double> currentPriceColumn;
    @FXML
    private TableColumn<AuctionView, String> statusColumn;
    @FXML
    private TableColumn<AuctionView, String> endTimeColumn;
    @FXML
    private Button startButton;
    @FXML
    private Button endButton;
    @FXML
    private Button cancelButton;

    private final ObservableList<AuctionView> auctions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("sellerUsername"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeDisplay"));

        auctionTable.setItems(auctions);
        auctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateActionButtons(newValue));
        updateActionButtons(null);

        try {
            AuctionClient.getInstance().connect("127.0.0.1", 8080);
            AuctionClient.getInstance().addObserver(this);
            AuctionClient.getInstance().requestAuctionList();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không kết nối được đến server: " + e.getMessage());
        }
    }

    @FXML
    private void handleStartAuction() {
        sendAdminAction("START");
    }

    @FXML
    private void handleEndAuction() {
        sendAdminAction("END");
    }

    @FXML
    private void handleCancelAuction() {
        sendAdminAction("CANCEL");
    }

    @FXML
    private void handleRefreshAuctions() {
        try {
            AuctionClient.getInstance().requestAuctionList();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không tải lại được danh sách phiên đấu giá.");
        }
    }

    @FXML
    private void handleOpenMarket() throws IOException {
        AuctionClient.getInstance().removeObserver(this);
        MainClient.changeScene("auction-list-view.fxml");
    }

    @Override
    public void onBidResponse(BidResponse response) {
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
        Platform.runLater(() -> {
            auctions.setAll(response.getAuctions());
            auctionTable.refresh();
            updateActionButtons(auctionTable.getSelectionModel().getSelectedItem());
        });
    }

    @Override
    public void onCreateAuctionResponse(CreateAuctionResponse response) {
    }

    @Override
    public void onAdminAuctionActionResponse(AdminAuctionActionResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Cập nhật thành công", response.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Cập nhật thất bại", response.getMessage());
            }
        });
    }

    private void sendAdminAction(String action) {
        AuctionView selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn phiên", "Vui lòng chọn một phiên đấu giá để thao tác.");
            return;
        }
        try {
            AuctionClient.getInstance().sendAdminAuctionActionRequest(
                    new AdminAuctionActionRequest(selected.getAuctionId(), action, ClientSession.getUsername())
            );
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không gửi được lệnh quản trị đến server.");
        }
    }

    private void updateActionButtons(AuctionView auction) {
        if (auction == null) {
            startButton.setDisable(true);
            endButton.setDisable(true);
            cancelButton.setDisable(true);
            return;
        }

        String status = auction.getStatus();
        startButton.setDisable(!"OPEN".equalsIgnoreCase(status));
        endButton.setDisable(!"RUNNING".equalsIgnoreCase(status));
        cancelButton.setDisable("CANCELED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
