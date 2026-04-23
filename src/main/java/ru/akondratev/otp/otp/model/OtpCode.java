package ru.akondratev.otp.otp.model;

import java.time.LocalDateTime;

public class OtpCode {

    private final long id;
    private final long userId;
    private final String operationId;
    private final String code;
    private final OtpStatus status;
    private final OtpChannel channel;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final LocalDateTime usedAt;

    public OtpCode(
            long id,
            long userId,
            String operationId,
            String code,
            OtpStatus status,
            OtpChannel channel,
            LocalDateTime createdAt,
            LocalDateTime expiresAt,
            LocalDateTime usedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.operationId = operationId;
        this.code = code;
        this.status = status;
        this.channel = channel;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCode() {
        return code;
    }

    public OtpStatus getStatus() {
        return status;
    }

    public OtpChannel getChannel() {
        return channel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
}
