package com.ryan.bartill.dao;

import com.ryan.bartill.model.StockItem;
import com.ryan.bartill.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StockItemDao {

    public void consumeBaseUnits(Connection conn, int stockItemId, int qtyBaseUnits, Integer staffId, String reason) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("""
            UPDATE stock_items
            SET current_qty_base_units = current_qty_base_units - ?
            WHERE id = ?
        """)) {
            ps.setInt(1, qtyBaseUnits);
            ps.setInt(2, stockItemId);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO stock_item_movements(stock_item_id, staff_id, qty_change_base_units, reason)
            VALUES(?,?,?,?)
        """)) {
            ps.setInt(1, stockItemId);

            if (staffId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, staffId);
            }

            ps.setInt(3, -qtyBaseUnits);
            ps.setString(4, reason);
            ps.executeUpdate();
        }
    }

    public void recordDelivery(int stockItemId, int qtyStockUnits, int unitCostCents, Integer staffId) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int stockUnitSize;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT stock_unit_size FROM stock_items WHERE id = ?")) {
                    ps.setInt(1, stockItemId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Stock item not found");
                        }
                        stockUnitSize = rs.getInt("stock_unit_size");
                    }
                }

                int qtyBaseUnits = qtyStockUnits * stockUnitSize;

                try (PreparedStatement ps = conn.prepareStatement("""
                    UPDATE stock_items
                    SET current_qty_base_units = current_qty_base_units + ?,
                        cost_price_cents_per_stock_unit = ?
                    WHERE id = ?
                """)) {
                    ps.setInt(1, qtyBaseUnits);
                    ps.setInt(2, unitCostCents);
                    ps.setInt(3, stockItemId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO stock_deliveries(stock_item_id, qty_stock_units, unit_cost_cents, staff_id)
                    VALUES(?,?,?,?)
                """)) {
                    ps.setInt(1, stockItemId);
                    ps.setInt(2, qtyStockUnits);
                    ps.setInt(3, unitCostCents);

                    if (staffId == null) {
                        ps.setNull(4, Types.INTEGER);
                    } else {
                        ps.setInt(4, staffId);
                    }

                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO stock_item_movements(stock_item_id, staff_id, qty_change_base_units, reason)
                    VALUES(?,?,?,?)
                """)) {
                    ps.setInt(1, stockItemId);

                    if (staffId == null) {
                        ps.setNull(2, Types.INTEGER);
                    } else {
                        ps.setInt(2, staffId);
                    }

                    ps.setInt(3, qtyBaseUnits);
                    ps.setString(4, "DELIVERY");
                    ps.executeUpdate();
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void applyStockTakeAdjustment(
            Connection conn,
            int stockItemId,
            int newActualBaseQty,
            String reason
    ) throws Exception {

        int oldQty = 0;

        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT current_qty_base_units FROM stock_items WHERE id = ?")) {
            ps.setInt(1, stockItemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    oldQty = rs.getInt("current_qty_base_units");
                } else {
                    throw new IllegalArgumentException("Stock item not found: " + stockItemId);
                }
            }
        }

        int diff = newActualBaseQty - oldQty;

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE stock_items SET current_qty_base_units = ? WHERE id = ?")) {
            ps.setInt(1, newActualBaseQty);
            ps.setInt(2, stockItemId);
            ps.executeUpdate();
        }

        if (diff != 0) {
            try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO stock_item_movements
            (stock_item_id, qty_change_base_units, reason, created_at)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
        """)) {
                ps.setInt(1, stockItemId);
                ps.setInt(2, diff);
                ps.setString(3, reason);
                ps.executeUpdate();
            }
        }
    }

    private StockItem map(ResultSet rs) throws Exception {
        return new StockItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category_name"),
                rs.getString("base_unit"),
                rs.getString("stock_unit_name"),
                rs.getInt("stock_unit_size"),
                rs.getInt("current_qty_base_units"),
                rs.getInt("cost_price_cents_per_stock_unit"),
                rs.getInt("low_stock_threshold_base_units"),
                rs.getInt("active") == 1
        );
    }

    public List<StockItem> listAllActive() throws Exception {
        String sql = """
            SELECT id, name, category_name, base_unit, stock_unit_name,
                   stock_unit_size, current_qty_base_units,
                   cost_price_cents_per_stock_unit,
                   low_stock_threshold_base_units,
                   active
            FROM stock_items
            WHERE active = 1
            ORDER BY name
        """;

        List<StockItem> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }
        }

        return out;
    }

    public List<StockItem> listLowStockActive() throws Exception {
        String sql = """
            SELECT id, name, category_name, base_unit, stock_unit_name,
                   stock_unit_size, current_qty_base_units,
                   cost_price_cents_per_stock_unit,
                   low_stock_threshold_base_units,
                   active
            FROM stock_items
            WHERE active = 1
              AND current_qty_base_units <= low_stock_threshold_base_units
            ORDER BY name
        """;

        List<StockItem> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }
        }

        return out;
    }

    public void createStockItem(String name,
                                String categoryName,
                                String baseUnit,
                                String stockUnitName,
                                int stockUnitSize,
                                int initialStockUnits,
                                int costPriceCentsPerStockUnit,
                                int lowStockThresholdStockUnits) throws Exception {

        int currentQtyBaseUnits = initialStockUnits * stockUnitSize;
        int lowStockThresholdBaseUnits = lowStockThresholdStockUnits * stockUnitSize;

        String sql = """
            INSERT INTO stock_items(
                name, category_name, base_unit, stock_unit_name,
                stock_unit_size, current_qty_base_units,
                cost_price_cents_per_stock_unit, low_stock_threshold_base_units, active
            )
            VALUES(?,?,?,?,?,?,?,?,1)
        """;

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name.trim());

            if (categoryName == null || categoryName.trim().isEmpty()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, categoryName.trim());
            }

            ps.setString(3, baseUnit.trim());
            ps.setString(4, stockUnitName.trim());
            ps.setInt(5, stockUnitSize);
            ps.setInt(6, currentQtyBaseUnits);
            ps.setInt(7, costPriceCentsPerStockUnit);
            ps.setInt(8, lowStockThresholdBaseUnits);

            ps.executeUpdate();
        }
    }

    public List<com.ryan.bartill.model.StockItemMovementRow> listRecentMovements(int limit) throws Exception {
        String sql = """
            SELECT
                sim.created_at,
                si.name AS stock_item_name,
                si.stock_unit_name,
                si.stock_unit_size,
                sim.reason,
                sim.qty_change_base_units,
                COALESCE(st.username, '') AS staff_username
            FROM stock_item_movements sim
            JOIN stock_items si ON si.id = sim.stock_item_id
            LEFT JOIN staff st ON st.id = sim.staff_id
            ORDER BY sim.id DESC
            LIMIT ?
        """;

        List<com.ryan.bartill.model.StockItemMovementRow> out = new ArrayList<>();

        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new com.ryan.bartill.model.StockItemMovementRow(
                            rs.getString("created_at"),
                            rs.getString("stock_item_name"),
                            rs.getString("stock_unit_name"),
                            rs.getInt("stock_unit_size"),
                            rs.getString("reason"),
                            rs.getInt("qty_change_base_units"),
                            rs.getString("staff_username")
                    ));
                }
            }
        }

        return out;
    }

    public int importFromCsv(java.nio.file.Path csvPath) throws Exception {
        int imported = 0;

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try (var lines = java.nio.file.Files.lines(csvPath)) {
                var it = lines.iterator();

                if (!it.hasNext()) return 0;
                it.next(); // skip header

                while (it.hasNext()) {
                    String line = it.next().trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(",", -1);
                    if (parts.length < 8) {
                        throw new IllegalArgumentException("Bad CSV line: " + line);
                    }

                    String name = parts[0].trim();
                    String category = parts[1].trim();
                    String baseUnit = parts[2].trim();
                    String stockUnit = parts[3].trim();
                    int unitSize = Integer.parseInt(parts[4].trim());
                    int initialUnits = Integer.parseInt(parts[5].trim());
                    int costCents = parseMoneyToCents(parts[6].trim());
                    int lowStockThresholdUnits = Integer.parseInt(parts[7].trim());

                    int baseQty = initialUnits * unitSize;
                    int lowStockThresholdBaseQty = lowStockThresholdUnits * unitSize;

                    String updateSql = """
                        UPDATE stock_items
                        SET category_name = ?,
                            base_unit = ?,
                            stock_unit_name = ?,
                            stock_unit_size = ?,
                            current_qty_base_units = ?,
                            cost_price_cents_per_stock_unit = ?,
                            low_stock_threshold_base_units = ?,
                            active = 1
                        WHERE LOWER(name) = LOWER(?)
                    """;

                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setString(1, category);
                        ps.setString(2, baseUnit);
                        ps.setString(3, stockUnit);
                        ps.setInt(4, unitSize);
                        ps.setInt(5, baseQty);
                        ps.setInt(6, costCents);
                        ps.setInt(7, lowStockThresholdBaseQty);
                        ps.setString(8, name);

                        int changed = ps.executeUpdate();
                        if (changed > 0) {
                            imported++;
                            continue;
                        }
                    }

                    String insertSql = """
                        INSERT INTO stock_items
                        (name, category_name, base_unit, stock_unit_name, stock_unit_size,
                         current_qty_base_units, cost_price_cents_per_stock_unit,
                         low_stock_threshold_base_units, active)
                        VALUES (?,?,?,?,?,?,?,?,1)
                    """;

                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        ps.setString(1, name);
                        ps.setString(2, category);
                        ps.setString(3, baseUnit);
                        ps.setString(4, stockUnit);
                        ps.setInt(5, unitSize);
                        ps.setInt(6, baseQty);
                        ps.setInt(7, costCents);
                        ps.setInt(8, lowStockThresholdBaseQty);
                        ps.executeUpdate();
                    }

                    imported++;
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

            conn.commit();
        }

        return imported;
    }

    private static int parseMoneyToCents(String text) {
        String t = text.trim();

        if (!t.contains(".")) {
            return Integer.parseInt(t) * 100;
        }

        String[] parts = t.split("\\.");

        int euros = Integer.parseInt(parts[0]);
        String cents = parts[1];

        if (cents.length() == 1) cents = cents + "0";

        return euros * 100 + Integer.parseInt(cents);
    }


}