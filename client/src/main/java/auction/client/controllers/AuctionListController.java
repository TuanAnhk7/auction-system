package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.exception.InvalidBidException;
import auction.common.model.item.Art;
import auction.common.model.item.Item;
import auction.common.model.network.BidRequest;
import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuctionListController implements Observer {

    @FXML
    private TableView<Art> auctionTable;

    @FXML
    private TableColumn<Art, String> nameColumn;

    @FXML
    private TableColumn<Art, String> artistColumn;

    @FXML
    private TableColumn<Art, Integer> yearColumn;

    @FXML
    private TableColumn<Art, Double> priceColumn;

    @FXML
    private TextField bidAmountField;

    @FXML
    public void initialize() {
        // Cau hinh map cot sang getter cua model Art.
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearCreated"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        auctionTable.setItems(FXCollections.observableArrayList());

        try {
            AuctionClient.getInstance().connect("127.0.0.1", 8080);
            AuctionClient.getInstance().addObserver(this);
            AuctionClient.getInstance().requestAuctionList();
        } catch (Exception e) {
            showError("Khong ket noi duoc den server realtime.");
        }

        System.out.println("Đang tải danh sách vật phẩm từ Server...");
    }

    @FXML
    private void handleBid() {
        Art selectedArt = auctionTable.getSelectionModel().getSelectedItem();
        if (selectedArt == null) {
            showError("Vui lòng chọn một vật phẩm để đấu giá.");
            return;
        }

        try {
            double bidAmount = Double.parseDouble(bidAmountField.getText().trim());
            validateBid(selectedArt, bidAmount);
            BidRequest request = new BidRequest(
                    selectedArt.getId(),
                    ClientSession.getUsername(),
                    bidAmount
            );
            AuctionClient.getInstance().sendBidRequest(request);
        } catch (NumberFormatException e) {
            showError("Giá đặt phải là một số hợp lệ.");
        } catch (java.io.IOException e) {
            showError("Khong gui duoc yeu cau dat gia den server.");
        } catch (InvalidBidException e) {
            showError(e.getMessage());
        }
    }

    private void validateBid(Art selectedArt, double bidAmount) throws InvalidBidException {
        if (bidAmount <= selectedArt.getCurrentPrice()) {
            throw new InvalidBidException("Giá đặt phải lớn hơn giá hiện tại.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi đấu giá");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void onBidResponse(BidResponse response) {
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showError(response.getMessage());
                return;
            }

            Item updatedItem = response.getUpdatedItem();
            if (!(updatedItem instanceof Art updatedArt)) {
                return;
            }

            for (Art art : auctionTable.getItems()) {
                if (art.getId().equals(updatedArt.getId())) {
                    art.updateCurrentPrice(updatedArt.getCurrentPrice());
                    break;
                }
            }

            auctionTable.refresh();
            bidAmountField.clear();
        });
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
        Platform.runLater(() -> {
            auctionTable.getItems().setAll(
                    response.getItems().stream()
                            .filter(Art.class::isInstance)
                            .map(Art.class::cast)
                            .toList()
            );
            auctionTable.refresh();
        });
    }
}
