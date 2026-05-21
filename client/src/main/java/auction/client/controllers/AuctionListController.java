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
import javafx.scene.control.ListView;
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
    private ListView<String> bidHistoryListView;

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
            e.printStackTrace(); // Thêm dòng này để xem lỗi chi tiết trong Console
            showError("Lỗi kết nối Server: " + e.getMessage());
        }

        auctionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection)  -> {
            if (newSelection != null){
                updateDetailView(newSelection);
            }
        });

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

    private void updateDetailView(Art selectedArt) {
        if (bidHistoryListView == null) {
            System.err.println("Lỗi: bidHistoryListView chưa được kết nối từ FXML!");
            return;
        }
        bidHistoryListView.getItems().clear();
        bidHistoryListView.getItems().add("        CHI TIẾT VẬT PHẨM");
        bidHistoryListView.getItems().add("Tên: " + selectedArt.getName());
        bidHistoryListView.getItems().add("Họa sĩ: " + selectedArt.getArtist());
        bidHistoryListView.getItems().add("Giá khởi điểm: " + selectedArt.getStartingPrice() + "USD");
        bidHistoryListView.getItems().add("Giá hiện tại: " + selectedArt.getCurrentPrice() + "USD");
        bidHistoryListView.getItems().add("        LỊCH SỬ ĐẶT GIÁ");

    }

    private void validateBid(Art selectedArt, double bidAmount) throws InvalidBidException {//ktra tinh hop le cua gia dat
        if (bidAmount <= selectedArt.getCurrentPrice()){
            throw new InvalidBidException(String.format("Giá đặt (%.2f) phải cao hơn giá hiện tại (%.2f) USD", bidAmount, selectedArt.getCurrentPrice()));
        }
    }

    private void showError(String message){
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
            if (!(updatedItem instanceof Art)) return;
            Art updatedArt = (Art) updatedItem;

            for (Art art : auctionTable.getItems()) {
                if (art.getId().equals(updatedArt.getId())) {
                    art.updateCurrentPrice(updatedArt.getCurrentPrice());
                    break;
                }
            }

            Art selected = auctionTable.getSelectionModel().getSelectedItem();//cập nhật lịch sử nếu vật phẩm được xem chi tiết
            if (selected != null && selected.getId().equals(updatedArt.getId())) {
                // Xác định người đặt giá dựa trên thông điệp từ server
                String bidderDisplay = "Người khác";
                if (response.getMessage().contains("thành công") || 
                    response.getMessage().contains(ClientSession.getUsername())) {
                    bidderDisplay = "Bạn";
                }
                String historyEntry = String.format("[%s] vừa đặt giá mới: %.2f USD",
                        bidderDisplay,
                        updatedArt.getCurrentPrice());
                bidHistoryListView.getItems().add(historyEntry);
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
