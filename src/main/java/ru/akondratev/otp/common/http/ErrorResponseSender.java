package ru.akondratev.otp.common.http;

import com.sun.net.httpserver.HttpExchange;
import ru.akondratev.otp.common.error.ErrorResponse;

import java.io.IOException;

public final class ErrorResponseSender {

    private ErrorResponseSender() {
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        JsonResponseSender.sendJson(exchange, statusCode, new ErrorResponse(message));
    }
}