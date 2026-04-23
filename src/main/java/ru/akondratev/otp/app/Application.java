package ru.akondratev.otp.app;

import com.sun.net.httpserver.HttpServer;
import ru.akondratev.otp.config.ApplicationProperties;

import java.net.InetSocketAddress;

public class Application {
    public static void main(String[] args) {
        try {
            ApplicationProperties properties = new ApplicationProperties();
            AppComponents components = new AppComponents(properties);

            int port = properties.getInt("app.port");

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/auth/register", components.authController());
            server.createContext("/auth/login", components.authController());
            server.createContext("/auth/logout", components.authController());
            server.createContext("/users/me", components.userController());
            server.createContext("/admin/users", components.userController());
            server.createContext("/otp/generate", components.otpController());
            server.createContext("/otp/validate", components.otpController());
            server.createContext("/admin/otp-config", components.otpAdminController());
            server.setExecutor(null);
            server.start();

            components.otpExpirationScheduler().start();

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