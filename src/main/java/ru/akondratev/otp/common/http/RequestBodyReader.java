package ru.akondratev.otp.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import ru.akondratev.otp.common.json.ObjectMapperFactory;

import java.io.IOException;
import java.io.InputStream;

public class RequestBodyReader {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    private RequestBodyReader() {
    }

    public static <T> T readJson(HttpExchange exchange, Class<T> targetClass) throws IOException {
        try (InputStream requestStream = exchange.getRequestBody()) {
            return OBJECT_MAPPER.readValue(requestStream, targetClass);
        }
    }
}
