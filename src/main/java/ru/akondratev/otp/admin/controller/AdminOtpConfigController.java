package ru.akondratev.otp.admin.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.akondratev.otp.admin.service.AdminOtpConfigService;
import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.auth.util.AuthUtil;
import ru.akondratev.otp.common.http.ErrorResponseSender;
import ru.akondratev.otp.common.http.JsonResponseSender;
import ru.akondratev.otp.common.http.MethodNotAllowedHandler;
import ru.akondratev.otp.common.http.RequestBodyReader;
import ru.akondratev.otp.otp.dto.UpdateOtpConfigRequest;
import ru.akondratev.otp.otp.model.OtpConfig;
import ru.akondratev.otp.user.model.UserResponse;
import ru.akondratev.otp.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

public class AdminOtpConfigController implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdminOtpConfigController.class);

    private final AdminOtpConfigService adminOtpConfigService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AdminOtpConfigController(
            AdminOtpConfigService adminOtpConfigService,
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.adminOtpConfigService = adminOtpConfigService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        logger.info("Incoming request: {} {}", method, path);

        try {
            if (!"/admin/otp-config".equals(path)) {
                ErrorResponseSender.sendError(exchange, 404, "Ресурс не найден");
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                handleGetConfig(exchange);
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                handleUpdateConfig(exchange);
                return;
            }

            MethodNotAllowedHandler.handle(exchange, "GET, PUT");

        } catch (IllegalArgumentException e) {
            logger.warn("Request validation failed: {} {} -> 400, message={}", method, path, e.getMessage());
            ErrorResponseSender.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while handling request: {} {} -> 500", method, path, e);
            ErrorResponseSender.sendError(exchange, 500, "Внутренняя ошибка сервера");
        }
    }

    private void handleGetConfig(HttpExchange exchange) throws Exception {
        UserResponse currentUser = requireAdminUser(exchange);
        OtpConfig config = adminOtpConfigService.getCurrentConfig();

        logger.info("OTP config requested by admin: adminId={}, adminLogin={}",
                currentUser.getId(),
                currentUser.getLogin());

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "id", config.getId(),
                "codeLength", config.getCodeLength(),
                "ttlSeconds", config.getTtlSeconds(),
                "updatedAt", config.getUpdatedAt(),
                "requestedBy", currentUser.getLogin()
        ));
    }

    private void handleUpdateConfig(HttpExchange exchange) throws Exception {
        UserResponse currentUser = requireAdminUser(exchange);

        UpdateOtpConfigRequest request = RequestBodyReader.readJson(exchange, UpdateOtpConfigRequest.class);
        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        if (request.getCodeLength() == null) {
            throw new IllegalArgumentException("Необходимо указать codeLength");
        }

        if (request.getTtlSeconds() == null) {
            throw new IllegalArgumentException("Необходимо указать ttlSeconds");
        }

        adminOtpConfigService.updateConfig(request.getCodeLength(), request.getTtlSeconds());

        OtpConfig config = adminOtpConfigService.getCurrentConfig();

        logger.info("OTP config updated by admin: adminId={}, adminLogin={}, codeLength={}, ttlSeconds={}",
                currentUser.getId(),
                currentUser.getLogin(),
                config.getCodeLength(),
                config.getTtlSeconds());

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "message", "Конфигурация OTP успешно обновлена",
                "id", config.getId(),
                "codeLength", config.getCodeLength(),
                "ttlSeconds", config.getTtlSeconds(),
                "updatedAt", config.getUpdatedAt()
        ));
    }

    private UserResponse requireAdminUser(HttpExchange exchange) throws Exception {
        String token = extractBearerToken(exchange);
        UserResponse currentUser = AuthUtil.requireUserByToken(token, tokenService, userRepository);
        AuthUtil.requireAdmin(currentUser);
        return currentUser;
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