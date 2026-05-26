package auction.common.repository;

import auction.common.model.user.User;

import auction.common.exception.DataAccessException;
import java.util.List;
import java.util.Optional;
//quản lí lưu trữ, truy xuất, cập nhật, xóa các user
public interface UserRepository {
    Optional<User> findById(String id) throws DataAccessException;
    Optional<User> findByUsername(String username) throws DataAccessException;
    List<User> findAll() throws DataAccessException;
    User save(User user) throws DataAccessException;
    void deleteById(String id) throws DataAccessException;
}