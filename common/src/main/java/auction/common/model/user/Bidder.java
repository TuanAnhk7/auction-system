package auction.common.model.user;

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
}
