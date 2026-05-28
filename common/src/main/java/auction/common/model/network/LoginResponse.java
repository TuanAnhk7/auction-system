package auction.common.model.network;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Role role;
    private final double balance;

    public LoginResponse(boolean success, String message, Role role) {
        this(success, message, role, 0.0);
    }

    public LoginResponse(boolean success, String message, Role role, double balance) {
        this.success = success;
        this.message = message;
        this.role = role;
        this.balance = balance;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Role getRole() {
        return role;
    }

    public double getBalance() {
        return balance;
    }
}
