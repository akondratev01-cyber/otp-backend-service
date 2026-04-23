package ru.akondratev.otp.app;

import com.sun.net.httpserver.HttpServer;
import ru.akondratev.otp.auth.controller.AuthController;
import ru.akondratev.otp.auth.service.AuthService;
import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.config.ApplicationProperties;
import ru.akondratev.otp.config.DatabaseConfig;
import ru.akondratev.otp.user.repository.UserRepository;
import ru.akondratev.otp.user.controller.UserController;
import ru.akondratev.otp.otp.controller.OtpController;
import ru.akondratev.otp.otp.repository.OtpCodeRepository;
import ru.akondratev.otp.otp.repository.OtpConfigRepository;
import ru.akondratev.otp.otp.service.OtpService;
import ru.akondratev.otp.notification.file.FileOtpNotificationService;
import ru.akondratev.otp.otp.scheduler.OtpExpirationScheduler;
import ru.akondratev.otp.otp.controller.OtpAdminController;
import ru.akondratev.otp.notification.email.EmailOtpNotificationService;
import ru.akondratev.otp.notification.telegram.TelegramOtpNotificationService;
import ru.akondratev.otp.notification.sms.SmsOtpNotificationService;

import javax.sql.DataSource;
import java.net.InetSocketAddress;

public class Application {
    public static void main(String[] args) {
        try {
            ApplicationProperties properties = new ApplicationProperties();
            DatabaseConfig databaseConfig = new DatabaseConfig(properties);
            DataSource dataSource = databaseConfig.dataSource();
            FileOtpNotificationService fileOtpNotificationService =
                    new FileOtpNotificationService(properties.get("otp.file.path"));
            EmailOtpNotificationService emailOtpNotificationService =
                    new EmailOtpNotificationService(
                            properties.get("mail.smtp.host"),
                            properties.getInt("mail.smtp.port"),
                            properties.get("mail.smtp.username"),
                            properties.get("mail.smtp.password"),
                            properties.get("mail.from.email"),
                            Boolean.parseBoolean(properties.get("mail.smtp.auth")),
                            Boolean.parseBoolean(properties.get("mail.smtp.starttls.enable"))
                    );
            TelegramOtpNotificationService telegramOtpNotificationService =
                    new TelegramOtpNotificationService(
                            properties.get("telegram.bot.token"),
                            properties.get("telegram.chat.id")
                    );
            SmsOtpNotificationService smsOtpNotificationService =
                    new SmsOtpNotificationService(
                            properties.get("smpp.host"),
                            properties.getInt("smpp.port"),
                            properties.get("smpp.system_id"),
                            properties.get("smpp.password"),
                            properties.get("smpp.system_type"),
                            properties.get("smpp.source_addr")
                    );

            UserRepository userRepository = new UserRepository(dataSource);
            OtpConfigRepository otpConfigRepository = new OtpConfigRepository(dataSource);
            OtpCodeRepository otpCodeRepository = new OtpCodeRepository(dataSource);
            OtpService otpService = new OtpService(
                    otpConfigRepository,
                    otpCodeRepository,
                    fileOtpNotificationService,
                    emailOtpNotificationService,
                    telegramOtpNotificationService,
                    smsOtpNotificationService
            );
            OtpExpirationScheduler otpExpirationScheduler =
                    new OtpExpirationScheduler(otpService, properties.getInt("otp.expire.check.interval.seconds"));
            TokenService tokenService = new TokenService(properties);
            AuthService authService = new AuthService(userRepository, tokenService);
            AuthController authController = new AuthController(authService);
            UserController userController = new UserController(userRepository, tokenService);
            OtpController otpController = new OtpController(otpService, tokenService, userRepository);
            OtpAdminController otpAdminController =
                    new OtpAdminController(otpService, tokenService, userRepository);


            int port = properties.getInt("app.port");

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/auth/register", authController);
            server.createContext("/auth/login", authController);
            server.createContext("/auth/logout", authController);
            server.createContext("/users/me", userController);
            server.createContext("/admin/users", userController);
            server.createContext("/otp/generate", otpController);
            server.createContext("/otp/validate", otpController);
            server.createContext("/admin/otp-config", otpAdminController);
            server.setExecutor(null);
            server.start();

            otpExpirationScheduler.start();

            System.out.println("HTTP Server Started on port " + port);
            System.out.println("Available endpoints: ");
            System.out.println("POST /auth/register - Регистрация нового пользователя");
            System.out.println("POST /auth/login - Логин пользователя");
            System.out.println("POST /auth/logout - Логаут пользователя");
            System.out.println("GET /users/me - Получить информацию о текущем пользователе (требуется токен в заголовке Authorization)");
            System.out.println("GET /admin/users - Получить список всех пользователей (требуется токен администратора в заголовке Authorization)");
            System.out.println("DELETE /admin/users/{id} - Удалить пользователя по id (требуется токен администратора в заголовке Authorization)");
            System.out.println("POST /otp/generate - Сгенерировать OTP-код");
            System.out.println("POST /otp/validate - Проверить OTP-код");
            System.out.println("OTP expiration scheduler started");
            System.out.println("GET /admin/otp-config - Получить текущую OTP-конфигурацию");
            System.out.println("PUT /admin/otp-config - Обновить OTP-конфигурацию");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}