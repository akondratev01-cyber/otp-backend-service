package ru.akondratev.otp.common.http;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class MethodNotAllowedHandler {

    private static final Logger logger = LoggerFactory.getLogger(MethodNotAllowedHandler.class);

    private MethodNotAllowedHandler() {
    }

    public static void handle(HttpExchange exchange, String allowedMethods) throws IOException {
        logger.warn("Method not allowed: {} {} -> 405, allowed={}",
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                allowedMethods);

        exchange.getResponseHeaders().set("Allow", allowedMethods);
        ErrorResponseSender.sendError(exchange, 405, "Метод не поддерживается");
    }
}