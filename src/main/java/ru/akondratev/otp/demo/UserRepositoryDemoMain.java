package ru.akondratev.otp.demo;

import ru.akondratev.otp.config.ApplicationProperties;
import ru.akondratev.otp.config.DatabaseConfig;
import ru.akondratev.otp.user.model.UserAuthData;
import ru.akondratev.otp.user.model.UserResponse;
import ru.akondratev.otp.user.repository.UserRepository;
import ru.akondratev.otp.user.util.PasswordUtil;

import javax.sql.DataSource;
import java.util.List;

public class UserRepositoryDemoMain {

    public static void main(String[] args) {
        try {
            ApplicationProperties properties = new ApplicationProperties();
            DatabaseConfig databaseConfig = new DatabaseConfig(properties);
            DataSource dataSource = databaseConfig.dataSource();

            UserRepository userRepository = new UserRepository(dataSource);

            String login = "test_user_1";
            String passwordHash = PasswordUtil.hashPassword("password123");

            if (!userRepository.existsByLogin(login)) {
                long createdUserId = userRepository.createUser(login, passwordHash);
                System.out.println("Пользователь создан, id: " + createdUserId);
            } else {
                System.out.println("Пользователь с таким логином уже существует");
            }

            UserAuthData userAuthData = userRepository.findByLogin(login);
            if (userAuthData != null) {
                System.out.println("findByLogin: id = " + userAuthData.getId()
                        + ", login = " + userAuthData.getLogin()
                        + ", role = " + userAuthData.getRole());

                UserResponse userResponse = userRepository.findById(userAuthData.getId());
                if (userResponse != null) {
                    System.out.println("findById: id = " +  userResponse.getId()
                            + ", login = " + userResponse.getLogin()
                            + ", role = " + userResponse.getRole());
                }
            }

            List<UserResponse> users = userRepository.findAllNonAdminUsers();
            System.out.println("Все пользователи (кроме админов):");
            for (UserResponse user : users) {
                System.out.println(user.getId() + " | " + user.getLogin() + " | " + user.getRole());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}