package ru.akondratev.otp.admin.service;

import ru.akondratev.otp.user.model.UserResponse;
import ru.akondratev.otp.user.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllNonAdminUsers() throws SQLException {
        return userRepository.findAllNonAdminUsers();
    }

    public void deleteUserById(long userId) throws SQLException {
        boolean deleted = userRepository.deleteById(userId);
        if (!deleted) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
    }
}