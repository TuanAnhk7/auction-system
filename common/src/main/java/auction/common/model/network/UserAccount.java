package auction.common.model.network;

import java.io.Serializable;

public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final Role role;
    private double accountBalance;

    public UserAccount(String username, String password, Role role) {
        this(username, password, role, 0.0);
    }

    public UserAccount(String username, String password, Role role, double accountBalance) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.accountBalance = accountBalance;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public double getAccountBalance() { // Thêm getter
        return accountBalance;
    }

    public void setAccountBalance(double accountBalance) { // Thêm setter
        this.accountBalance = accountBalance;
    }
}