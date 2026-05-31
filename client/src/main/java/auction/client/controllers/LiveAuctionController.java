package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.client.view.AuctionHistoryChart;
import auction.common.exception.InvalidBidException;
import auction.common.model.network.*;
import auction.common.model.user.Bidder;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.layout.VBox;

public class LiveAuctionController implements Observer {
    private static final Pattern BID_HISTORY_PATTERN =
            Pattern.compile("^\\[(.+?)]\\s+(.+?)\\s+đặt\\s+([0-9]+(?:[.,][0-9]+)?)\\s+USD$");

    @FXML private Label lblBalance;
    @FXML private Label lblItemName;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblCurrentWinner;
    @FXML private Label lblCountdown;
    @FXML private Label lblStatusMessage;
    @FXML private TextField txtBidAmount;
    @FXML private Button btnPlaceBid;
    @FXML private Button btnQuick50;
    @FXML private Button btnQuick100;
    @FXML private Button btnQuick500;
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;
    @FXML private Button btnRegisterAutoBid;
    @FXML private ListView<String> liveBidStream;
    @FXML private VBox chartContainer;

    private AuctionView auctionView;
    private Timeline countdownTimeline;
    private String previousStatus;
    private boolean endPopupShown;
    private boolean finalResultAddedToStream;
    private AuctionHistoryChart chart;
    private List<Bidder.Bid> bids = new ArrayList<>();
    private int displayedBidHistoryCount;

    public void setAuctionHistoryChart(AuctionHistoryChart chart) {
        this.chart = chart;
    }

    public void onNewBid(Bidder.Bid bid) {
        bids.add(bid);
        if (chart != null) {
            chart.updateChart(bids);
        }
    }

    @FXML
    public void initialize() {
        System.out.println("LIVE AUCTION INITIALIZE RUNNING");
        System.out.println("chartContainer = " + chartContainer);

        AuctionClient.getInstance().addObserver(this);

        chart = new AuctionHistoryChart();
        chart.setPrefHeight(250);

        if (chartContainer != null) {
            chartContainer.getChildren().setAll(chart);
            System.out.println("chartContainer children = "
                    + chartContainer.getChildren().size());
        } else {
            System.err.println("chartContainer is null; live auction chart will not be shown.");
        }

    }

    public void setAuctionView(AuctionView auctionView) {
        this.auctionView = auctionView;
        this.previousStatus = auctionView.getStatus();
        this.finalResultAddedToStream = false;
        this.displayedBidHistoryCount = 0;

        refreshAuctionHistoryChart();
        refreshView();
        syncLiveBidStreamWithHistory(auctionView, true);
        updateBalanceDisplay();
        startCountdown();
    }

    private void loadBidHistoryToChart() {
    }

    public void cleanup() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        AuctionClient.getInstance().removeObserver(this);
    }

    @FXML
    private void handleClose() {
        cleanup();
        if (lblItemName != null && lblItemName.getScene() != null) {
            Stage stage = (Stage) lblItemName.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (auctionView == null) return;
        try {
            double bidAmount = parseDouble(txtBidAmount.getText());
            validateBid(bidAmount);

            // Gọi static trực tiếp từ ClientSession chuẩn 100%
            BidRequest request = new BidRequest(
                    String.valueOf(auctionView.getAuctionId()),
                    ClientSession.getUsername(),
                    bidAmount
            );

            AuctionClient.getInstance().sendBidRequest(request);
            txtBidAmount.clear();
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số tiền hợp lệ!");
        } catch (InvalidBidException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Lỗi kết nối mạng khi gửi lượt đặt giá: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegisterAutoBid() {
        if (auctionView == null) return;
        try {
            double maxBid = parseDouble(txtMaxBid.getText());
            double increment = parseDouble(txtIncrement.getText());

            if (maxBid <= auctionView.getCurrentPrice()) {
                throw new InvalidBidException("Giá tối đa (Max Bid) phải lớn hơn giá hiện tại!");
            }
            if (increment <= 0) {
                throw new InvalidBidException("Bước giá (Increment) phải lớn hơn 0!");
            }

            // Đóng gói thông tin thầu tự động gọi Static từ ClientSession
            AutoBidRequest request = new AutoBidRequest(
                    String.valueOf(auctionView.getAuctionId()),
                    ClientSession.getUsername(),
                    maxBid,
                    increment
            );

            AuctionClient.getInstance().sendAutoBidRequest(request);
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập định dạng số cho Max Bid và Increment!");
        } catch (InvalidBidException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Lỗi kết nối mạng khi cài đặt Auto-Bid: " + e.getMessage());
        }
    }

    @FXML private void handleQuickBid50() {  bidQuickly(50);  }
    @FXML private void handleQuickBid100() { bidQuickly(100); }
    @FXML private void handleQuickBid500() { bidQuickly(500); }

    private void bidQuickly(double extraAmount) {
        if (auctionView == null) return;
        try {
            double currentPrice = auctionView.getCurrentPrice();
            double newBid = currentPrice + extraAmount;
            validateBid(newBid);
            
            BidRequest request = new BidRequest(
                    String.valueOf(auctionView.getAuctionId()),
                    ClientSession.getUsername(),
                    newBid
            );
            AuctionClient.getInstance().sendBidRequest(request);
        } catch (InvalidBidException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Lỗi kết nối mạng: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi không xác định: " + e.getMessage());
        }
    }

    @Override
    public void onBidResponse(BidResponse response) {
        if (response == null) return;
        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showError(response.getMessage());
                return;
            }

            if (response.isSuccess() && response.getUpdatedAuction() != null
                    && auctionView != null
                    && auctionView.getAuctionId().equals(response.getUpdatedAuction().getAuctionId())) {
                this.auctionView = response.getUpdatedAuction();
                refreshAuctionHistoryChart();
                refreshView();
                syncLiveBidStreamWithHistory(response.getUpdatedAuction(), false);
                checkStatusTransition();
            }
            if (response.getBidderUsername() != null
                    && response.getBidderUsername().equals(ClientSession.getUsername())) {
                ClientSession.setBalance(response.getBalance());
                updateBalanceDisplay();
            }
        });
    }

    @Override
    public void onAutoBidResponse(AutoBidResponse response) {
        if (response == null) {
            return;
        }

        Platform.runLater(() -> {
            if (!response.isSuccess()) {
                showError(response.getMessage());
                return;
            }

            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            liveBidStream.getItems().add(0, String.format("[%s] [HỆ THỐNG] %s", time, response.getMessage()));
        });
    }

    @Override
    public void onAuctionExtendedResponse(AuctionExtendedResponse response) {
        if (response == null || !response.isSuccess() || response.getUpdatedAuction() == null || auctionView == null) {
            return;
        }

        if (!auctionView.getAuctionId().equals(response.getAuctionId())) {
            return;
        }

        Platform.runLater(() -> {
            this.auctionView = response.getUpdatedAuction();
            refreshAuctionHistoryChart();
            refreshView();
            syncLiveBidStreamWithHistory(response.getUpdatedAuction(), false);
            updateCountdown();
        });
    }

    @Override
    public void onBalanceUpdateResponse(BalanceUpdateResponse response) {
        if (response == null || !response.isSuccess()) {
            return;
        }

        Platform.runLater(() -> {
            ClientSession.setBalance(response.getBalance());
            updateBalanceDisplay();
        });
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
        if (response == null || auctionView == null) return;
        Platform.runLater(() -> {
            response.getAuctions().stream()
                    .filter(a -> a.getAuctionId().equals(auctionView.getAuctionId()))
                    .findFirst()
                    .ifPresent(updated -> {
                        this.auctionView = updated;
                        refreshAuctionHistoryChart();
                        refreshView();
                        syncLiveBidStreamWithHistory(updated, false);
                        checkStatusTransition();
                    });
        });
    }

    private void updateBalanceDisplay() {
        if (lblBalance != null) {
            lblBalance.setText(String.format(Locale.US, "Số dư của bạn: %.2f USD", ClientSession.getBalance()));
        }
    }

    private void refreshAuctionHistoryChart() {
        if (auctionView == null) {
            return;
        }

        bids = convertHistoryToBids(auctionView.getBidHistory());
        if (chart != null) {
            chart.updateChart(bids);
        }
    }

    private void refreshView() {
        if (auctionView == null) return;
        lblItemName.setText(auctionView.getItemName());
        lblCurrentPrice.setText(String.format(Locale.US, "%.2f USD", auctionView.getCurrentPrice()));
        lblCurrentWinner.setText(auctionView.getLeadingBidderDisplay());
        lblStatusMessage.setText("Trạng thái: " + auctionView.getStatusDisplay());
        if (txtBidAmount != null && txtBidAmount.getText().isEmpty()) {
            txtBidAmount.setText(String.format(Locale.US, "%.2f", auctionView.getCurrentPrice() + 1.0));
        }
        updateCountdown();
    }

    private void updateCountdown() {
        if (auctionView == null || lblCountdown == null) {
            return;
        }

        if (!"RUNNING".equalsIgnoreCase(auctionView.getStatus())) {
            lblCountdown.setText("00:00:00");
            return;
        }

        long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), auctionView.getEndTime());
        if (secondsLeft <= 0) {
            lblCountdown.setText("Thời gian đã hết!");
            if (countdownTimeline != null) {
                countdownTimeline.stop();
            }
            return;
        }

        long hours = secondsLeft / 3600;
        long minutes = (secondsLeft % 3600) / 60;
        long seconds = secondsLeft % 60;
        lblCountdown.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void checkStatusTransition() {
        String currentStatus = auctionView.getStatus();
        if ("FINISHED".equalsIgnoreCase(currentStatus) && !"FINISHED".equalsIgnoreCase(previousStatus)) {
            appendFinalResultToStream(auctionView);
            if (!endPopupShown) {
                endPopupShown = true;
                showInfo("Kết thúc", buildFinishedAuctionMessage("Phiên đấu giá đã khép lại!", auctionView));
            }
        } else if ("CANCELLED".equalsIgnoreCase(currentStatus) && !"CANCELLED".equalsIgnoreCase(previousStatus)) {
            appendCancellationToStream();
            showError("Phiên đấu giá này đã bị hủy bỏ bởi quản trị viên.");
        }
        this.previousStatus = currentStatus;
    }

    private void startCountdown() {
        if (countdownTimeline != null) countdownTimeline.stop();
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateCountdown()));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void configureLiveBidStream() {
        liveBidStream.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
    }

    private void validateBid(double amount) throws InvalidBidException {
        if (!"RUNNING".equalsIgnoreCase(auctionView.getStatus())) throw new InvalidBidException("Phiên không còn mở.");
        if (amount <= auctionView.getCurrentPrice()) {
            throw new InvalidBidException(String.format("Giá đặt (%.2f) phải cao hơn giá hiện tại (%.2f)", amount, auctionView.getCurrentPrice()));
        }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Lỗi");
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }

    private void showInfo(String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }

    private double parseDouble(String val) {
        return Double.parseDouble(val.trim().replace(',', '.'));
    }

    private String buildFinishedAuctionMessage(String pref, AuctionView a) {
        String w = a.getLeadingBidderDisplay();
        return pref + ("Chưa có".equalsIgnoreCase(w) ? "\nChưa có người thắng." : "\nNgười thắng: " + w + "\nGiá chốt: " + String.format("%.2f USD", a.getCurrentPrice()));
    }

    private void appendFinalResultToStream(AuctionView a) {
        if (finalResultAddedToStream) return;
        finalResultAddedToStream = true;
        String w = a.getLeadingBidderDisplay();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        liveBidStream.getItems().add(0, String.format("[%s] Kết quả cuối phiên: %s", time, "Chưa có".equalsIgnoreCase(w) ? "chưa có người thắng." : w + " thắng với giá " + String.format("%.2f USD", a.getCurrentPrice())));
    }

    private void syncLiveBidStreamWithHistory(AuctionView updatedAuction, boolean replaceAll) {
        if (updatedAuction == null || liveBidStream == null) {
            return;
        }

        List<String> history = updatedAuction.getBidHistoryDisplay();
        if (replaceAll || displayedBidHistoryCount > history.size()) {
            liveBidStream.getItems().clear();
            for (int i = history.size() - 1; i >= 0; i--) {
                liveBidStream.getItems().add(history.get(i));
            }
            displayedBidHistoryCount = history.size();
            return;
        }

        for (int i = displayedBidHistoryCount; i < history.size(); i++) {
            liveBidStream.getItems().add(0, history.get(i));
        }
        displayedBidHistoryCount = history.size();
    }

    private void appendCancellationToStream() {
        if (finalResultAddedToStream) return;
        finalResultAddedToStream = true;
        liveBidStream.getItems().add(0, "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Phiên đấu giá đã bị hủy bởi quản trị viên.");
    }

    private List<Bidder.Bid> convertHistoryToBids(List<String> history) {
        List<Bidder.Bid> result = new ArrayList<>();
        if (history == null) {
            return result;
        }

        for (String line : history) {
            try {
                if (line == null || line.isBlank()) {
                    continue;
                }

                Matcher matcher = BID_HISTORY_PATTERN.matcher(line.trim());
                if (!matcher.matches()) {
                    continue;
                }

                LocalDateTime time = parseHistoryTime(matcher.group(1).trim());
                String username = matcher.group(2).trim();
                double amount = Double.parseDouble(matcher.group(3).replace(',', '.'));
                result.add(new Bidder.Bid(username, amount, time));
            } catch (Exception e) {
                System.err.println("Bỏ qua dòng lịch sử bid không hợp lệ: " + line);
            }
        }
        return result;
    }

    private LocalDateTime parseHistoryTime(String rawTime) {
        try {
            return LocalDateTime.parse(rawTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.ofInstant(Instant.parse(rawTime), ZoneId.systemDefault());
        }
    }
}
