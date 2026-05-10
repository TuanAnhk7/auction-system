package auction.client.controllers;

import auction.common.exception.InvalidBidException;
import auction.common.model.item.Art;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuctionListController {

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
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("yearCreated"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        auctionTable.setItems(FXCollections.observableArrayList(
                new Art("Mona Lisa", "Portrait by Leonardo da Vinci", 1000.0, "Leonardo da Vinci", 1503),
                new Art("The Starry Night", "Painting by Vincent van Gogh", 1200.0, "Vincent van Gogh", 1889)
        ));

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
            selectedArt.updateCurrentPrice(bidAmount);
            auctionTable.refresh();
            bidAmountField.clear();
            System.out.println("Đã gửi yêu cầu đấu giá cho " + selectedArt.getName() + ": " + bidAmount);
        } catch (NumberFormatException e) {
            showError("Giá đặt phải là một số hợp lệ.");
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
}
