package ru.akondratev.otp.app;

import ru.akondratev.otp.auth.controller.AuthController;
import ru.akondratev.otp.auth.service.AuthService;
import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.config.ApplicationProperties;
import ru.akondratev.otp.config.DatabaseConfig;
import ru.akondratev.otp.notification.email.EmailOtpNotificationService;
import ru.akondratev.otp.notification.file.FileOtpNotificationService;
import ru.akondratev.otp.notification.sms.SmsOtpNotificationService;
import ru.akondratev.otp.notification.telegram.TelegramOtpNotificationService;
import ru.akondratev.otp.otp.controller.OtpAdminController;
import ru.akondratev.otp.otp.controller.OtpController;
import ru.akondratev.otp.otp.repository.OtpCodeRepository;
import ru.akondratev.otp.otp.repository.OtpConfigRepository;
import ru.akondratev.otp.otp.scheduler.OtpExpirationScheduler;
import ru.akondratev.otp.otp.service.OtpService;
import ru.akondratev.otp.user.controller.UserController;
import ru.akondratev.otp.user.repository.UserRepository;

import javax.sql.DataSource;

public class AppComponents {

    private final ApplicationProperties properties;
    private final DataSource dataSource;

    private final UserRepository userRepository;
    private final OtpConfigRepository otpConfigRepository;
    private final OtpCodeRepository otpCodeRepository;

    private final FileOtpNotificationService fileOtpNotificationService;
    private final EmailOtpNotificationService emailOtpNotificationService;
    private final TelegramOtpNotificationService telegramOtpNotificationService;
    private final SmsOtpNotificationService smsOtpNotificationService;

    private final TokenService tokenService;
    private final AuthService authService;
    private final OtpService otpService;

    private final AuthController authController;
    private final UserController userController;
    private final OtpController otpController;
    private final OtpAdminController otpAdminController;

    private final OtpExpirationScheduler otpExpirationScheduler;

    public AppComponents(ApplicationProperties properties) {
        this.properties = properties;

        DatabaseConfig databaseConfig = new DatabaseConfig(properties);
        this.dataSource = databaseConfig.dataSource();

        this.userRepository = new UserRepository(dataSource);
        this.otpConfigRepository = new OtpConfigRepository(dataSource);
        this.otpCodeRepository = new OtpCodeRepository(dataSource);

        this.fileOtpNotificationService =
                new FileOtpNotificationService(properties.get("otp.file.path"));

        this.emailOtpNotificationService =
                new EmailOtpNotificationService(
                        properties.get("mail.smtp.host"),
                        properties.getInt("mail.smtp.port"),
                        properties.get("mail.smtp.username"),
                        properties.get("mail.smtp.password"),
                        properties.get("mail.from.email"),
                        Boolean.parseBoolean(properties.get("mail.smtp.auth")),
                        Boolean.parseBoolean(properties.get("mail.smtp.starttls.enable"))
                );

        this.telegramOtpNotificationService =
                new TelegramOtpNotificationService(
                        properties.get("telegram.bot.token"),
                        properties.get("telegram.chat.id")
                );

        this.smsOtpNotificationService =
                new SmsOtpNotificationService(
                        properties.get("smpp.host"),
                        properties.getInt("smpp.port"),
                        properties.get("smpp.system_id"),
                        properties.get("smpp.password"),
                        properties.get("smpp.system_type"),
                        properties.get("smpp.source_addr")
                );

        this.tokenService = new TokenService(properties);
        this.authService = new AuthService(userRepository, tokenService);

        this.otpService = new OtpService(
                otpConfigRepository,
                otpCodeRepository,
                fileOtpNotificationService,
                emailOtpNotificationService,
                telegramOtpNotificationService,
                smsOtpNotificationService
        );

        this.authController = new AuthController(authService);
        this.userController = new UserController(userRepository, tokenService);
        this.otpController = new OtpController(otpService, tokenService, userRepository);
        this.otpAdminController = new OtpAdminController(otpService, tokenService, userRepository);

        this.otpExpirationScheduler =
                new OtpExpirationScheduler(
                        otpService,
                        properties.getInt("otp.expire.check.interval.seconds")
                );
    }

    public ApplicationProperties properties() {
        return properties;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public UserRepository userRepository() {
        return userRepository;
    }

    public OtpConfigRepository otpConfigRepository() {
        return otpConfigRepository;
    }

    public OtpCodeRepository otpCodeRepository() {
        return otpCodeRepository;
    }

    public FileOtpNotificationService fileOtpNotificationService() {
        return fileOtpNotificationService;
    }

    public EmailOtpNotificationService emailOtpNotificationService() {
        return emailOtpNotificationService;
    }

    public TelegramOtpNotificationService telegramOtpNotificationService() {
        return telegramOtpNotificationService;
    }

    public SmsOtpNotificationService smsOtpNotificationService() {
        return smsOtpNotificationService;
    }

    public TokenService tokenService() {
        return tokenService;
    }

    public AuthService authService() {
        return authService;
    }

    public OtpService otpService() {
        return otpService;
    }

    public AuthController authController() {
        return authController;
    }

    public UserController userController() {
        return userController;
    }

    public OtpController otpController() {
        return otpController;
    }

    public OtpAdminController otpAdminController() {
        return otpAdminController;
    }

    public OtpExpirationScheduler otpExpirationScheduler() {
        return otpExpirationScheduler;
    }
}