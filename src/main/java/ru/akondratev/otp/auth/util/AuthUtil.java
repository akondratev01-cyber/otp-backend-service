package ru.akondratev.otp.auth.util;

import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.user.model.UserResponse;
import ru.akondratev.otp.user.repository.UserRepository;

import java.sql.SQLException;

public final class AuthUtil {

    private AuthUtil() {
    }

    public static UserResponse requireUserByToken(
            String token,
            TokenService tokenService,
            UserRepository userRepository
    ) throws SQLException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Токен отсутствует");
        }

        Long userId = tokenService.getUserIdByToken(token);
        if (userId == null) {
            throw new IllegalArgumentException("Токен недействителен или истек");
        }

        UserResponse user = userRepository.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        return user;
    }

    public static void requireAdmin(UserResponse user) {
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        if (!"ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("Недостаточно прав для доступа к этому ресурсу");
        }
    }
}
