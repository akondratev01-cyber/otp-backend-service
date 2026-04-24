package ru.akondratev.otp.auth.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.akondratev.otp.auth.service.AuthService;
import ru.akondratev.otp.common.http.ErrorResponseSender;
import ru.akondratev.otp.common.http.JsonResponseSender;
import ru.akondratev.otp.common.http.MethodNotAllowedHandler;
import ru.akondratev.otp.common.http.RequestBodyReader;
import ru.akondratev.otp.user.dto.LoginRequest;
import ru.akondratev.otp.user.dto.RegisterRequest;

import java.io.IOException;
import java.util.Map;

public class AuthController implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        logger.info("Incoming request: {} {}", method, path);

        try {
            if ("/auth/register".equals(path)) {
                handleRegister(exchange, method);
                return;
            }

            if ("/auth/login".equals(path)) {
                handleLogin(exchange, method);
                return;
            }

            if ("/auth/logout".equals(path)) {
                handleLogout(exchange, method);
                return;
            }

            ErrorResponseSender.sendError(exchange, 404, "Ресурс не найден");
        } catch (IllegalArgumentException e) {
            logger.warn("Request validation failed: {} {} -> 400, message={}", method, path, e.getMessage());
            ErrorResponseSender.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while handling request: {} {} -> 500", method, path, e);
            ErrorResponseSender.sendError(exchange, 500, "Внутренняя ошибка сервера");
        }
    }

    private void handleRegister(HttpExchange exchange, String method) throws Exception {
        if (!"POST".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange, "POST");
            return;
        }

        RegisterRequest request = RequestBodyReader.readJson(exchange, RegisterRequest.class);
        long userId = authService.registerUser(request);

        logger.info("User registered successfully: login={}, id={}", request.getLogin().trim(), userId);

        JsonResponseSender.sendJson(exchange, 201, Map.of(
                "message", "Пользователь успешно зарегистрирован",
                "id", userId,
                "login", request.getLogin().trim()
        ));
    }

    private void handleLogin(HttpExchange exchange, String method) throws Exception {
        if (!"POST".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange, "POST");
            return;
        }

        LoginRequest request = RequestBodyReader.readJson(exchange, LoginRequest.class);
        String token = authService.login(request);

        logger.info("User logged in successfully: login={}", request.getLogin());

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "message", "Вход выполнен успешно",
                "token", token
        ));
    }

    private void handleLogout(HttpExchange exchange, String method) throws Exception {
        if (!"POST".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange, "POST");
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Заголовок Authorization отсутствует");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Неверный формат заголовка Authorization");
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        authService.logout(token);

        logger.info("User logged out successfully");

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "message", "Выход выполнен успешно"
        ));
    }
}