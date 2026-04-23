package ru.akondratev.otp.auth.service;

import ru.akondratev.otp.config.ApplicationProperties;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TokenService {

    private final Map<String, TokenData> tokenToData = new ConcurrentHashMap<>();
    private final Map<Long, String> userIdToToken = new ConcurrentHashMap<>();
    private final int tokenTtlMinutes;

    public TokenService(ApplicationProperties properties) {
        this.tokenTtlMinutes = properties.getInt("auth.token.ttl.minutes");
    }

    public String generateOrRefreshToken(long userId) {
        removeExpiredTokens();

        String existingToken = userIdToToken.get(userId);
        if (existingToken != null) {
            TokenData existingData = tokenToData.get(existingToken);

            if (existingData != null && !existingData.isExpired()) {
                return existingToken;
            }

            removeToken(existingToken);
        }

        String newToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenTtlMinutes);

        TokenData tokenData = new TokenData(userId, expiresAt);
        tokenToData.put(newToken, tokenData);
        userIdToToken.put(userId, newToken);

        return newToken;
    }

    public Long getUserIdByToken(String token) {
        removeExpiredTokens();

        TokenData tokenData = tokenToData.get(token);
        if (tokenData == null) {
            return null;
        }

        if (tokenData.isExpired()) {
            removeToken(token);
            return null;
        }

        return tokenData.getUserId();
    }

    public boolean hasActiveToken(long userId) {
        removeExpiredTokens();

        String token = userIdToToken.get(userId);
        if (token == null) {
            return false;
        }

        TokenData tokenData = tokenToData.get(token);
        if (tokenData == null || tokenData.isExpired()) {
            removeToken(token);
            return false;
        }

        return true;
    }

    public String getActiveTokenByUserId(long userId) {
        removeExpiredTokens();

        String token = userIdToToken.get(userId);
        if (token == null) {
            return null;
        }

        TokenData tokenData = tokenToData.get(token);
        if (tokenData == null || tokenData.isExpired()) {
            removeToken(token);
            return null;
        }

        return token;
    }

    public void removeToken(String token) {
        TokenData tokenData = tokenToData.remove(token);

        if (tokenData != null) {
            userIdToToken.remove(tokenData.getUserId());
        }
    }

    public void removeExpiredTokens() {
        for (Map.Entry<String, TokenData> entry : tokenToData.entrySet()) {
            String token = entry.getKey();
            TokenData tokenData = entry.getValue();

            if (tokenData.isExpired()) {
                removeToken(token);
            }
        }
    }

    private static class TokenData {
        private final long userId;
        private final LocalDateTime expiresAt;

        public TokenData(long userId, LocalDateTime expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }

        public long getUserId() {
            return userId;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
