package auction.common.repository;

import auction.common.model.user.User;

import auction.common.exception.DataAccessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {//kho dữ liệu tạm thời dành cho user

    private final Map<String, User> users = new ConcurrentHashMap<>();

    private boolean simulateNetworkError = false;
    private int errorCounter = 0;
    private int errorsBeforeSuccess = 2;

    public void setSimulateNetworkError(boolean simulateNetworkError) {
        this.simulateNetworkError = simulateNetworkError;
        this.errorCounter = 0;
    }

    private void checkAndSimulateError() {
        if (simulateNetworkError) {
            errorCounter++;
            if (errorCounter <= errorsBeforeSuccess) {
                throw new DataAccessException("Simulated network error during data access.");
            }
        }
    }

    @Override
    public Optional<User> findById(String id) throws DataAccessException {
        checkAndSimulateError();
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByUsername(String username) throws DataAccessException {
        checkAndSimulateError();
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        checkAndSimulateError();
        return new ArrayList<>(users.values());
    }

    @Override
    public User save(User user) throws DataAccessException {
        checkAndSimulateError();
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(String id) throws DataAccessException {
        checkAndSimulateError();
        users.remove(id);
    }
}