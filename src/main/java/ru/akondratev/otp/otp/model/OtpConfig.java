package ru.akondratev.otp.otp.model;

import java.time.LocalDateTime;

public class OtpConfig {

    private final int id;
    private final int codeLength;
    private final int ttlSeconds;
    private final LocalDateTime updatedAt;

    public OtpConfig(int id, int codeLength, int ttlSeconds, LocalDateTime updatedAt) {
        this.id = id;
        this.codeLength = codeLength;
        this.ttlSeconds = ttlSeconds;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
