package ru.akondratev.otp.user.repository;

import ru.akondratev.otp.user.model.UserAuthData;
import ru.akondratev.otp.user.model.UserResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean existsByLogin(String login) throws SQLException {
        String sql = """
                SELECT 1
                FROM users
                WHERE login = ?
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean adminExists() throws SQLException {
        String sql = """
                SELECT 1
                FROM users
                WHERE role = 'ADMIN'
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next();
        }
    }

    public long createUser(String login, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users (login, password_hash, role)
                VALUES (?, ?, 'USER')
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, passwordHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
                throw new SQLException("Не удалось создать пользователя");
            }
        }
    }

    public long createAdmin(String login, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users (login, password_hash, role)
                VALUES (?, ?, 'ADMIN')
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, passwordHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
                throw new SQLException("Не удалось создать администратора");
            }
        }
    }

    public UserAuthData findByLogin(String login) throws SQLException {
        String sql = """
                SELECT id, login, password_hash, role
                FROM users
                WHERE login = ?
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserAuthData(
                            resultSet.getLong("id"),
                            resultSet.getString("login"),
                            resultSet.getString("password_hash"),
                            resultSet.getString("role")
                    );
                }
                return null;
            }
        }
    }

    public UserResponse findById(long id) throws SQLException {
        String sql = """
                SELECT id, login, role
                FROM users
                WHERE id = ?
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserResponse(
                            resultSet.getLong("id"),
                            resultSet.getString("login"),
                            resultSet.getString("role")
                    );
                }
                return null;
            }
        }
    }

    public List<UserResponse> findAllNonAdminUsers() throws SQLException {
        String sql = """
                SELECT id, login, role
                FROM users
                WHERE role <> 'ADMIN'
                ORDER BY id
                """;

        List<UserResponse> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(new UserResponse(
                        resultSet.getLong("id"),
                        resultSet.getString("login"),
                        resultSet.getString("role")
                ));
            }
        }

        return users;
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = """
                DELETE FROM users
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }
}
