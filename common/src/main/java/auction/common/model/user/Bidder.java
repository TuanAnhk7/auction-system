package auction.common.model.user;

import auction.common.model.item.Item;
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
        if (item == null) {
            return false;
        }

        synchronized (item) {
            if (item.getStatus() != Item.Status.OPEN) {
                return false;
            }

            Instant now = Instant.now();
            if (item.getStartTime() == null || now.isBefore(item.getStartTime()) || 
                item.getEndTime() == null || now.isAfter(item.getEndTime())) {
                return false;
            }

            if (this.accountBalance < bidAmount) {
                return false;
            }

            boolean isFirstBid = item.getHighestBidderId() == null;
            if (isFirstBid) {
                if (bidAmount < item.getStartingPrice()) {
                    return false;
                }
            } else {
                if (bidAmount <= item.getCurrentPrice()) {
                    return false;
                }
            }

            item.setCurrentPrice(bidAmount);
            item.setHighestBidderId(this.getId());

            return true;
        }
    }
}