package ru.akondratev.otp.common.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public final class MethodNotAllowedHandler {

    private MethodNotAllowedHandler() {
    }

    public static void handle(HttpExchange exchange, String allowedMethods) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethods);
        ErrorResponseSender.sendError(exchange, 405, "Метод не поддерживается");
    }
}