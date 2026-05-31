package auction.client.controllers;

import auction.client.ClientSession;
import auction.client.network.AuctionClient;
import auction.client.network.Observer;
import auction.common.exception.AuctionException;
import auction.common.model.network.BalanceUpdateRequest;
import auction.common.model.network.BalanceUpdateResponse;
import auction.common.model.network.BidResponse;
import auction.common.model.network.GetAuctionListResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class ProfilePopupController implements Observer {
    @FXML
    private Label statusLabel;
    @FXML
    private Label usernameValueLabel;
    @FXML
    private Label balanceValueLabel;
    @FXML
    private TextField depositAmountField;

    private double currentBalance;

    @FXML
    public void initialize() {
        AuctionClient.getInstance().addObserver(this);
    }

    public void setProfile(String username, double balance) {
        this.currentBalance = balance;
        usernameValueLabel.setText(username);
        updateBalanceLabel();
    }

    public void cleanup() {
        AuctionClient.getInstance().removeObserver(this);
    }

    @FXML
    private void handleDeposit() {
        try {
            double amount = parseAmount(depositAmountField.getText());
            if (amount <= 0) {
                throw new AuctionException("Số tiền nạp phải lớn hơn 0.");
            }

            setStatus("Đang gửi yêu cầu nạp tiền...", false);
            AuctionClient.getInstance().sendBalanceUpdateRequest(new BalanceUpdateRequest(amount));
        } catch (NumberFormatException e) {
            setStatus("Vui lòng nhập số tiền hợp lệ.", false);
        } catch (AuctionException e) {
            setStatus(e.getMessage(), false);
        } catch (IOException e) {
            setStatus("Không gửi được yêu cầu nạp tiền: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) depositAmountField.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onBidResponse(BidResponse response) {
    }

    @Override
    public void onAuctionListResponse(GetAuctionListResponse response) {
    }

    @Override
    public void onBalanceUpdateResponse(BalanceUpdateResponse response) {
        if (response == null) {
            return;
        }

        Platform.runLater(() -> {
            if (response.isSuccess()) {
                currentBalance = response.getBalance();
                ClientSession.setBalance(currentBalance);
                updateBalanceLabel();
                depositAmountField.clear();
                setStatus(response.getMessage(), true);
            } else {
                setStatus(response.getMessage(), false);
            }
        });
    }

    private void updateBalanceLabel() {
        balanceValueLabel.setText(String.format(Locale.US, "%.2f USD", currentBalance));
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
                ? "-fx-text-fill: #166534;"
                : "-fx-text-fill: #b91c1c;");
    }

    private double parseAmount(String value) {
        if (value == null) {
            throw new NumberFormatException("empty");
        }
        return Double.parseDouble(value.trim().replace(',', '.'));
    }
}
