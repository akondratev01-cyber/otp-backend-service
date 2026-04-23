package ru.akondratev.otp.admin.service;

import ru.akondratev.otp.otp.model.OtpConfig;
import ru.akondratev.otp.otp.service.OtpService;

import java.sql.SQLException;

public class AdminOtpConfigService {

    private final OtpService otpService;

    public AdminOtpConfigService(OtpService otpService) {
        this.otpService = otpService;
    }

    public OtpConfig getCurrentConfig() throws SQLException {
        return otpService.getCurrentConfig();
    }

    public void updateConfig(int codeLength, int ttlSeconds) throws SQLException {
        boolean updated = otpService.updateConfig(codeLength, ttlSeconds);
        if (!updated) {
            throw new IllegalStateException("Не удалось обновить конфигурацию OTP");
        }
    }
}