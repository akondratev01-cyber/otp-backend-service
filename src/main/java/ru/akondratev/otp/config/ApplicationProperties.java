package ru.akondratev.otp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
    private final Properties properties = new Properties();

    public ApplicationProperties() {
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (inputStream == null) {
                throw new IllegalStateException("application.properties not found");
            }

            properties.load(inputStream);

            try (InputStream localInputStream = getClass()
                    .getClassLoader()
                    .getResourceAsStream("application-local.properties")) {
                if (localInputStream != null) {
                    properties.load(localInputStream);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }
}
