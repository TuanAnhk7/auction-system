package com.auction.common.model.user;

public abstract class User extends Entity {
    private String username;
    private String password;
    private String fullName;

    // Constructor và các Methods
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public String toString() { return fullName; }
}
