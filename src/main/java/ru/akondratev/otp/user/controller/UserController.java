package ru.akondratev.otp.user.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.auth.util.AuthUtil;
import ru.akondratev.otp.common.http.ErrorResponseSender;
import ru.akondratev.otp.common.http.JsonResponseSender;
import ru.akondratev.otp.common.http.MethodNotAllowedHandler;
import ru.akondratev.otp.user.model.UserResponse;
import ru.akondratev.otp.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UserController implements HttpHandler {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserController(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("/users/me".equals(path)){
                handleMe(exchange, method);
                return;
            }

            if ("/admin/users".equals(path)){
                handleAdminUsers(exchange, method);
                return;
            }

            if (path.startsWith("/admin/users/")){
                handleDeleteUser(exchange, path, method);
                return;
            }

            ErrorResponseSender.sendError(exchange, 404, "Ресурс не найден");

        } catch (IllegalArgumentException e) {
            ErrorResponseSender.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponseSender.sendError(exchange, 500, "Внутренняя ошибка сервера");
        }
    }

    private void handleMe(HttpExchange exchange, String method) throws Exception {
        if (!"GET".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange,"GET");
            return;
        }

        String token = extractBearerToken(exchange);
        UserResponse currentUser = AuthUtil.requireUserByToken(token, tokenService, userRepository);

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "id", currentUser.getId(),
                "login", currentUser.getLogin(),
                "role", currentUser.getRole()
        ));
    }

    private void handleAdminUsers(HttpExchange exchange, String method) throws Exception {
        if (!"GET".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange,"GET");
            return;
        }

        String token = extractBearerToken(exchange);
        UserResponse currentUser = AuthUtil.requireUserByToken(token, tokenService, userRepository);
        AuthUtil.requireAdmin(currentUser);

        List<UserResponse> users = userRepository.findAllNonAdminUsers();
        JsonResponseSender.sendJson(exchange, 200, users);
    }

    private void handleDeleteUser(HttpExchange exchange, String path, String method) throws Exception {
        if (!"DELETE".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange,"DELETE");
            return;
        }

        String token = extractBearerToken(exchange);
        UserResponse currentUser = AuthUtil.requireUserByToken(token, tokenService, userRepository);
        AuthUtil.requireAdmin(currentUser);

        String[] paths = path.split("/");
        if (paths.length !=4) {
            throw new IllegalArgumentException("Некорректный путь запроса");
        }

        long userId;
        try {
            userId = Long.parseLong(paths[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Идентификатор пользователя должен быть числом");
        }

        boolean deleted = userRepository.deleteById(userId);
        if (!deleted) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "message", "Пользователь успешно удален",
                "id", userId
        ));
    }

    private String extractBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Заголовок Authorization отсутствует");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Некорректный формат Authorization");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            throw new IllegalArgumentException("Токен отсутствует");
        }

        return token;
    }
}
