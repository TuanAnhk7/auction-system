package auction.common.model.user;

import auction.common.model.BaseEntity;

public abstract class User extends BaseEntity {
    private final String username;
    private String password;
    private String fullName;

    protected User(String username, String password, String fullName) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        touch();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        touch();
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
