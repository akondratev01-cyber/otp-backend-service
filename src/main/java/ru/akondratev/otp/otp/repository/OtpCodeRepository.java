package ru.akondratev.otp.otp.repository;

import ru.akondratev.otp.otp.model.OtpChannel;
import ru.akondratev.otp.otp.model.OtpCode;
import ru.akondratev.otp.otp.model.OtpStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class OtpCodeRepository {

    private final DataSource dataSource;

    public OtpCodeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long createOtpCode(
            long userId,
            String operationId,
            String code,
            OtpStatus status,
            OtpChannel channel,
            LocalDateTime expiresAt
    ) throws SQLException {
        String sql = """
                INSERT INTO otp_codes (
                   user_id,
                   operation_id,
                   code,
                   status,
                   channel,
                   expires_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);
            statement.setString(3, code);
            statement.setString(4, status.name());
            statement.setString(5, channel.name());
            statement.setTimestamp(6, java.sql.Timestamp.valueOf(expiresAt));

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }
                throw new SQLException("Не удалось создать OTP-код");
            }
        }
    }

    public OtpCode findActiveCode(long userId, String operationId, String code) throws SQLException {
        String sql = """
                SELECT id, user_id, operation_id, code, status, channel, created_at, expires_at, used_at
                FROM otp_codes
                WHERE user_id = ?
                    AND operation_id = ?
                    AND code = ?
                    AND status = 'ACTIVE'
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);
            statement.setString(3, code);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToOtpCode(resultSet);
                }
                return null;
            }
        }
    }

    public OtpCode findActiveCodeByOperation(long userId, String operationId) throws SQLException {
        String sql = """
                SELECT id, user_id, operation_id, code, status, channel, created_at, expires_at, used_at
                FROM otp_codes
                WHERE user_id = ?
                    AND operation_id = ?
                    AND status = 'ACTIVE'
                ORDER BY id DESC
                LIMIT 1
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setString(2, operationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRowToOtpCode(resultSet);
                }
                return null;
            }
        }
    }

    public boolean markAsUsed(long otpId) throws SQLException {
        String sql = """
                UPDATE otp_codes
                SET status = 'USED', used_at = NOW()
                WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, otpId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    public int markExpiredCodes() throws SQLException {
        String sql = """
            UPDATE otp_codes
            SET status = 'EXPIRED'
            WHERE status = 'ACTIVE'
              AND expires_at < NOW()
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            return statement.executeUpdate();
        }
    }

    private OtpCode mapRowToOtpCode(ResultSet resultSet) throws SQLException {
        return new OtpCode(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("operation_id"),
                resultSet.getString("code"),
                OtpStatus.valueOf(resultSet.getString("status")),
                OtpChannel.valueOf(resultSet.getString("channel")),
                resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("expires_at").toLocalDateTime(),
                resultSet.getTimestamp("used_at") != null
                        ? resultSet.getTimestamp("used_at").toLocalDateTime()
                        : null
        );
    }
}
