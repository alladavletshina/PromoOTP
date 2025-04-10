package org.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.model.Operation;
import org.example.util.DbConnection;

public class OperationDao {

    private Connection connection;

    public OperationDao() {
        try {
            connection = DbConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Operation> findAllOperations() {
        List<Operation> operations = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM operations");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Operation operation = new Operation(
                        rs.getLong("id"),
                        rs.getString("description"),
                        rs.getLong("user_id")
                );
                operations.add(operation);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return operations;
    }

    public Operation findOperationById(long id) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM operations WHERE id = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Operation(
                        rs.getLong("id"),
                        rs.getString("description"),
                        rs.getLong("user_id")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean saveOperation(Operation operation) {
        try {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO operations(description, user_id) VALUES (?, ?)");
            stmt.setString(2, operation.getDescription());
            stmt.setLong(3, operation.getUserId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateOperation(Operation operation) {
        try {
            PreparedStatement stmt = connection.prepareStatement("UPDATE operations SET description = ? WHERE id = ?");
            stmt.setString(1, operation.getDescription());
            stmt.setLong(2, operation.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteOperation(long id) {
        try {
            PreparedStatement stmt = connection.prepareStatement("DELETE FROM operations WHERE id = ?");
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}