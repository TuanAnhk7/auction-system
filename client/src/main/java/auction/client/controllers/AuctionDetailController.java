package auction.client.controllers;

import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.model.network.AdminAuctionActionResponse;
import auction.common.model.network.AuctionView;
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
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AuctionDetailController implements Observer {
    @FXML
    private Label itemNameLabel;
    @FXML
    private Label currentPriceLabel;
    @FXML
    private Label creatorLabel;
    @FXML
    private Label highestBidderLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label countdownLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ListView<String> bidHistoryListView;
    @FXML
    private Button closeButton;

    private AuctionView auctionView;
    private Timeline countdownTimeline;
    private String previousStatus;

    @FXML
    public void initialize() {
        AuctionClient.getInstance().addObserver(this);
    }

    public void setAuctionView(AuctionView auctionView) {
        this.auctionView = auctionView;
        this.previousStatus = auctionView.getStatus();
        refreshView();
        startCountdown();
    }

    @FXML
    private void handleClose() {
        cleanup();
        Stage stage = (Stage) itemNameLabel.getScene().getWindow();
        stage.close();
    }

    public void cleanup() {
        stopCountdown();
        AuctionClient.getInstance().removeObserver(this);
    }

    @Override
    public void onBidResponse(BidResponse response) {
        Platform.runLater(() -> {
            if (auctionView == null || !response.isSuccess() || response.getUpdatedAuction() == null) {
                if (!response.isSuccess()) {
                    showError(response.getMessage());
                }
                return;
            }
            AuctionView updatedAuction = response.getUpdatedAuction();
            if (!auctionView.getItemId().equals(updatedAuction.getItemId())) {
                return;
            }
            handleStatusTransition(auctionView, updatedAuction);
            auctionView = updatedAuction;
            refreshView();
        });
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
        Platform.runLater(() -> response.getAuctions().stream()
                .filter(auction -> auctionView != null && auctionView.getItemId().equals(auction.getItemId()))
                .findFirst()
                .ifPresent(updatedAuction -> {
                    handleStatusTransition(auctionView, updatedAuction);
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
                handleStatusTransition(auctionView, response.getUpdatedAuction());
                auctionView = response.getUpdatedAuction();
                refreshView();
            });
        }
    }

    private void refreshView() {
        itemNameLabel.setText(auctionView.getItemName());
        currentPriceLabel.setText(String.format("%.2f USD", auctionView.getCurrentPrice()));
        creatorLabel.setText(auctionView.getCreatorName());
        highestBidderLabel.setText(auctionView.getHighestBidderUsername() == null
                ? "Chưa có"
                : auctionView.getHighestBidderUsername());
        statusLabel.setText(auctionView.getStatusDisplay());
        descriptionArea.setText(auctionView.getDescription());
        bidHistoryListView.getItems().setAll(auctionView.getBidHistoryDisplay());
        updateCountdown();
    }

    private void startCountdown() {
        stopCountdown();
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateCountdown()));
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
        if (auctionView == null) {
            countdownLabel.setText("--:--:--");
            return;
        }
        long remainingSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), auctionView.getEndTime());
        if (remainingSeconds <= 0) {
            countdownLabel.setText("Đã kết thúc");
            return;
        }
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;
        countdownLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    // Theo dõi đổi trạng thái để hiện thông báo rõ nguyên nhân cho người dùng.
    private void handleStatusTransition(AuctionView oldAuction, AuctionView newAuction) {
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
            showInfo("Phiên bị hủy", "Phiên đấu giá đã bị quản trị viên hủy.");
        } else if ("FINISHED".equalsIgnoreCase(newStatus)) {
            showInfo("Phiên kết thúc", "Phiên đấu giá đã kết thúc. Người dẫn đầu hiện tại: " + newAuction.getLeadingBidderDisplay());
        } else if ("RUNNING".equalsIgnoreCase(newStatus) && "OPEN".equalsIgnoreCase(oldStatus)) {
            showInfo("Phiên bắt đầu", "Phiên đấu giá đã được mở và người dùng có thể theo dõi kết quả.");
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
}
