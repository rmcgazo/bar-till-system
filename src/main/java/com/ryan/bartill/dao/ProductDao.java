package com.ryan.bartill.dao;

import com.ryan.bartill.model.Product;
import com.ryan.bartill.model.StockMovement;
import com.ryan.bartill.util.Db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    // -----------------------------
    // Stock movements
    // -----------------------------
    public List<StockMovement> listMovementsForProduct(int productId, int limit) throws Exception {
        String sql = """
            SELECT
              sm.created_at,
              COALESCE(st.username, '') AS staff_username,
              sm.reason,
              sm.qty_change
            FROM stock_movements sm
            LEFT JOIN staff st ON st.id = sm.staff_id
            WHERE sm.product_id = ?
            ORDER BY sm.id DESC
            LIMIT ?
        """;

        List<StockMovement> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new StockMovement(
                            rs.getString("created_at"),
                            rs.getString("staff_username"),
                            rs.getString("reason"),
                            rs.getInt("qty_change")
                    ));
                }
            }
        }

        return out;
    }

    private void logStockMovement(Connection conn, int productId, Integer staffId, int qtyChange, String reason) throws Exception {
        String sql = """
            INSERT INTO stock_movements(product_id, staff_id, qty_change, reason)
            VALUES(?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);

            if (staffId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, staffId);
            }

            ps.setInt(3, qtyChange);
            ps.setString(4, reason);
            ps.executeUpdate();
        }
    }

    public void logSaleMovement(Connection conn, int productId, int staffId, int qty) throws Exception {
        logStockMovement(conn, productId, staffId, -qty, "SALE");
    }

    // -----------------------------
    // Legacy stock update helpers
    // -----------------------------
    public void decrementStock(Connection conn, int productId, int qty) throws Exception {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, productId);
            ps.setInt(3, qty);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException("Not enough stock for product id " + productId);
            }
        }
    }

    // -----------------------------
    // Product mapping helper
    // -----------------------------
    private Product mapProduct(ResultSet rs) throws Exception {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("barcode"),
                rs.getInt("price_ex_cents"),
                rs.getInt("cost_price_cents"),
                rs.getInt("vat_rate"),
                rs.getInt("stock_qty"),
                rs.getInt("active") == 1,
                (Integer) rs.getObject("category_id"),
                rs.getString("category_name"),
                (Integer) rs.getObject("default_stock_item_id"),
                (Integer) rs.getObject("default_qty_used")
        );
    }

    public void setDefaultStockLink(int productId, Integer stockItemId, Integer qtyUsed) throws Exception {
        String sql = """
            UPDATE products
            SET default_stock_item_id = ?, default_qty_used = ?
            WHERE id = ?
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (stockItemId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, stockItemId);
            }

            if (qtyUsed == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, qtyUsed);
            }

            ps.setInt(3, productId);
            ps.executeUpdate();
        }
    }

    // -----------------------------
    // Find / list (WITH category JOIN)
    // -----------------------------
    public Product findByNameOrBarcode(String query) throws Exception {
        String sql = """
            SELECT
              p.id,
              p.name,
              p.barcode,
              p.price_ex_cents,
              COALESCE(p.cost_price_cents, 0) AS cost_price_cents,
              p.vat_rate,
              p.stock_qty,
              p.active,
              p.category_id,
              c.name AS category_name,
              p.default_stock_item_id,
              p.default_qty_used
            FROM products p
            LEFT JOIN product_categories c ON c.id = p.category_id
            WHERE p.active = 1
              AND (LOWER(p.name) = LOWER(?) OR p.barcode = ?)
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, query);
            ps.setString(2, query);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapProduct(rs);
            }
        }
    }

    public Product findById(int id) throws Exception {
        String sql = """
            SELECT
              p.id,
              p.name,
              p.barcode,
              p.price_ex_cents,
              COALESCE(p.cost_price_cents, 0) AS cost_price_cents,
              p.vat_rate,
              p.stock_qty,
              p.active,
              p.category_id,
              c.name AS category_name,
              p.default_stock_item_id,
              p.default_qty_used
            FROM products p
            LEFT JOIN product_categories c ON c.id = p.category_id
            WHERE p.id = ?
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return mapProduct(rs);
            }
        }
    }

    public Integer findIdByName(String name) throws Exception {
        String sql = "SELECT id FROM product_categories WHERE LOWER(name) = LOWER(?)";

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
                return null;
            }
        }
    }

    public int findOrCreateIdByName(Connection conn, String name) throws Exception {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Category name blank");
        }

        String findSql = "SELECT id FROM product_categories WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSql = "INSERT INTO product_categories(name, color_hex) VALUES(?, '#4B7BEC')";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(findSql)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Could not create/find category");
                }
                return rs.getInt("id");
            }
        }
    }

    public List<Product> listAllActive() throws Exception {
        String sql = """
            SELECT
              p.id,
              p.name,
              p.barcode,
              p.price_ex_cents,
              COALESCE(p.cost_price_cents, 0) AS cost_price_cents,
              p.vat_rate,
              p.stock_qty,
              p.active,
              p.category_id,
              c.name AS category_name,
              p.default_stock_item_id,
              p.default_qty_used
            FROM products p
            LEFT JOIN product_categories c ON c.id = p.category_id
            WHERE p.active = 1
            ORDER BY p.name
        """;

        List<Product> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(mapProduct(rs));
            }
        }

        return out;
    }

    public List<Product> listByCategory(int categoryId) throws Exception {
        String sql = """
            SELECT
              p.id,
              p.name,
              p.barcode,
              p.price_ex_cents,
              COALESCE(p.cost_price_cents, 0) AS cost_price_cents,
              p.vat_rate,
              p.stock_qty,
              p.active,
              p.category_id,
              c.name AS category_name,
              p.default_stock_item_id,
              p.default_qty_used
            FROM products p
            LEFT JOIN product_categories c ON c.id = p.category_id
            WHERE p.active = 1
              AND p.category_id = ?
            ORDER BY p.name
        """;

        List<Product> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapProduct(rs));
                }
            }
        }

        return out;
    }

    // -----------------------------
    // Legacy product stock actions
    // -----------------------------
    public void addStock(int productId, int qty, int staffId) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ?")) {
                    ps.setInt(1, qty);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                logStockMovement(conn, productId, staffId, qty, "RESTOCK");
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void removeStock(int productId, int qty, int staffId) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?")) {
                    ps.setInt(1, qty);
                    ps.setInt(2, productId);
                    ps.setInt(3, qty);

                    if (ps.executeUpdate() == 0) {
                        throw new IllegalStateException("Not enough stock");
                    }
                }

                logStockMovement(conn, productId, staffId, -qty, "WASTE");
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void recordDelivery(int productId, int qty, int staffId) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET stock_qty = stock_qty + ? WHERE id = ?")) {
                    ps.setInt(1, qty);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }

                logStockMovement(conn, productId, staffId, qty, "DELIVERY");
                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // -----------------------------
    // Create product
    // -----------------------------
    public void createProduct(String name, String barcodeOrNull,
                              int priceExCents, int costPriceCents,
                              int vatRate) throws Exception {

        String sql = """
        INSERT INTO products(name, barcode, price_ex_cents, cost_price_cents, vat_rate, stock_qty, active)
        VALUES(?,?,?,?,?,0,1)
    """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.trim());

            if (barcodeOrNull == null || barcodeOrNull.trim().isEmpty()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, barcodeOrNull.trim());
            }

            ps.setInt(3, priceExCents);
            ps.setInt(4, costPriceCents);
            ps.setInt(5, vatRate);

            ps.executeUpdate();
        }
    }

    // -----------------------------
    // CSV import
    // -----------------------------
    public int importFromCsv(Path csvPath) throws Exception {
        int imported = 0;

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try (var lines = Files.lines(csvPath)) {
                var it = lines.iterator();
                if (!it.hasNext()) {
                    return 0;
                }

                it.next(); // skip header

                while (it.hasNext()) {
                    String line = it.next().trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split(",", -1);

                    if (parts.length != 7 && parts.length != 8) {
                        throw new IllegalArgumentException("Bad line: " + line);
                    }

                    String name = parts[0].trim();
                    String barcode = parts[1].trim();
                    String priceEx = parts[2].trim();

                    String costPrice;
                    String vat;
                    String stock;
                    String active;
                    String categoryName;

                    if (parts.length == 8) {
                        costPrice = parts[3].trim();
                        vat = parts[4].trim();
                        stock = parts[5].trim();
                        active = parts[6].trim();
                        categoryName = parts[7].trim();
                    } else {
                        costPrice = "0";
                        vat = parts[3].trim();
                        stock = parts[4].trim();
                        active = parts[5].trim();
                        categoryName = parts[6].trim();
                    }

                    if (name.isEmpty()) {
                        continue;
                    }

                    int priceCents = parseMoneyToCents(priceEx);
                    int costPriceCents = costPrice.isEmpty() ? 0 : parseMoneyToCents(costPrice);
                    int vatRate = Integer.parseInt(vat);
                    int stockQty = Integer.parseInt(stock);
                    int activeInt = parseActive(active);

                    Integer categoryId = null;
                    if (!categoryName.isEmpty()) {
                        categoryId = findOrCreateIdByName(conn, categoryName);
                    }

                    if (!barcode.isEmpty()) {
                        imported += upsertByBarcode(
                                conn, name, barcode, priceCents, costPriceCents,
                                vatRate, stockQty, activeInt, categoryId
                        );
                    } else {
                        imported += upsertByName(
                                conn, name, priceCents, costPriceCents,
                                vatRate, stockQty, activeInt, categoryId
                        );
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

            conn.commit();
        }

        return imported;
    }

    private int upsertByBarcode(Connection conn, String name, String barcode,
                                int priceCents, int costPriceCents,
                                int vatRate, int stockQty, int active,
                                Integer categoryId) throws Exception {

        String update = """
            UPDATE products
            SET name = ?, price_ex_cents = ?, cost_price_cents = ?, vat_rate = ?, stock_qty = ?, active = ?, category_id = ?
            WHERE barcode = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, name);
            ps.setInt(2, priceCents);
            ps.setInt(3, costPriceCents);
            ps.setInt(4, vatRate);
            ps.setInt(5, stockQty);
            ps.setInt(6, active);

            if (categoryId == null) {
                ps.setNull(7, Types.INTEGER);
            } else {
                ps.setInt(7, categoryId);
            }

            ps.setString(8, barcode);

            int changed = ps.executeUpdate();
            if (changed > 0) {
                return 1;
            }
        }

        String insert = """
            INSERT INTO products(name, barcode, price_ex_cents, cost_price_cents, vat_rate, stock_qty, active, category_id)
            VALUES(?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, name);
            ps.setString(2, barcode);
            ps.setInt(3, priceCents);
            ps.setInt(4, costPriceCents);
            ps.setInt(5, vatRate);
            ps.setInt(6, stockQty);
            ps.setInt(7, active);

            if (categoryId == null) {
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(8, categoryId);
            }

            ps.executeUpdate();
            return 1;
        }
    }

    private int upsertByName(Connection conn, String name,
                             int priceCents, int costPriceCents,
                             int vatRate, int stockQty, int active,
                             Integer categoryId) throws Exception {

        String update = """
            UPDATE products
            SET price_ex_cents = ?, cost_price_cents = ?, vat_rate = ?, stock_qty = ?, active = ?, category_id = ?
            WHERE LOWER(name) = LOWER(?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setInt(1, priceCents);
            ps.setInt(2, costPriceCents);
            ps.setInt(3, vatRate);
            ps.setInt(4, stockQty);
            ps.setInt(5, active);

            if (categoryId == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, categoryId);
            }

            ps.setString(7, name);

            int changed = ps.executeUpdate();
            if (changed > 0) {
                return 1;
            }
        }

        String insert = """
            INSERT INTO products(name, barcode, price_ex_cents, cost_price_cents, vat_rate, stock_qty, active, category_id)
            VALUES(?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, name);
            ps.setNull(2, Types.VARCHAR);
            ps.setInt(3, priceCents);
            ps.setInt(4, costPriceCents);
            ps.setInt(5, vatRate);
            ps.setInt(6, stockQty);
            ps.setInt(7, active);

            if (categoryId == null) {
                ps.setNull(8, Types.INTEGER);
            } else {
                ps.setInt(8, categoryId);
            }

            ps.executeUpdate();
            return 1;
        }
    }

    private static int parseMoneyToCents(String text) {
        String t = text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Money value is blank");
        }

        if (!t.contains(".")) {
            return Integer.parseInt(t) * 100;
        }

        String[] parts = t.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Bad money value: " + text);
        }

        int euros = Integer.parseInt(parts[0]);
        String centsPart = parts[1];

        if (centsPart.length() == 1) {
            centsPart = centsPart + "0";
        }
        if (centsPart.length() != 2) {
            throw new IllegalArgumentException("Bad money value: " + text);
        }

        int cents = Integer.parseInt(centsPart);
        return euros * 100 + cents;
    }

    private static int parseActive(String text) {
        String t = text.trim().toLowerCase();

        if (t.equals("1") || t.equals("true") || t.equals("yes") || t.equals("y")) {
            return 1;
        }
        if (t.equals("0") || t.equals("false") || t.equals("no") || t.equals("n")) {
            return 0;
        }

        return Integer.parseInt(text.trim());
    }

    // -----------------------------
    // Category assignment
    // -----------------------------
    public void setCategory(int productId, Integer categoryId) throws Exception {
        String sql = "UPDATE products SET category_id = ? WHERE id = ?";

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (categoryId == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, categoryId);
            }

            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public String getCategoryNameForProduct(int productId) throws Exception {
        String sql = """
            SELECT c.name
            FROM products p
            LEFT JOIN product_categories c ON c.id = p.category_id
            WHERE p.id = ?
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "";
                }
                String name = rs.getString("name");
                return name == null ? "" : name;
            }
        }
    }

    public List<com.ryan.bartill.model.ProductStockLinkRow> listProductStockLinks() throws Exception {
        String sql = """
        SELECT
            p.id,
            p.name AS product_name,
            c.name AS category_name,
            si.name AS stock_item_name,
            p.default_qty_used
        FROM products p
        LEFT JOIN product_categories c ON c.id = p.category_id
        LEFT JOIN stock_items si ON si.id = p.default_stock_item_id
        WHERE p.active = 1
        ORDER BY p.name
    """;

        List<com.ryan.bartill.model.ProductStockLinkRow> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new com.ryan.bartill.model.ProductStockLinkRow(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getString("stock_item_name"),
                        (Integer) rs.getObject("default_qty_used")
                ));
            }
        }

        return out;
    }

    public int importDefaultStockLinksFromCsv(java.nio.file.Path csvPath) throws Exception {
        int imported = 0;
        List<String> skipped = new ArrayList<>();

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try (var lines = java.nio.file.Files.lines(csvPath)) {
                var it = lines.iterator();

                if (!it.hasNext()) {
                    return 0;
                }

                it.next(); // skip header

                while (it.hasNext()) {
                    String line = it.next().trim();

                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) {
                        skipped.add("Bad line: " + line);
                        continue;
                    }

                    String productName = parts[0].trim();
                    String stockItemName = parts[1].trim();
                    String qtyText = parts[2].trim();

                    if (productName.isEmpty() || stockItemName.isEmpty() || qtyText.isEmpty()) {
                        skipped.add("Blank value in line: " + line);
                        continue;
                    }

                    Integer productId = findProductIdByName(conn, productName);
                    if (productId == null) {
                        skipped.add("Product not found: " + productName);
                        continue;
                    }

                    Integer stockItemId = findStockItemIdByName(conn, stockItemName);
                    if (stockItemId == null) {
                        skipped.add("Stock item not found: " + stockItemName);
                        continue;
                    }

                    int qtyUsed;
                    try {
                        qtyUsed = Integer.parseInt(qtyText);
                        if (qtyUsed <= 0) {
                            skipped.add("Qty must be > 0 for product: " + productName);
                            continue;
                        }
                    } catch (Exception e) {
                        skipped.add("Bad qty for product: " + productName + " (" + qtyText + ")");
                        continue;
                    }

                    try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE products
                    SET default_stock_item_id = ?, default_qty_used = ?
                    WHERE id = ?
                """)) {
                        ps.setInt(1, stockItemId);
                        ps.setInt(2, qtyUsed);
                        ps.setInt(3, productId);
                        ps.executeUpdate();
                    }

                    imported++;
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }

        if (!skipped.isEmpty()) {
            System.out.println("----- STOCK LINK IMPORT SKIPPED ROWS -----");
            for (String s : skipped) {
                System.out.println(s);
            }
            System.out.println("------------------------------------------");
        }

        return imported;
    }

    private Integer findProductIdByName(Connection conn, String productName) throws Exception {
        String sql = "SELECT id FROM products WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }

    private Integer findStockItemIdByName(Connection conn, String stockItemName) throws Exception {
        String sql = "SELECT id FROM stock_items WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stockItemName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }
}