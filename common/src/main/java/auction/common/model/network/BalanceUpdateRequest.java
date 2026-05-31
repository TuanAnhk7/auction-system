package auction.common.model.network;

import java.io.Serializable;

public class BalanceUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double amount;

    public BalanceUpdateRequest(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
