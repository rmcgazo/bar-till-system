package com.ryan.bartill.dao;

import com.ryan.bartill.model.Category;
import com.ryan.bartill.util.Db;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public List<Category> listAll() throws Exception {
        String sql = "SELECT id, name, color_hex FROM product_categories ORDER BY name";
        List<Category> out = new ArrayList<>();
        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("color_hex")
                ));
            }
        }
        return out;
    }

    public void create(String name, String colorHexOrNull) throws Exception {
        String sql = """
            INSERT INTO product_categories(name, color_hex)
            VALUES(?, COALESCE(NULLIF(?, ''), '#4B7BEC'))
        """;

        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, colorHexOrNull == null ? "" : colorHexOrNull.trim());
            ps.executeUpdate();
        }
    }

    // --- NEW: used by CSV import (uses SAME connection as import transaction) ---
    public int findOrCreateIdByName(Connection conn, String name) throws Exception {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Category name blank");

        // 1) Try find
        String find = "SELECT id FROM product_categories WHERE LOWER(name)=LOWER(?)";
        try (var ps = conn.prepareStatement(find)) {
            ps.setString(1, trimmed);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }

        // 2) Create (default color)
        String insert = "INSERT INTO product_categories(name, color_hex) VALUES(?, '#4B7BEC')";
        try (var ps = conn.prepareStatement(insert)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
        }

        // 3) Read id back
        try (var ps = conn.prepareStatement(find)) {
            ps.setString(1, trimmed);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("id");
            }
        }
    }
}