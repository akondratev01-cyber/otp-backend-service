package ru.akondratev.otp.demo;

import ru.akondratev.otp.auth.service.AuthService;
import ru.akondratev.otp.auth.service.TokenService;
import ru.akondratev.otp.config.ApplicationProperties;
import ru.akondratev.otp.config.DatabaseConfig;
import ru.akondratev.otp.user.dto.LoginRequest;
import ru.akondratev.otp.user.dto.RegisterRequest;
import ru.akondratev.otp.user.repository.UserRepository;

import javax.sql.DataSource;

public class AuthServiceDemoMain {

    public static void main(String[] args) {
        try {
            ApplicationProperties properties = new ApplicationProperties();
            DatabaseConfig databaseConfig = new DatabaseConfig(properties);
            DataSource dataSource = databaseConfig.dataSource();

            UserRepository userRepository = new UserRepository(dataSource);
            TokenService tokenService = new TokenService(properties);
            AuthService authService = new AuthService(userRepository, tokenService);

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setLogin("demo_auth_user");
            registerRequest.setPassword("Password123");

            try {
                long userId = authService.registerUser(registerRequest);
                System.out.println("Пользователь зарегистрирован, id: " + userId);
            } catch (IllegalArgumentException e) {
                System.out.println("Регистрация: " + e.getMessage());
            }

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setLogin("demo_auth_user");
            loginRequest.setPassword("Password123");

            String token = authService.login(loginRequest);
            System.out.println("Токен после логина: " + token);

            Long userIdByToken = tokenService.getUserIdByToken(token);
            System.out.println("User id по токену: " + userIdByToken);

            authService.logout(token);
            System.out.println("После logout userId по токену: " + tokenService.getUserIdByToken(token));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}