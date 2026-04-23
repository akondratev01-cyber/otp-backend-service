package ru.akondratev.otp.otp.service;

import ru.akondratev.otp.otp.model.OtpChannel;
import ru.akondratev.otp.otp.model.OtpCode;
import ru.akondratev.otp.otp.model.OtpConfig;
import ru.akondratev.otp.otp.model.OtpStatus;
import ru.akondratev.otp.otp.repository.OtpCodeRepository;
import ru.akondratev.otp.otp.repository.OtpConfigRepository;
import ru.akondratev.otp.notification.file.FileOtpNotificationService;
import ru.akondratev.otp.notification.email.EmailOtpNotificationService;
import ru.akondratev.otp.notification.telegram.TelegramOtpNotificationService;
import ru.akondratev.otp.notification.sms.SmsOtpNotificationService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;

public class OtpService {

    private final OtpConfigRepository otpConfigRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final FileOtpNotificationService fileOtpNotificationService;
    private final EmailOtpNotificationService emailOtpNotificationService;
    private final TelegramOtpNotificationService telegramOtpNotificationService;
    private final SmsOtpNotificationService smsOtpNotificationService;

    public OtpService(
            OtpConfigRepository otpConfigRepository,
            OtpCodeRepository otpCodeRepository,
            FileOtpNotificationService fileOtpNotificationService,
            EmailOtpNotificationService emailOtpNotificationService,
            TelegramOtpNotificationService telegramOtpNotificationService,
            SmsOtpNotificationService smsOtpNotificationService) {
        this.otpConfigRepository = otpConfigRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.fileOtpNotificationService = fileOtpNotificationService;
        this.emailOtpNotificationService = emailOtpNotificationService;
        this.telegramOtpNotificationService = telegramOtpNotificationService;
        this.smsOtpNotificationService = smsOtpNotificationService;
    }

    public long generateOtp(long userId, String operationId, OtpChannel channel, String email) throws SQLException {
        validateGenerateRequest(userId, operationId, channel, email);

        otpCodeRepository.markExpiredCodes();

        OtpCode existingOtp = otpCodeRepository.findActiveCodeByOperation(userId, operationId);
        if (existingOtp != null && existingOtp.getExpiresAt().isAfter(LocalDateTime.now())) {
            long remainingSeconds = Duration.between(LocalDateTime.now(), existingOtp.getExpiresAt()).getSeconds();
            remainingSeconds = Math.max(remainingSeconds, 1);

            throw new IllegalArgumentException(
                    "Время действия ранее сгенерированного OTP-кода еще не истекло. Повторите попытку через " + remainingSeconds + " сек."
            );
        }

        OtpConfig config = otpConfigRepository.getCurrentConfig();
        if (config == null) {
            throw new IllegalStateException("Конфигурация OTP не найдена");
        }

        String code = generateNumericCode(config.getCodeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.getTtlSeconds());

        long otpId = otpCodeRepository.createOtpCode(
                userId,
                operationId,
                code,
                OtpStatus.ACTIVE,
                channel,
                expiresAt
        );

        if (channel == OtpChannel.FILE) {
            try {
                fileOtpNotificationService.saveOtpCode(userId, operationId, code);
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось сохранить OTP-код в файл", e);
            }
        }

        if (channel == OtpChannel.EMAIL) {
            try {
                emailOtpNotificationService.sendOtpCode(email, userId, operationId, code);
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось отправить OTP-код по email", e);
            }
        }

        if (channel == OtpChannel.TELEGRAM) {
            try {
                telegramOtpNotificationService.sendOtpCode(
                        userId,
                        operationId,
                        code,
                        expiresAt,
                        config.getTtlSeconds());
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось отправить OTP-код в Telegram", e);
            }
        }

        if (channel == OtpChannel.SMS) {
            try {
                smsOtpNotificationService.sendOtpCode(operationId, code);
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось отправить OTP-код по SMS", e);
            }
        }

        return otpId;
    }

    public OtpConfig getCurrentConfig() throws SQLException {
        OtpConfig config = otpConfigRepository.getCurrentConfig();
        if (config == null) {
            throw new IllegalStateException("Конфигурация OTP не найдена");
        }
        return config;
    }

    public boolean updateConfig(int codeLength, int ttlSeconds) throws SQLException {
        validateConfigUpdate(codeLength, ttlSeconds);
        return otpConfigRepository.updateConfig(codeLength, ttlSeconds);
    }

    public boolean validateOtp(long userId, String operationId, String code) throws SQLException {
        validateVerificationRequest(userId, operationId, code);

        otpCodeRepository.markExpiredCodes();

        OtpCode otpCode = otpCodeRepository.findActiveCode(userId, operationId, code);
        if (otpCode == null) {
            return false;
        }

        if (otpCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpCodeRepository.markExpiredCodes();
            return false;
        }

        return otpCodeRepository.markAsUsed(otpCode.getId());
    }

    private void validateConfigUpdate(int codeLength, int ttlSeconds) {
        if (codeLength < 4 || codeLength > 12) {
            throw new IllegalArgumentException("Длина OTP-кода должна быть от 4 до 12");
        }

        if (ttlSeconds < 30 || ttlSeconds > 3600) {
            throw new IllegalArgumentException("TTL OTP-кода должен быть от 30 до 3600 секунд");
        }
    }

    public int expireCodes() throws SQLException {
        return otpCodeRepository.markExpiredCodes();
    }

    private void validateGenerateRequest(long userId, String operationId, OtpChannel channel, String email) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Некорректный идентификатор пользователя");
        }

        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор операции обязателен");
        }

        if (channel == null) {
            throw new IllegalArgumentException("Канал отправки обязателен");
        }

        if (channel == OtpChannel.EMAIL) {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email обязателен для канала EMAIL");
            }
        }
    }

    private void validateVerificationRequest(long userId, String operationId, String code) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Некорректный идентификатор пользователя");
        }

        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор операции обязателен");
        }

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("OTP-код обязателен");
        }
    }

    private String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            builder.append(digit);
        }

        return builder.toString();
    }
}