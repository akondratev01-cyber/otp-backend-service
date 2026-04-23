package ru.akondratev.otp.otp.repository;

import ru.akondratev.otp.otp.model.OtpConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class OtpConfigRepository {

    private final DataSource dataSource;

    public OtpConfigRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public OtpConfig getCurrentConfig() throws SQLException {
        String sql = """
                SELECT id, code_length, ttl_seconds, updated_at
                FROM otp_config
                WHERE id = 1
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return new OtpConfig(
                        resultSet.getInt("id"),
                        resultSet.getInt("code_length"),
                        resultSet.getInt("ttl_seconds"),
                        resultSet.getTimestamp("updated_at").toLocalDateTime()
                );
            }

            return null;
        }
    }

    public boolean updateConfig(int codeLength, int ttlSeconds) throws SQLException {
        String sql = """
                UPDATE otp_config
                SET code_length = ?, ttl_seconds = ?, updated_at = NOW()
                WHERE id = 1
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, codeLength);
            statement.setInt(2, ttlSeconds);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }
}
