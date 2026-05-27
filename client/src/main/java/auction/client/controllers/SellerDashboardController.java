package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.MainClient;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.model.auction.AuctionStatus;
import auction.common.model.network.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

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

    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnUpdateStatus;

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
        btnEdit.setDisable(true);
        btnDelete.setDisable(true);
        btnUpdateStatus.setDisable(true);

        myAuctionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                btnEdit.setDisable(false);
                btnDelete.setDisable(false);
                btnUpdateStatus.setDisable(false);
            } else {
                btnEdit.setDisable(true);
                btnDelete.setDisable(true);
                btnUpdateStatus.setDisable(true);
            }
        });

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

            AuctionClient.getInstance().sendCreateAuctionRequest(new CreateAuctionRequest(
                    ClientSession.getUsername(),
                    itemName,
                    startingPrice,
                    description,
                    itemType,
                    LocalDateTime.now(),
                    durationMinutes,
                    "",
                    0
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
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không tải lại được danh sách. Vui lòng kiểm tra kết nối và thử lại.");
        }
    }

    @FXML
    private void handleOpenMarket() throws IOException {
        AuctionClient.getInstance().removeObserver(this);
        MainClient.changeScene("auction-list-view.fxml");
    }

    @FXML
    private void handleEditAuction() {
        AuctionView selected = myAuctionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin vật phẩm");
        dialog.setHeaderText("Chỉnh sửa thông tin cho: " + selected.getItemName());

        ButtonType saveButtonType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField newNameField = new TextField();
        newNameField.setText(selected.getItemName());
        TextField newPriceField = new TextField();
        newPriceField.setText(String.valueOf(selected.getStartingPrice()));
        TextArea newDescriptionArea = new TextArea();
        newDescriptionArea.setText(selected.getDescription());

        grid.add(new Label("Tên mới:"), 0, 0);
        grid.add(newNameField, 1, 0);
        grid.add(new Label("Giá mới:"), 0, 1);
        grid.add(newPriceField, 1, 1);
        grid.add(new Label("Mô tả mới:"), 0, 2);
        grid.add(newDescriptionArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Pair<>(newNameField.getText(), newPriceField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            try {
                String newName = pair.getKey();
                double newPrice = Double.parseDouble(pair.getValue());
                String newDescription = newDescriptionArea.getText();

                UpdateItemRequest request = new UpdateItemRequest(selected.getAuctionId(), newName, newPrice, newDescription);
                AuctionClient.getInstance().sendUpdateItemRequest(request);

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá phải là một con số hợp lệ.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không gửi được yêu cầu cập nhật. Vui lòng kiểm tra kết nối tới server và thử lại.");
            }
        });
    }

    @FXML
    private void handleDeleteAuction() {
        AuctionView selected = myAuctionsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showAlert(Alert.AlertType.INFORMATION, "Chức năng", "Chức năng Xóa chưa được triển khai cho vật phẩm: " + selected.getItemName());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        AuctionView selected = myAuctionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        try {
            // Chuyển trạng thái sang RUNNING
            ChangeStatusRequest request = new ChangeStatusRequest(selected.getAuctionId(), AuctionStatus.RUNNING);
            AuctionClient.getInstance().sendChangeStatusRequest(request);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu bắt đầu phiên đấu giá.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi mạng", "Không gửi được yêu cầu cập nhật trạng thái.");
        }
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
                handleRefreshMyAuctions();
            } else {
                showAlert(Alert.AlertType.ERROR, "Tạo phiên thất bại", response.getMessage());
            }
        });
    }

    @Override
    public void onUpdateItemResponse(UpdateItemResponse response) {
        Platform.runLater(() -> {
            if (response.isSuccess()) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", response.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Cập nhật thất bại", response.getMessage());
            }
        });
    }

    private void clearCreateAuctionForm() {
        productNameField.clear();
        startingPriceField.clear();
        descriptionArea.clear();
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