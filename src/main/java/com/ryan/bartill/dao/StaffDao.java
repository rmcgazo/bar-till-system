package com.ryan.bartill.dao;

import com.ryan.bartill.model.Role;
import com.ryan.bartill.model.Staff;
import com.ryan.bartill.util.Db;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StaffDao {

    public Staff findByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password_hash, role, active FROM staff WHERE username = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Staff(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        Role.valueOf(rs.getString("role")),
                        rs.getInt("active") == 1
                );
            }
        }
    }

    public void createStaff(String username, String rawPassword, com.ryan.bartill.model.Role role, String pin) throws Exception {
        if (pin == null) pin = "";
        pin = pin.trim();
        if (!pin.matches("\\d{2}")) {
            throw new IllegalArgumentException("PIN must be 2 digits");
        }

        String sql = """
        INSERT INTO staff(username, password_hash, role, active, pin_code)
        VALUES(?, ?, ?, 1, ?)
    """;

        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(rawPassword, org.mindrot.jbcrypt.BCrypt.gensalt());

        try (var conn = com.ryan.bartill.util.Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, hash);
            ps.setString(3, role.name());
            ps.setString(4, pin);

            ps.executeUpdate();
        }
    }

    public void logLoginAttempt(Integer staffIdOrNull, String usernameAttempted, boolean success) throws Exception {
        String sql = "INSERT INTO login_audit(staff_id, username_attempted, success) VALUES(?,?,?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (staffIdOrNull == null) ps.setNull(1, java.sql.Types.INTEGER);
            else ps.setInt(1, staffIdOrNull);

            ps.setString(2, usernameAttempted);
            ps.setInt(3, success ? 1 : 0);
            ps.executeUpdate();
        }
    }


    public Staff findByPin(String pin) throws Exception {
        String sql = """
        SELECT id, username, password_hash, role, active, pin_code
        FROM staff
        WHERE pin_code = ? LIMIT 1
    """;

        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, pin);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Staff(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        com.ryan.bartill.model.Role.valueOf(rs.getString("role")),
                        rs.getInt("active") == 1
                        // if your Staff model has pin_code, add it; otherwise ignore
                );
            }
        }
    }

    public void setPin(int staffId, String pin) throws Exception {
        String sql = "UPDATE staff SET pin_code = ? WHERE id = ?";
        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, pin);
            ps.setInt(2, staffId);
            ps.executeUpdate();
        }
    }


}