package auction.common.service;

import auction.common.exception.AuthenticationException;
import auction.common.exception.UsernameAlreadyExistsException;
import auction.common.model.user.Bidder;
import auction.common.model.user.Role;
import auction.common.model.user.User;
import auction.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authService = new AuthService(userRepository);
    }

    @Test
    void registerUser_Success() throws AuthenticationException {
        String username = "testuser";
        String password = "password123";
        String fullName = "Test User";
        Role role = Role.BIDDER;

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(username, password, fullName, role);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists_ThrowsException() {
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mock(User.class)));

        assertThrows(UsernameAlreadyExistsException.class, () ->
            authService.registerUser(username, "password123", "Name", Role.BIDDER)
        );
    }

    @Test
    void login_Success() throws AuthenticationException {
        String username = "testuser";
        String password = "password123";
        String hashedPassword = auction.common.util.PasswordHasher.hashPassword(password);
        User user = new Bidder(username, hashedPassword, "Test User", 100.0);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = authService.login(username, password);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }
}
