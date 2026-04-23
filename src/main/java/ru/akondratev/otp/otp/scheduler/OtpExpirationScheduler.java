package ru.akondratev.otp.otp.scheduler;

import ru.akondratev.otp.otp.service.OtpService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpirationScheduler {

    private final OtpService otpService;
    private final int intervalSeconds;
    private final ScheduledExecutorService executorService;

    public OtpExpirationScheduler(OtpService otpService, int intervalSeconds) {
        this.otpService = otpService;
        this.intervalSeconds = intervalSeconds;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                int expiredCount = otpService.expireCodes();
                if (expiredCount > 0) {
                    System.out.println("Переведено в EXPIRED: " + expiredCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
    }
}