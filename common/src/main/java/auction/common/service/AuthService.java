package auction.common.service;

import auction.common.exception.AuthenticationException;
import auction.common.exception.InvalidCredentialsException;
import auction.common.exception.InvalidPasswordException;
import auction.common.exception.UserNotFoundException;
import auction.common.exception.DataAccessException;
import auction.common.exception.UsernameAlreadyExistsException;
import auction.common.model.user.Admin;
import auction.common.model.user.Bidder;
import auction.common.model.user.Role;
import auction.common.model.user.Seller;
import auction.common.model.user.User;
import auction.common.repository.UserRepository;
import auction.common.util.PasswordHasher;

import java.util.Optional;
import java.util.Collections;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 100;

    public User registerUser(String username, String plainTextPassword, String fullName, Role role) throws AuthenticationException {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException("Username '" + username + "' already exists.");
        }
        if (plainTextPassword == null || plainTextPassword.length() < 6) {
            throw new InvalidPasswordException("Password must be at least 6 characters long.");
        }
        String hashedPassword = PasswordHasher.hashPassword(plainTextPassword);
        User newUser;
        switch (role) {
            case BIDDER -> newUser = new Bidder(username, hashedPassword, fullName, 0.0);
            case SELLER -> newUser = new Seller(username, hashedPassword, fullName, 0.0);
            case ADMIN -> newUser = new Admin(username, hashedPassword, fullName, Collections.emptyList());
            default -> throw new AuthenticationException("Invalid role specified for registration.");
        }

        // Retry mechanism for saving new user
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return userRepository.save(newUser);
            } catch (DataAccessException e) {
                if (i < MAX_RETRIES - 1) {
                    System.err.println("Data access error during registration, retrying... (" + (i + 1) + "/" + MAX_RETRIES + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AuthenticationException("Registration interrupted.", ie);
                    }
                } else {
                    throw new AuthenticationException("Failed to register user after " + MAX_RETRIES + " retries.", e);
                }
            }
        }
        throw new AuthenticationException("Unexpected error during user registration."); // Should not be reached
    }

    public User login(String username, String plainTextPassword) throws AuthenticationException {
        Optional<User> userOptional = Optional.empty();
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                userOptional = userRepository.findByUsername(username);
                break; // Success, exit retry loop
            } catch (DataAccessException e) {
                if (i < MAX_RETRIES - 1) {
                    System.err.println("Data access error during login, retrying... (" + (i + 1) + "/" + MAX_RETRIES + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AuthenticationException("Login interrupted.", ie);
                    }
                } else {
                    throw new AuthenticationException("Failed to login after " + MAX_RETRIES + " retries.", e);
                }
            }
        }

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with username '" + username + "' not found.");
        }
        User user = userOptional.get();
        if (!PasswordHasher.checkPassword(plainTextPassword, user.getHashedPassword())) {
            throw new InvalidCredentialsException("Incorrect password for user '" + username + "'.");
        }
        return user;
    }
}