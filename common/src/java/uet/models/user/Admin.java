package src.java.uet.models.user;

public class Admin extends User {
    private String[] privileges;

    public String[] getPrivileges() { return privileges; }

    public void blockUser(User user) {
        // Logic khóa người dùng
        System.out.println("User " + user.getUsername() + " has been blocked.");
    }
}