package ru.akondratev.otp.otp.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.auth.util.AuthUtil;
import ru.akondratev.otp.common.http.ErrorResponseSender;
import ru.akondratev.otp.common.http.JsonResponseSender;
import ru.akondratev.otp.common.http.MethodNotAllowedHandler;
import ru.akondratev.otp.common.http.RequestBodyReader;
import ru.akondratev.otp.otp.dto.GenerateOtpRequest;
import ru.akondratev.otp.otp.dto.ValidateOtpRequest;
import ru.akondratev.otp.otp.model.OtpChannel;
import ru.akondratev.otp.otp.service.OtpService;
import ru.akondratev.otp.user.model.UserResponse;
import ru.akondratev.otp.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;

public class OtpController implements HttpHandler {

    private final OtpService otpService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public OtpController(
            OtpService otpService,
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("/otp/generate".equals(path)) {
                handleGenerate(exchange, method);
                return;
            }

            if ("/otp/validate".equals(path)) {
                handleValidate(exchange, method);
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

    private void handleGenerate(HttpExchange exchange, String method) throws Exception {
        if (!"POST".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange, "POST");
            return;
        }

        String token = extractBearerToken(exchange);
        UserResponse currentUser = AuthUtil.requireUserByToken(token, tokenService, userRepository);

        GenerateOtpRequest request = RequestBodyReader.readJson(exchange, GenerateOtpRequest.class);

        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        OtpChannel channel = parseChannel(request.getChannel());
        long otpId = otpService.generateOtp(
                currentUser.getId(),
                request.getOperationId(),
                channel,
                request.getEmail()
        );

        JsonResponseSender.sendJson(exchange, 201, Map.of(
                "message", "OTP-код успешно создан",
                "otpId", otpId,
                "operationId", request.getOperationId(),
                "channel", channel.name()
        ));
    }

    private void handleValidate(HttpExchange exchange, String method) throws Exception {
        if (!"POST".equalsIgnoreCase(method)) {
            MethodNotAllowedHandler.handle(exchange, "POST");
            return;
        }

        String token = extractBearerToken(exchange);
        UserResponse currentUser = AuthUtil.requireUserByToken(token, tokenService, userRepository);

        ValidateOtpRequest request = RequestBodyReader.readJson(exchange, ValidateOtpRequest.class);

        if (request == null) {
            throw new IllegalArgumentException("Тело запроса отсутствует");
        }

        boolean valid = otpService.validateOtp(
                currentUser.getId(),
                request.getOperationId(),
                request.getCode()
        );

        JsonResponseSender.sendJson(exchange, 200, Map.of(
                "valid", valid,
                "operationId", request.getOperationId()
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

    private OtpChannel parseChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("Канал отправки обязателен");
        }

        try {
            return OtpChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный канал отправки");
        }
    }
}