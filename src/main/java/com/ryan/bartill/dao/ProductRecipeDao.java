package com.ryan.bartill.dao;

import com.ryan.bartill.model.RecipeRow;
import com.ryan.bartill.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductRecipeDao {

    public static class RecipeLine {
        private final int stockItemId;
        private final int qtyBaseUnitsUsed;

        public RecipeLine(int stockItemId, int qtyBaseUnitsUsed) {
            this.stockItemId = stockItemId;
            this.qtyBaseUnitsUsed = qtyBaseUnitsUsed;
        }

        public int getStockItemId() {
            return stockItemId;
        }

        public int getQtyBaseUnitsUsed() {
            return qtyBaseUnitsUsed;
        }
    }

    public List<RecipeLine> listRecipeForProduct(int productId) throws Exception {
        String sql = """
            SELECT stock_item_id, qty_base_units_used
            FROM product_recipe_items
            WHERE product_id = ?
            ORDER BY id
        """;

        List<RecipeLine> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new RecipeLine(
                            rs.getInt("stock_item_id"),
                            rs.getInt("qty_base_units_used")
                    ));
                }
            }
        }

        return out;
    }

    public List<RecipeRow> listAllRecipeRows() throws Exception {
        String sql = """
            SELECT
                p.name AS product_name,
                s.name AS stock_item_name,
                pri.qty_base_units_used
            FROM product_recipe_items pri
            JOIN products p ON p.id = pri.product_id
            JOIN stock_items s ON s.id = pri.stock_item_id
            ORDER BY p.name
        """;

        List<RecipeRow> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(new RecipeRow(
                        rs.getString("product_name"),
                        rs.getString("stock_item_name"),
                        rs.getInt("qty_base_units_used")
                ));
            }
        }

        return out;
    }

    public void upsertSingleRecipe(int productId, int stockItemId, int qtyBaseUnitsUsed) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM product_recipe_items WHERE product_id = ?")) {
                    del.setInt(1, productId);
                    del.executeUpdate();
                }

                try (PreparedStatement ins = conn.prepareStatement("""
                    INSERT INTO product_recipe_items(product_id, stock_item_id, qty_base_units_used)
                    VALUES(?,?,?)
                """)) {
                    ins.setInt(1, productId);
                    ins.setInt(2, stockItemId);
                    ins.setInt(3, qtyBaseUnitsUsed);
                    ins.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int getAvailableStockForProduct(int productId) throws Exception {
        try (Connection conn = Db.getConnection()) {

            // 1) First check full recipe rows
            String recipeSql = """
                SELECT
                    MIN(si.current_qty_base_units / pri.qty_base_units_used) AS available_units
                FROM product_recipe_items pri
                JOIN stock_items si ON si.id = pri.stock_item_id
                WHERE pri.product_id = ?
            """;

            try (PreparedStatement ps = conn.prepareStatement(recipeSql)) {
                ps.setInt(1, productId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Integer available = (Integer) rs.getObject("available_units");
                        if (available != null) {
                            return available;
                        }
                    }
                }
            }

            // 2) If no recipe, check default stock link
            String linkSql = """
                SELECT
                    si.current_qty_base_units,
                    p.default_qty_used
                FROM products p
                JOIN stock_items si ON si.id = p.default_stock_item_id
                WHERE p.id = ?
                  AND p.default_stock_item_id IS NOT NULL
                  AND p.default_qty_used IS NOT NULL
                  AND p.default_qty_used > 0
            """;

            try (PreparedStatement ps = conn.prepareStatement(linkSql)) {
                ps.setInt(1, productId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int currentQtyBaseUnits = rs.getInt("current_qty_base_units");
                        int defaultQtyUsed = rs.getInt("default_qty_used");

                        return currentQtyBaseUnits / defaultQtyUsed;
                    }
                }
            }

            return 0;
        }
    }

    public String getAvailableStockDisplayForProduct(int productId) throws Exception {
        int available = getAvailableStockForProduct(productId);
        return String.valueOf(available);
    }

    public int importRecipesFromCsv(java.nio.file.Path csvPath) throws Exception {
        int imported = 0;

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try (var lines = java.nio.file.Files.lines(csvPath)) {
                var it = lines.iterator();

                if (!it.hasNext()) return 0;
                it.next(); // skip header

                java.util.Map<Integer, java.util.List<RecipeLineToInsert>> grouped = new java.util.HashMap<>();

                while (it.hasNext()) {
                    String line = it.next().trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) {
                        throw new IllegalArgumentException("Bad CSV line: " + line);
                    }

                    String productName = parts[0].trim();
                    String stockItemName = parts[1].trim();
                    String qtyText = parts[2].trim();

                    if (productName.isEmpty() || stockItemName.isEmpty() || qtyText.isEmpty()) {
                        throw new IllegalArgumentException("Blank value in line: " + line);
                    }

                    int qtyBaseUnitsUsed = Integer.parseInt(qtyText);
                    if (qtyBaseUnitsUsed <= 0) {
                        throw new IllegalArgumentException("Qty must be > 0 in line: " + line);
                    }

                    Integer productId = findProductIdByName(conn, productName);
                    Integer stockItemId = findStockItemIdByName(conn, stockItemName);

                    if (productId == null) {
                        throw new IllegalArgumentException("Product not found: " + productName);
                    }
                    if (stockItemId == null) {
                        throw new IllegalArgumentException("Stock item not found: " + stockItemName);
                    }

                    grouped.computeIfAbsent(productId, k -> new java.util.ArrayList<>())
                            .add(new RecipeLineToInsert(stockItemId, qtyBaseUnitsUsed));
                }

                for (var entry : grouped.entrySet()) {
                    int productId = entry.getKey();
                    var recipeLines = entry.getValue();

                    try (PreparedStatement del = conn.prepareStatement(
                            "DELETE FROM product_recipe_items WHERE product_id = ?")) {
                        del.setInt(1, productId);
                        del.executeUpdate();
                    }

                    try (PreparedStatement ins = conn.prepareStatement("""
                    INSERT INTO product_recipe_items(product_id, stock_item_id, qty_base_units_used)
                    VALUES(?,?,?)
                """)) {
                        for (RecipeLineToInsert r : recipeLines) {
                            ins.setInt(1, productId);
                            ins.setInt(2, r.stockItemId);
                            ins.setInt(3, r.qtyBaseUnitsUsed);
                            ins.addBatch();
                            imported++;
                        }
                        ins.executeBatch();
                    }
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }

        return imported;
    }

    private static class RecipeLineToInsert {
        final int stockItemId;
        final int qtyBaseUnitsUsed;

        RecipeLineToInsert(int stockItemId, int qtyBaseUnitsUsed) {
            this.stockItemId = stockItemId;
            this.qtyBaseUnitsUsed = qtyBaseUnitsUsed;
        }
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

    public int getCalculatedCostPerSaleCents(int productId) throws Exception {
        int totalCost = 0;
        boolean foundRecipe = false;

        try (Connection conn = Db.getConnection()) {

            String recipeSql = """
            SELECT
                pri.qty_base_units_used,
                si.cost_price_cents_per_stock_unit,
                si.stock_unit_size
            FROM product_recipe_items pri
            JOIN stock_items si ON si.id = pri.stock_item_id
            WHERE pri.product_id = ?
        """;

            try (PreparedStatement ps = conn.prepareStatement(recipeSql)) {
                ps.setInt(1, productId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        foundRecipe = true;

                        int qtyBaseUnitsUsed = rs.getInt("qty_base_units_used");
                        int stockUnitCostCents = rs.getInt("cost_price_cents_per_stock_unit");
                        int stockUnitSize = rs.getInt("stock_unit_size");

                        if (stockUnitSize > 0 && qtyBaseUnitsUsed > 0) {
                            double costPerBaseUnit = (double) stockUnitCostCents / stockUnitSize;
                            totalCost += (int) Math.round(costPerBaseUnit * qtyBaseUnitsUsed);
                        }
                    }
                }
            }

            if (foundRecipe) {
                return totalCost;
            }

            String defaultSql = """
            SELECT
                p.default_qty_used,
                si.cost_price_cents_per_stock_unit,
                si.stock_unit_size
            FROM products p
            JOIN stock_items si ON si.id = p.default_stock_item_id
            WHERE p.id = ?
              AND p.default_stock_item_id IS NOT NULL
              AND p.default_qty_used IS NOT NULL
              AND p.default_qty_used > 0
        """;

            try (PreparedStatement ps = conn.prepareStatement(defaultSql)) {
                ps.setInt(1, productId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int qtyBaseUnitsUsed = rs.getInt("default_qty_used");
                        int stockUnitCostCents = rs.getInt("cost_price_cents_per_stock_unit");
                        int stockUnitSize = rs.getInt("stock_unit_size");

                        if (stockUnitSize > 0 && qtyBaseUnitsUsed > 0) {
                            double costPerBaseUnit = (double) stockUnitCostCents / stockUnitSize;
                            return (int) Math.round(costPerBaseUnit * qtyBaseUnitsUsed);
                        }
                    }
                }
            }
        }

        return 0;
    }
}