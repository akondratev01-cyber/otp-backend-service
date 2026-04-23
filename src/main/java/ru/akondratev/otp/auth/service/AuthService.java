package ru.akondratev.otp.auth.service;

import ru.akondratev.otp.user.dto.LoginRequest;
import ru.akondratev.otp.user.dto.RegisterRequest;
import ru.akondratev.otp.user.model.UserAuthData;
import ru.akondratev.otp.user.repository.UserRepository;
import ru.akondratev.otp.user.util.AuthValidationUtil;
import ru.akondratev.otp.user.util.PasswordUtil;

import java.sql.SQLException;


public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public long registerUser(RegisterRequest request)  throws SQLException {
        validateRegisterRequest(request);

        String login = request.getLogin().trim();

        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        String passwordHash = PasswordUtil.hashPassword(request.getPassword());
        return userRepository.createUser(login, passwordHash);
    }

    public long registerAdmin(RegisterRequest request) throws SQLException {
        validateRegisterRequest(request);
        String login = request.getLogin().trim();

        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        if (userRepository.adminExists()) {
            throw new IllegalArgumentException("Администратор уже существует");
        }

        String passwordHash = PasswordUtil.hashPassword(request.getPassword());
        return userRepository.createAdmin(login, passwordHash);
    }

    public String login(LoginRequest request) throws SQLException {
        validateLoginRequest(request);

        String login = request.getLogin().trim();

        UserAuthData user = userRepository.findByLogin(login);
        if (user == null) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        boolean passwordMatches = PasswordUtil.matchesPassword(
                request.getPassword(),
                user.getPasswordHash()
        );
        if (!passwordMatches) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        return tokenService.generateOrRefreshToken(user.getId());
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Токен отсутствует");
        }

        tokenService.removeToken(token);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        String loginError = AuthValidationUtil.validateLogin(request.getLogin());
        if (loginError != null) {
            throw new IllegalArgumentException(loginError);
        }

        String passwordError = AuthValidationUtil.validatePassword(request.getPassword());
        if (passwordError != null) {
            throw new IllegalArgumentException(passwordError);
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        String loginError = AuthValidationUtil.validateLogin(request.getLogin());
        if (loginError != null) {
            throw new IllegalArgumentException(loginError);
        }

        String passwordError = AuthValidationUtil.validatePassword(request.getPassword());
        if (passwordError != null) {
            throw new IllegalArgumentException(passwordError);
        }
    }
}
