package ru.akondratev.otp.notification.telegram;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TelegramOtpNotificationService {

    private final String botToken;
    private final String chatId;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TelegramOtpNotificationService(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void sendOtpCode(
            long userId,
            String operationId,
            String code,
            LocalDateTime expiresAt,
            int ttlSeconds) {
        String formattedExpiresAt = expiresAt.format(DATE_TIME_FORMATTER);
        String message = """
        🔐 <b>Your OTP Code</b>
        
        <b>Code:</b> <code>%s</code>
        <b>Operation ID:</b> %s
        <b>User ID:</b> %d
        <b>Valid for:</b> %d seconds
        <b>Expires at:</b> %s
        
        This code is valid for a limited time.
        """.formatted(code, operationId, userId, ttlSeconds, formattedExpiresAt);

        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&parse_mode=HTML&text=%s",
                botToken,
                chatId,
                urlEncode(message)
        );

        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                throw new IllegalStateException(
                        "Telegram API error. Status code: " + statusCode
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error sending Telegram message.", e);
        } catch (IOException e) {
            throw new RuntimeException("Error sending Telegram message.", e);
        }
    }


    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}