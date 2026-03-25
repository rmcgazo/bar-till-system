package com.ryan.bartill.dao;

import com.ryan.bartill.model.QuickButton;
import com.ryan.bartill.util.Db;

import java.util.ArrayList;
import java.util.List;

public class QuickButtonDao {

    public List<QuickButton> listByPage(int page) throws Exception {
        String sql = """
            SELECT id, page, position, type, product_id, category_id, label, color_hex
            FROM quick_buttons
            WHERE page = ?
            ORDER BY position
        """;
        List<QuickButton> out = new ArrayList<>();
        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, page);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new QuickButton(
                            rs.getInt("id"),
                            rs.getInt("page"),
                            rs.getInt("position"),
                            rs.getString("type"),
                            (Integer) rs.getObject("product_id"),
                            (Integer) rs.getObject("category_id"),
                            rs.getString("label"),
                            rs.getString("color_hex")
                    ));
                }
            }
        }
        return out;
    }

    public void upsertButton(int page, int position, String type,
                             Integer productId, Integer categoryId,
                             String label, String colorHex) throws Exception {

        String sql = """
        INSERT INTO quick_buttons(page, position, type, product_id, category_id, label, color_hex)
        VALUES(?,?,?,?,?,?,?)
        ON CONFLICT(page, position) DO UPDATE SET
          type=excluded.type,
          product_id=excluded.product_id,
          category_id=excluded.category_id,
          label=excluded.label,
          color_hex=excluded.color_hex
    """;

        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, page);
            ps.setInt(2, position);
            ps.setString(3, type);


            if (productId == null) ps.setNull(4, java.sql.Types.INTEGER);
            else ps.setInt(4, productId);

            if (categoryId == null) ps.setNull(5, java.sql.Types.INTEGER);
            else ps.setInt(5, categoryId);

            if (label == null || label.isBlank()) ps.setNull(6, java.sql.Types.VARCHAR);
            else ps.setString(6, label);

            if (colorHex == null || colorHex.isBlank()) ps.setNull(7, java.sql.Types.VARCHAR);
            else ps.setString(7, colorHex);

            ps.executeUpdate();
        }
    }

    public void deleteButton(int page, int position) throws Exception {
        String sql = "DELETE FROM quick_buttons WHERE page = ? AND position = ?";
        try (var conn = Db.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, page);
            ps.setInt(2, position);
            ps.executeUpdate();
        }
    }

    public String getCategoryName(int categoryId) throws Exception {
        String sql = "SELECT name FROM product_categories WHERE id = ?";

        try (var conn = com.ryan.bartill.util.Db.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return "";
                String name = rs.getString("name");
                return name == null ? "" : name;
            }
        }
    }
}