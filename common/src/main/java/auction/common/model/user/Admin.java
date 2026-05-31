package auction.common.model.user;

import auction.common.model.item.Item;
import auction.common.model.network.Role;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Admin extends User {
    private final List<String> privileges;

    public Admin(String username, String password, String fullName, List<String> privileges) {
        super(username, password, fullName, Role.ADMIN);
        this.privileges = (privileges != null) ? new ArrayList<>(privileges) : new ArrayList<>();
    }

    public List<String> getPrivileges() {
        return Collections.unmodifiableList(privileges);
    }

    public boolean openAuctionSession(Item item) {
        if (item == null) {
            return false;
        }
        if (item.getStatus() == Item.Status.PENDING) {
            Instant now = Instant.now();
            if (item.getStartTime() != null && item.getEndTime() != null) {
                Duration duration = Duration.between(item.getStartTime(), item.getEndTime());
                item.setEndTime(now.plus(duration));
            }
            item.setStartTime(now);
            item.setStatus(Item.Status.OPEN);
            return true;
        }
        return false;
    }
}
