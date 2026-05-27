package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.exception.InvalidBidException;
import auction.common.model.network.AuctionView;
import auction.common.model.network.BidRequest;
import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListResponse;

import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ListView;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;

public class AuctionListController implements Observer {
    private static final String FILTER_ALL = "Tất cả";
    private static final String SORT_DEFAULT = "Mặc định";
    private static final String SORT_PRICE_ASC = "Giá tăng dần";
    private static final String SORT_PRICE_DESC = "Giá giảm dần";
    private static final String SORT_END_TIME_ASC = "Sắp kết thúc";
    private static final String SORT_NAME_ASC = "Tên A-Z";

    @FXML
    private TableView<AuctionView> auctionTable;
    @FXML
    private TableColumn<AuctionView, String> nameColumn;
    @FXML
    private TableColumn<AuctionView, String> artistColumn;
    @FXML
    private TableColumn<AuctionView, String> statusColumn;
    @FXML
    private TableColumn<AuctionView, String> leaderColumn;
    @FXML
    private TableColumn<AuctionView, String> endTimeColumn;
    @FXML
    private TableColumn<AuctionView, String> timeRemainingColumn;
    @FXML
    private TableColumn<AuctionView, Double> priceColumn;
    @FXML
    private TextField bidAmountField;
    @FXML
    private ListView<String> bidHistoryListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private ComboBox<String> sortComboBox;

    private final Timeline refreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> auctionTable.refresh())
    );
    // Dữ liệu gốc lấy từ server. Search/filter/sort luôn chạy trên danh sách này
    // để tránh mất dữ liệu khi người dùng đổi điều kiện hiển thị.
    private final ObservableList<AuctionView> masterAuctionList = FXCollections.observableArrayList();
    private FilteredList<AuctionView> filteredAuctions;
    private SortedList<AuctionView> sortedAuctions;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("creatorName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        leaderColumn.setCellValueFactory(new PropertyValueFactory<>("leadingBidderDisplay"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTimeDisplay"));
        timeRemainingColumn.setCellValueFactory(new PropertyValueFactory<>("timeRemainingDisplay"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        configureSearchAndFilter();
        configureSort();
        bindAuctionTable();
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

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
        auctionTable.setRowFactory(table -> {
            TableRow<AuctionView> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openAuctionScreen(row.getItem());
                }
            });
            return row;
        });

        System.out.println("Đang tải danh sách vật phẩm từ Server...");
    }

    private void configureSearchAndFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                FILTER_ALL,
                "OPEN",
                "RUNNING",
                "FINISHED",
                "PAID",
                "CANCELED"
        ));
        statusFilterComboBox.setValue(FILTER_ALL);

        // FilteredList giữ vai trò lớp lọc trung gian giữa dữ liệu gốc và bảng hiển thị.
        filteredAuctions = new FilteredList<>(masterAuctionList, auction -> true);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
    }

    private void configureSort() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                SORT_DEFAULT,
                SORT_PRICE_ASC,
                SORT_PRICE_DESC,
                SORT_END_TIME_ASC,
                SORT_NAME_ASC
        ));
        sortComboBox.setValue(SORT_DEFAULT);
        sortComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applySort());
    }

    private void bindAuctionTable() {
        // SortedList nhận đầu vào từ FilteredList, sau đó mới bind vào TableView.
        // Luồng dữ liệu là: masterAuctionList -> filteredAuctions -> sortedAuctions -> auctionTable.
        sortedAuctions = new SortedList<>(filteredAuctions);
        auctionTable.setItems(sortedAuctions);
    }

    private void applyFilters() {
        String searchKeyword = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilterComboBox.getValue();

        filteredAuctions.setPredicate(auction -> {
            boolean matchesStatus = FILTER_ALL.equals(selectedStatus)
                    || selectedStatus == null
                    || selectedStatus.equalsIgnoreCase(auction.getStatus());

            if (searchKeyword.isBlank()) {
                return matchesStatus;
            }

            // Search đang hỗ trợ tìm nhanh trên các trường dễ nhìn thấy trên màn hình.
            boolean matchesSearch =
                    auction.getItemName().toLowerCase(Locale.ROOT).contains(searchKeyword)
                            || auction.getCreatorName().toLowerCase(Locale.ROOT).contains(searchKeyword)
                            || auction.getLeadingBidderDisplay().toLowerCase(Locale.ROOT).contains(searchKeyword)
                            || auction.getStatus().toLowerCase(Locale.ROOT).contains(searchKeyword)
                            || auction.getStatusDisplay().toLowerCase(Locale.ROOT).contains(searchKeyword);

            return matchesStatus && matchesSearch;
        });
        applySort();
    }

    private void applySort() {
        String sortOption = sortComboBox.getValue();
        Comparator<AuctionView> comparator = null;

        // Sort chỉ thay comparator của SortedList, không đụng vào dữ liệu gốc.
        if (SORT_PRICE_ASC.equals(sortOption)) {
            comparator = Comparator.comparingDouble(AuctionView::getCurrentPrice);
        } else if (SORT_PRICE_DESC.equals(sortOption)) {
            comparator = Comparator.comparingDouble(AuctionView::getCurrentPrice).reversed();
        } else if (SORT_END_TIME_ASC.equals(sortOption)) {
            comparator = Comparator.comparing(AuctionView::getEndTime);
        } else if (SORT_NAME_ASC.equals(sortOption)) {
            comparator = Comparator.comparing(AuctionView::getItemName, String.CASE_INSENSITIVE_ORDER);
        }

        if (comparator == null) {
            sortedAuctions.comparatorProperty().unbind();
            sortedAuctions.setComparator(null);
            auctionTable.sortPolicyProperty().set(table -> true);
            return;
        }

        sortedAuctions.comparatorProperty().unbind();
        sortedAuctions.setComparator(comparator);
    }

    @FXML
    private void handleBid() {
        AuctionView selectedAuction = auctionTable.getSelectionModel().getSelectedItem();
        if (selectedAuction == null) {
            showError("Vui lòng chọn một vật phẩm để đấu giá.");
            return;
        }
        try {
            double bidAmount = Double.parseDouble(bidAmountField.getText().trim());
            validateBid(selectedAuction, bidAmount);
            BidRequest request = new BidRequest(
                    selectedAuction.getItemId(),
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

    @FXML
    private void handleRefresh() {
        try {
            AuctionClient.getInstance().requestAuctionList();
        } catch (IOException e) {
            showError("Không tải lại được danh sách phiên đấu giá.");
        }
    }

    private void updateDetailView(AuctionView selectedAuction) {
        if (bidHistoryListView == null) {
            System.err.println("Lỗi: bidHistoryListView chưa được kết nối từ FXML!");
            return;
        }
        bidHistoryListView.getItems().clear();
        bidHistoryListView.getItems().add("        CHI TIẾT VẬT PHẨM");
        bidHistoryListView.getItems().add("Tên: " + selectedAuction.getItemName());
        bidHistoryListView.getItems().add("Người tạo: " + selectedAuction.getCreatorName());
        bidHistoryListView.getItems().add("Trạng thái: " + selectedAuction.getStatusDisplay());
        bidHistoryListView.getItems().add("Người dẫn đầu: " + selectedAuction.getLeadingBidderDisplay());
        bidHistoryListView.getItems().add("Kết thúc lúc: " + selectedAuction.getEndTimeDisplay());
        bidHistoryListView.getItems().add("Thời gian còn lại: " + selectedAuction.getTimeRemainingDisplay());
        bidHistoryListView.getItems().add("Giá khởi điểm: " + selectedAuction.getStartingPrice() + " USD");
        bidHistoryListView.getItems().add("Giá hiện tại: " + selectedAuction.getCurrentPrice() + " USD");
        bidHistoryListView.getItems().add("        LỊCH SỬ ĐẶT GIÁ");
        bidHistoryListView.getItems().addAll(selectedAuction.getBidHistoryDisplay());
    }

    private void validateBid(AuctionView selectedAuction, double bidAmount) throws InvalidBidException {
        if (bidAmount <= selectedAuction.getCurrentPrice()){
            throw new InvalidBidException(String.format(
                    "Giá đặt (%.2f) phải cao hơn giá hiện tại (%.2f) USD",
                    bidAmount,
                    selectedAuction.getCurrentPrice()
            ));
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

            AuctionView updatedAuction = response.getUpdatedAuction();
            if (updatedAuction == null) {
                return;
            }

            // Realtime update phải cập nhật vào danh sách gốc trước, sau đó filter/sort mới giữ được trạng thái hiện tại.
            for (int i = 0; i < masterAuctionList.size(); i++) {
                AuctionView current = masterAuctionList.get(i);
                if (current.getItemId().equals(updatedAuction.getItemId())) {
                    masterAuctionList.set(i, updatedAuction);
                    break;
                }
            }

            AuctionView selected = auctionTable.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getItemId().equals(updatedAuction.getItemId())) {
                updateDetailView(updatedAuction);
            }

            auctionTable.refresh();
            bidAmountField.clear();
        });
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
        Platform.runLater(() -> {
            // Khi server trả danh sách mới, thay toàn bộ dữ liệu gốc rồi áp lại filter hiện tại.
            masterAuctionList.setAll(response.getAuctions());
            applyFilters();
            auctionTable.refresh();
        });
    }

    private void openAuctionScreen(AuctionView auctionView) {
        if ("RUNNING".equalsIgnoreCase(auctionView.getStatus())) {
            openAuctionWindow(
                    auctionView,
                    "/auction/client/views/live-auction-view.fxml",
                    "Đấu giá trực tiếp",
                    LiveAuctionController.class
            );
            return;
        }

        openAuctionWindow(
                auctionView,
                "/auction/client/views/auction-detail-view.fxml",
                "Chi tiết phiên đấu giá",
                AuctionDetailController.class
        );
    }

    private <T> void openAuctionWindow(AuctionView auctionView, String fxmlPath, String title, Class<T> controllerType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AuctionDetailController detailController) {
                detailController.setAuctionView(auctionView);
            } else if (controller instanceof LiveAuctionController liveController) {
                liveController.setAuctionView(auctionView);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setOnHidden(event -> {
                if (controller instanceof AuctionDetailController detailController) {
                    detailController.cleanup();
                } else if (controller instanceof LiveAuctionController liveController) {
                    liveController.cleanup();
                }
            });
            stage.show();
        } catch (IOException e) {
            showError("Không mở được màn hình phiên đấu giá.");
        }
    }
}