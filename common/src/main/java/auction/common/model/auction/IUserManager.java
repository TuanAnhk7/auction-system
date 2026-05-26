package auction.common.model.auction;
import auction.common.model.network.UserAccount;
public interface IUserManager {
    UserAccount findByUsername(String username);
    void updateAccountBalance(String username, double amount);
}
