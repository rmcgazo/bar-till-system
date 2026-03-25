package com.ryan.bartill.app;

import com.ryan.bartill.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SeedProducts {

    public static void main(String[] args) throws Exception {
        String sql = """
            INSERT OR IGNORE INTO products
            (name, barcode, price_ex_cents, vat_rate, stock_qty, active)
            VALUES (?, ?, ?, ?, ?, 1)
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            insert(ps, "Guinness Pint", "111", 550, 23, 50);
            insert(ps, "Heineken Pint", "222", 530, 23, 40);
            insert(ps, "Coke", "333", 250, 23, 100);
        }

        System.out.println("Products seeded");
    }

    private static void insert(PreparedStatement ps,
                               String name, String barcode,
                               int price, int vat, int stock) throws Exception {
        ps.setString(1, name);
        ps.setString(2, barcode);
        ps.setInt(3, price);
        ps.setInt(4, vat);
        ps.setInt(5, stock);
        ps.executeUpdate();
    }
}