package auction.common.model.user;

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
}
