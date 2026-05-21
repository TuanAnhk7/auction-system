package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.exception.InvalidBidException;
import auction.common.model.network.AdminAuctionActionResponse;
import auction.common.model.network.AuctionView;
import auction.common.model.network.BidRequest;
import auction.common.model.network.BidResponse;
import auction.common.model.network.CreateAuctionResponse;
import auction.common.model.network.GetAuctionListResponse;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class LiveAuctionController implements Observer {
    @FXML
    private Label lblItemName;
    @FXML
    private Label lblCurrentPrice;
    @FXML
    private Label lblCurrentWinner;
    @FXML
    private Label lblCountdown;
    @FXML
    private Label lblStatusMessage;
    @FXML
    private TextField txtBidAmount;
    @FXML
    private Button btnPlaceBid;
    @FXML
    private Button btnQuick50;
    @FXML
    private Button btnQuick100;
    @FXML
    private Button btnQuick500;
    @FXML
    private ListView<String> liveBidStream;

    private AuctionView auctionView;
    private Timeline countdownTimeline;
    private String previousStatus;
    private boolean endPopupShown;
    private boolean finalResultAddedToStream;

    @FXML
    public void initialize() {
        AuctionClient.getInstance().addObserver(this);
        configureLiveBidStream();
    }

    public void setAuctionView(AuctionView auctionView) {
        this.auctionView = auctionView;
        this.previousStatus = auctionView.getStatus();
        this.finalResultAddedToStream = false;
        refreshView();
        startCountdown();
    }

    @FXML
    private void handlePlaceBid() {
        if (auctionView == null) {
            return;
        }
        try {
            double bidAmount = parseBidAmount(txtBidAmount.getText());
            validateBid(bidAmount);
            AuctionClient.getInstance().sendBidRequest(new BidRequest(
                    auctionView.getItemId(),
                    ClientSession.getUsername(),
                    bidAmount
            ));
        } catch (NumberFormatException e) {
            showError("Giá đặt phải là một số hợp lệ.");
        } catch (InvalidBidException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Không gửi được yêu cầu đặt giá đến server.");
        }
    }

    @FXML
    private void handleQuickBid50() {
        applyQuickBid(50.0);
    }

    @FXML
    private void handleQuickBid100() {
        applyQuickBid(100.0);
    }

    @FXML
    private void handleQuickBid500() {
        applyQuickBid(500.0);
    }

    @FXML
    private void handleClose() {
        cleanup();
        Stage stage = (Stage) lblItemName.getScene().getWindow();
        stage.close();
    }

    public void cleanup() {
        stopCountdown();
        AuctionClient.getInstance().removeObserver(this);
    }

    private void configureLiveBidStream() {
        // Tô màu riêng cho các dòng kết quả cuối phiên để người dùng nhìn thấy ngay trạng thái chốt.
        liveBidStream.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);
                if (item.contains("Ket qua cuoi phien")) {
                    setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-weight: bold;");
                } else if (item.contains("Phien dau gia da bi huy")) {
                    setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    @Override
    public void onBidResponse(BidResponse response) {
        Platform.runLater(() -> {
            if (auctionView == null) {
                return;
            }
            if (!response.isSuccess()) {
                showError(response.getMessage());
                return;
            }
            AuctionView updatedAuction = response.getUpdatedAuction();
            if (updatedAuction == null || !auctionView.getItemId().equals(updatedAuction.getItemId())) {
                return;
            }
            // Bắt chuyển trạng thái để hiện popup đúng ngữ cảnh.
            handleStatusTransition(auctionView, updatedAuction, false);
            auctionView = updatedAuction;
            refreshView();

            String bidder = response.getBidderUsername() == null ? "Ẩn danh" : response.getBidderUsername();
            String line = String.format("[%s] %s vừa đặt %.2f USD",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                    bidder,
                    updatedAuction.getCurrentPrice());
            liveBidStream.getItems().add(0, line);
            txtBidAmount.clear();
        });
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
        Platform.runLater(() -> response.getAuctions().stream()
                .filter(auction -> auctionView != null && auctionView.getItemId().equals(auction.getItemId()))
                .findFirst()
                .ifPresent(updatedAuction -> {
                    handleStatusTransition(auctionView, updatedAuction, wasForcedFinishByAdmin(auctionView, updatedAuction));
                    auctionView = updatedAuction;
                    refreshView();
                }));
    }

    @Override
    public void onCreateAuctionResponse(CreateAuctionResponse response) {
    }

    @Override
    public void onAdminAuctionActionResponse(AdminAuctionActionResponse response) {
        if (response.isSuccess() && response.getUpdatedAuction() != null && auctionView != null
                && auctionView.getItemId().equals(response.getUpdatedAuction().getItemId())) {
            Platform.runLater(() -> {
                handleStatusTransition(auctionView, response.getUpdatedAuction(), true);
                auctionView = response.getUpdatedAuction();
                refreshView();
            });
        }
    }

    private void refreshView() {
        lblItemName.setText(auctionView.getItemName());
        lblCurrentPrice.setText(String.format("%.2f USD", auctionView.getCurrentPrice()));
        lblCurrentWinner.setText(auctionView.getLeadingBidderDisplay());
        liveBidStream.getItems().setAll(auctionView.getBidHistoryDisplay());
        updateCountdown();
        updateInteractiveState();
    }

    private void updateInteractiveState() {
        boolean running = "RUNNING".equalsIgnoreCase(auctionView.getStatus());
        boolean expired = auctionView.getEndTime() == null || !LocalDateTime.now().isBefore(auctionView.getEndTime());
        boolean disabled = !running || expired;

        btnPlaceBid.setDisable(disabled);
        btnQuick50.setDisable(disabled);
        btnQuick100.setDisable(disabled);
        btnQuick500.setDisable(disabled);
        txtBidAmount.setDisable(disabled);

        if (disabled) {
            lblStatusMessage.setText("Phiên đã đóng");
        } else {
            lblStatusMessage.setText("Phiên đang mở để đặt giá");
        }
    }

    private void startCountdown() {
        stopCountdown();
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            updateCountdown();
            updateInteractiveState();
            if (auctionView != null && !LocalDateTime.now().isBefore(auctionView.getEndTime()) && !endPopupShown) {
                endPopupShown = true;
                appendFinalResultToStream(auctionView);
                showInfo(
                        "Phiên kết thúc",
                        buildFinishedAuctionMessage(
                                "Phiên đấu giá đã kết thúc do hết thời gian.",
                                auctionView
                        )
                );
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void stopCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
    }

    private void updateCountdown() {
        if (auctionView == null || auctionView.getEndTime() == null) {
            lblCountdown.setText("--:--");
            return;
        }
        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), auctionView.getEndTime());
        if (remainingSeconds <= 0 || !"RUNNING".equalsIgnoreCase(auctionView.getStatus())) {
            lblCountdown.setText("00:00");
            return;
        }

        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        lblCountdown.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void applyQuickBid(double increment) {
        if (auctionView == null) {
            return;
        }
        // Nếu người dùng đã bấm nhiều nút nhanh liên tiếp thì cộng dồn trên giá đang nhập.
        // Nếu ô đang trống hoặc không hợp lệ thì quay về lấy từ giá hiện tại của phiên.
        double baseAmount = auctionView.getCurrentPrice();
        String currentText = txtBidAmount.getText() == null ? "" : txtBidAmount.getText().trim();
        if (!currentText.isEmpty()) {
            try {
                baseAmount = parseBidAmount(currentText);
            } catch (NumberFormatException ignored) {
                baseAmount = auctionView.getCurrentPrice();
            }
        }

        double nextBid = baseAmount + increment;
        txtBidAmount.setText(String.format(Locale.US, "%.2f", nextBid));
    }

    // Phân biệt rõ kết thúc do admin hay do hết giờ để popup dễ hiểu hơn.
    private void handleStatusTransition(AuctionView oldAuction, AuctionView newAuction, boolean fromAdminAction) {
        if (oldAuction == null || newAuction == null) {
            return;
        }

        String oldStatus = oldAuction.getStatus();
        String newStatus = newAuction.getStatus();
        if (oldStatus.equalsIgnoreCase(newStatus) || newStatus.equalsIgnoreCase(previousStatus)) {
            return;
        }

        previousStatus = newStatus;
        if ("CANCELED".equalsIgnoreCase(newStatus)) {
            endPopupShown = true;
            appendCancellationToStream();
            showInfo("Phiên bị hủy", "Phiên đấu giá đã bị quản trị viên hủy. Mọi thao tác đặt giá đã bị khóa.");
        } else if ("FINISHED".equalsIgnoreCase(newStatus) && fromAdminAction) {
            endPopupShown = true;
            appendFinalResultToStream(newAuction);
            showInfo(
                    "Phiên kết thúc sớm",
                    buildFinishedAuctionMessage(
                            "Phiên đấu giá đã được quản trị viên kết thúc.",
                            newAuction
                    )
            );
        } else if ("RUNNING".equalsIgnoreCase(newStatus) && "OPEN".equalsIgnoreCase(oldStatus)) {
            showInfo("Phiên bắt đầu", "Phiên đấu giá đã được mở. Bạn có thể bắt đầu đặt giá.");
        }
    }

    // Nếu phiên chuyển RUNNING -> FINISHED trước thời điểm endTime cũ thì gần như chắc là admin đã kết thúc sớm.
    private boolean wasForcedFinishByAdmin(AuctionView oldAuction, AuctionView newAuction) {
        if (oldAuction == null || newAuction == null || oldAuction.getEndTime() == null) {
            return false;
        }

        return "RUNNING".equalsIgnoreCase(oldAuction.getStatus())
                && "FINISHED".equalsIgnoreCase(newAuction.getStatus())
                && LocalDateTime.now().isBefore(oldAuction.getEndTime());
    }

    private void validateBid(double bidAmount) throws InvalidBidException {
        if (!"RUNNING".equalsIgnoreCase(auctionView.getStatus())) {
            throw new InvalidBidException("Phiên đấu giá không còn mở để đặt giá.");
        }
        if (bidAmount <= auctionView.getCurrentPrice()) {
            throw new InvalidBidException(String.format(
                    "Giá đặt (%.2f) phải cao hơn giá hiện tại (%.2f) USD",
                    bidAmount,
                    auctionView.getCurrentPrice()
            ));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi đấu giá");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Chấp nhận cả 1234.50 và 1234,50 để tránh lỗi theo locale của máy.
    private double parseBidAmount(String rawValue) {
        return Double.parseDouble(rawValue.trim().replace(',', '.'));
    }

    // Khi phiên kết thúc, popup cần công bố thẳng người thắng và giá chốt.
    private String buildFinishedAuctionMessage(String prefix, AuctionView auction) {
        String winner = auction.getLeadingBidderDisplay();
        String finalPrice = String.format("%.2f USD", auction.getCurrentPrice());

        if ("Chưa có".equalsIgnoreCase(winner)) {
            return prefix + "\nChưa có người thắng vì chưa có lượt đặt giá hợp lệ.";
        }

        return prefix
                + "\nNgười thắng cuộc: " + winner
                + "\nGiá chốt: " + finalPrice;
    }

    // Ghi kết quả cuối phiên vào live stream để người dùng xem lại lịch sử ngay trên màn hình.
    private void appendFinalResultToStream(AuctionView auction) {
        if (finalResultAddedToStream) {
            return;
        }
        finalResultAddedToStream = true;

        String winner = auction.getLeadingBidderDisplay();
        String finalPrice = String.format("%.2f USD", auction.getCurrentPrice());
        String time = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        if ("Chưa có".equalsIgnoreCase(winner)) {
            liveBidStream.getItems().add(0, "[" + time + "] Ket qua cuoi phien: chua co nguoi thang.");
            return;
        }

        liveBidStream.getItems().add(0,
                String.format("[%s] Ket qua cuoi phien: %s thang voi gia %s", time, winner, finalPrice));
    }

    private void appendCancellationToStream() {
        if (finalResultAddedToStream) {
            return;
        }
        finalResultAddedToStream = true;
        String time = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        liveBidStream.getItems().add(0, "[" + time + "] Phien dau gia da bi huy boi quan tri vien.");
    }
}
