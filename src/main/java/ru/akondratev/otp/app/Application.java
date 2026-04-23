package ru.akondratev.otp.app;

import com.sun.net.httpserver.HttpServer;
import ru.akondratev.otp.config.ApplicationProperties;

import java.net.InetSocketAddress;

public class Application {
    public static void main(String[] args) {
        try {
            StartupPrinter.printBanner();
            StartupPrinter.printStep(1, 4, "Loading application properties...");
            ApplicationProperties properties = new ApplicationProperties();

            StartupPrinter.printStep(2, 4, "Initializing application components...");
            AppComponents components = new AppComponents(properties);

            int port = properties.getInt("app.port");

            StartupPrinter.printStep(3, 4, "Registering HTTP routes and starting server...");
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/auth/register", components.authController());
            server.createContext("/auth/login", components.authController());
            server.createContext("/auth/logout", components.authController());
            server.createContext("/users/me", components.userController());
            server.createContext("/admin/users", components.adminUserController());
            server.createContext("/otp/generate", components.otpController());
            server.createContext("/otp/validate", components.otpController());
            server.createContext("/admin/otp-config", components.adminOtpConfigController());
            server.setExecutor(null);
            server.start();

            StartupPrinter.printStep(4, 4, "Starting OTP expiration scheduler...");
            components.otpExpirationScheduler().start();

            StartupPrinter.printSummary(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}