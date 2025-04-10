package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.model.OtpCode;
import org.example.util.DbConnection;

public class OtpDao {

    private Connection connection;

    public OtpDao() {
        try {
            connection = DbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<OtpCode> findAllOtpCodes() {
        List<OtpCode> otpCodes = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM otp_codes");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OtpCode otpCode = new OtpCode(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getLong("operation_id"),
                        rs.getString("code"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("expires_at") != null ? rs.getTimestamp("expires_at").toLocalDateTime() : null,
                        rs.getString("description_operation")
                );
                otpCodes.add(otpCode);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return otpCodes;
    }


    public boolean saveOtpCode(OtpCode otpCode) {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO otp_codes(user_id, operation_id, otp_code, status, created_at, expires_at, description_operation) VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmt.setLong(1, otpCode.getUser_id());
            stmt.setLong(2, otpCode.getOperationId());
            stmt.setString(3, otpCode.getCode());
            stmt.setString(4, otpCode.getStatus());
            stmt.setObject(5, otpCode.getCreationTime());
            stmt.setObject(6, otpCode.getExpirationTime());
            stmt.setString(7, otpCode.getDescription_operation());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateOtpCode(OtpCode otpCode) {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE otp_codes SET status = ?, expires_at = ? WHERE id = ?");
            stmt.setString(1, otpCode.getStatus().toString());
            stmt.setObject(2, otpCode.getExpirationTime());
            stmt.setLong(3, otpCode.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteOtpCode(long id) {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM otp_codes WHERE id = ?");
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
