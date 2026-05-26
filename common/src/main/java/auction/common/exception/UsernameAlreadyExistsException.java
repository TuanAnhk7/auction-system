package auction.common.exception;

public class UsernameAlreadyExistsException extends AuthenticationException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}