package auction.common.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {//mã hóa mật khẩu để ktra tính chính xác mkhau khi đăng nhập
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        return BCrypt.checkpw(plainTextPassword, hashedPassword);
    }
}