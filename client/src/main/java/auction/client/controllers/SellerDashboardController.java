package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.MainClient;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.model.network.AuctionView;
import auction.common.model.network.BidResponse;
import auction.common.model.network.CreateAuctionRequest;
import auction.common.model.network.CreateAuctionResponse;
import auction.common.model.network.GetAuctionListResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDateTime;

public class SellerDashboardController implements Observer {
    @FXML
    private TextField productNameField;
    @FXML
    private TextField startingPriceField;
    @FXML
    private TextField durationMinutesField;
    @FXML
    private ComboBox<String> itemTypeComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField specificProp1Field;
    @FXML
    private TextField specificProp2Field;
    @FXML
    private TableView<AuctionView> myAuctionsTable;
    @FXML
    private TableColumn<AuctionView, String> myProductColumn;
    @FXML
    private TableColumn<AuctionView, String> myTypeColumn;
    @FXML
    private TableColumn<AuctionView, Double> myPriceColumn;
    @FXML
    private TableColumn<AuctionView, String> myStatusColumn;
    @FXML
    private TableColumn<AuctionView, String> myEndTimeColumn;

    private final ObservableList<AuctionView> myAuctionSource = FXCollections.observableArrayList();
    private FilteredList<AuctionView> myAuctions;

    @FXML
    public void initialize() {
        itemTypeComboBox.setItems(FXCollections.observableArrayList("Art", "Antique"));
        itemTypeComboBox.setValue("Art");
        durationMinutesField.setText("60");

        myProductColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        myTypeColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        myPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        myStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        myEndTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeDisplay"));

        myAuctions = new FilteredList<>(myAuctionSource, auction -> true);
        myAuctionsTable.setItems(myAuctions);

        try {
            AuctionClient.getInstance().connect("127.0.0.1", 8080);
            AuctionClient.getInstance().addObserver(this);
            AuctionClient.getInstance().requestAuctionList();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không kết nối được đến server: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateAuction() {
        try {
            String itemName = productNameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String itemType = itemTypeComboBox.getValue();
            double startingPrice = Double.parseDouble(startingPriceField.getText().trim());
            long durationMinutes = Long.parseLong(durationMinutesField.getText().trim());
            String prop1 = specificProp1Field.getText().trim();
            int prop2 = Integer.parseInt(specificProp2Field.getText().trim());

            validateCreateAuctionForm(itemName, description, itemType, startingPrice, durationMinutes, prop1);

            AuctionClient.getInstance().sendCreateAuctionRequest(new CreateAuctionRequest(
                    ClientSession.getUsername(),
                    itemName,
                    startingPrice,
                    description,
                    itemType,
                    LocalDateTime.now(),
                    durationMinutes,
                    prop1,
                    prop2
            ));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", "Giá khởi điểm và thời lượng phải là số hợp lệ.");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Dữ liệu không hợp lệ", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không gửi được yêu cầu tạo phiên đấu giá.");
        }
    }

    @FXML
    private void handleRefreshMyAuctions() {
        try {
            AuctionClient.getInstance().requestAuctionList();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không tải lại được danh sách phiên của người bán.");
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
            myAuctionSource.setAll(response.getAuctions());
            myAuctions.setPredicate(auction -> ClientSession.getUsername().equalsIgnoreCase(auction.getSellerUsername()));
            myAuctionsTable.refresh();
        });
    }

    @Override
    public void onCreateAuctionResponse(CreateAuctionResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                clearCreateAuctionForm();
                showAlert(Alert.AlertType.INFORMATION, "Tạo phiên thành công", response.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Tạo phiên thất bại", response.getMessage());
            }
        });
    }

    private void validateCreateAuctionForm(
            String itemName,
            String description,
            String itemType,
            double startingPrice,
            long durationMinutes,
            String prop1
    ) {
        if (itemName.isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Mô tả không được để trống.");
        }
        if (itemType == null || itemType.isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại vật phẩm.");
        }
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Thời lượng đấu giá phải lớn hơn 0 phút.");
        }
        if (prop1.isBlank()) {
            throw new IllegalArgumentException("Thông tin đặc trưng (Họa sĩ/Nguồn gốc) không được để trống.");
        }
    }

    private void clearCreateAuctionForm() {
        productNameField.clear();
        startingPriceField.clear();
        descriptionArea.clear();
        specificProp1Field.clear();
        specificProp2Field.clear();
        itemTypeComboBox.setValue("Art");
        durationMinutesField.setText("60");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
