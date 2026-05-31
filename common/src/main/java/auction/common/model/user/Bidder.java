package auction.common.model.user;

import auction.common.model.item.Item;
import auction.common.model.network.Role;
import auction.common.model.user.User;
import java.time.LocalDateTime;

import java.time.Instant;

public class Bidder extends User {
    private double accountBalance;

    public Bidder(String username, String hashedPassword, String fullName, double accountBalance) {
        super(username, hashedPassword, fullName, Role.BIDDER);
        this.accountBalance = accountBalance;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
        touch();
    }

    public boolean placeBid(Item item, double bidAmount) {
        if (item == null || bidAmount <= 0) {
            return false;
        }

        if (item.getStatus() != Item.Status.OPEN) {
            return false;
        }

        if (this.accountBalance < bidAmount) {
            return false;
        }

        if (item.getCurrentPrice() >= bidAmount) {
            return false;
        }

        this.accountBalance -= bidAmount;
        item.setCurrentPrice(bidAmount);
        item.setHighestBidderId(this.getId());

        Bid bid = new Bid(this.getFullName(), bidAmount, LocalDateTime.now());
        return true;
    }

    public static class Bid {
        private String bidderName;
        private double bidAmount;
        private LocalDateTime bidTime;

        public Bid(String bidderName, double bidAmount, LocalDateTime bidTime) {
            this.bidderName = bidderName;
            this.bidAmount = bidAmount;
            this.bidTime = bidTime;
        }

        public String getBidderName() {
            return bidderName;
        }

        public double getBidAmount() {
            return bidAmount;
        }

        public LocalDateTime getBidTime() {
            return bidTime;
        }

        @Override
        public String toString() {
            return String.format("Bidder: %s | Amount: %.2f | Time: %s",
                    bidderName, bidAmount, bidTime);
        }
    }
}
