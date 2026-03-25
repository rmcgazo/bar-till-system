package com.ryan.bartill.service;

import com.ryan.bartill.dao.ProductRecipeDao;
import com.ryan.bartill.dao.StockItemDao;
import com.ryan.bartill.model.Product;
import com.ryan.bartill.util.Db;
import com.ryan.bartill.dao.ProductDao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class SaleService {

    private final ProductDao productDao = new ProductDao();


    public static class SaleLine {
        public final int productId;
        public final String name;
        public final int qty;
        public final int unitPriceExCents;
        public final int vatRate;

        public SaleLine(int productId, String name, int qty, int unitPriceExCents, int vatRate) {
            this.productId = productId;
            this.name = name;
            this.qty = qty;
            this.unitPriceExCents = unitPriceExCents;
            this.vatRate = vatRate;
        }
    }

    public static class Receipt {
        public final long saleId;
        public final String createdAt;
        public final List<ReceiptLine> lines;
        public final int totalExCents;
        public final int totalVatCents;
        public final int totalIncCents;
        public final int cashReceivedCents;
        public final int changeGivenCents;
        public final String paymentMethod;

        public Receipt(long saleId, String createdAt,
                       List<ReceiptLine> lines,
                       int totalExCents, int totalVatCents, int totalIncCents,
                       int cashReceivedCents, int changeGivenCents,
                       String paymentMethod) {
            this.saleId = saleId;
            this.createdAt = createdAt;
            this.lines = lines;
            this.totalExCents = totalExCents;
            this.totalVatCents = totalVatCents;
            this.totalIncCents = totalIncCents;
            this.cashReceivedCents = cashReceivedCents;
            this.changeGivenCents = changeGivenCents;
            this.paymentMethod = paymentMethod;
        }
    }

    public static class ReceiptLine {
        public final String name;
        public final int qty;
        public final int lineIncCents;

        public ReceiptLine(String name, int qty, int lineIncCents) {
            this.name = name;
            this.qty = qty;
            this.lineIncCents = lineIncCents;
        }
    }

    private final ProductRecipeDao productRecipeDao = new ProductRecipeDao();
    private final StockItemDao stockItemDao = new StockItemDao();

    public Receipt completeCashSale(int staffId, List<SaleLine> lines,
                                    int cashReceivedCents, int changeGivenCents) throws Exception {
        return completeSale(staffId, lines, "CASH", cashReceivedCents, changeGivenCents);
    }

    public Receipt completeCardSale(int staffId, List<SaleLine> lines) throws Exception {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Empty sale");
        }

        int totalEx = 0;
        int totalVat = 0;

        for (SaleLine l : lines) {
            int lineEx = l.qty * l.unitPriceExCents;
            int lineVat = (lineEx * l.vatRate) / 100;
            totalEx += lineEx;
            totalVat += lineVat;
        }

        int totalInc = totalEx + totalVat;

        return completeSale(staffId, lines, "CARD", totalInc, 0);
    }

    private Receipt completeSale(int staffId, List<SaleLine> lines,
                                 String paymentMethod,
                                 int cashReceivedCents, int changeGivenCents) throws Exception {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Empty sale");
        }

        int totalEx = 0;
        int totalVat = 0;

        for (SaleLine l : lines) {
            int lineEx = l.qty * l.unitPriceExCents;
            int lineVat = (lineEx * l.vatRate) / 100;
            totalEx += lineEx;
            totalVat += lineVat;
        }

        int totalInc = totalEx + totalVat;

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);

            try {
                long saleId = insertSale(
                        conn,
                        staffId,
                        totalEx,
                        totalVat,
                        totalInc,
                        paymentMethod,
                        cashReceivedCents,
                        changeGivenCents
                );

                for (SaleLine l : lines) {
                    int lineEx = l.qty * l.unitPriceExCents;
                    int lineVat = (lineEx * l.vatRate) / 100;
                    int lineInc = lineEx + lineVat;

                    insertSaleItem(conn, saleId, l, lineEx, lineVat, lineInc);
                    consumeRecipeStock(conn, staffId, l);
                }

                String createdAt = fetchSaleTimestamp(conn, saleId);

                conn.commit();

                List<ReceiptLine> receiptLines = new ArrayList<>();
                for (SaleLine l : lines) {
                    int lineEx = l.qty * l.unitPriceExCents;
                    int lineVat = (lineEx * l.vatRate) / 100;
                    int lineInc = lineEx + lineVat;
                    receiptLines.add(new ReceiptLine(l.name, l.qty, lineInc));
                }

                return new Receipt(
                        saleId,
                        createdAt,
                        receiptLines,
                        totalEx,
                        totalVat,
                        totalInc,
                        cashReceivedCents,
                        changeGivenCents,
                        paymentMethod
                );

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void consumeRecipeStock(Connection conn, int staffId, SaleLine saleLine) throws Exception {
        List<ProductRecipeDao.RecipeLine> recipeLines =
                productRecipeDao.listRecipeForProduct(saleLine.productId);

        if (recipeLines != null && !recipeLines.isEmpty()) {
            for (ProductRecipeDao.RecipeLine recipeLine : recipeLines) {
                int totalBaseUnitsToUse = recipeLine.getQtyBaseUnitsUsed() * saleLine.qty;

                stockItemDao.consumeBaseUnits(
                        conn,
                        recipeLine.getStockItemId(),
                        totalBaseUnitsToUse,
                        staffId,
                        "SALE"
                );
            }
            return;
        }

        Product product = productDao.findById(saleLine.productId);
        if (product == null) {
            return;
        }

        if (product.getDefaultStockItemId() != null && product.getDefaultQtyUsed() != null && product.getDefaultQtyUsed() > 0) {
            int totalBaseUnitsToUse = product.getDefaultQtyUsed() * saleLine.qty;

            stockItemDao.consumeBaseUnits(
                    conn,
                    product.getDefaultStockItemId(),
                    totalBaseUnitsToUse,
                    staffId,
                    "SALE"
            );
        }
    }

    private long insertSale(Connection conn, int staffId,
                            int totalEx, int totalVat, int totalInc,
                            String paymentMethod,
                            int cashReceived, int changeGiven) throws Exception {
        String sql = """
            INSERT INTO sales(
                staff_id,
                total_ex_cents,
                total_vat_cents,
                total_inc_cents,
                payment_method,
                cash_received_cents,
                change_given_cents
            )
            VALUES (?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, staffId);
            ps.setInt(2, totalEx);
            ps.setInt(3, totalVat);
            ps.setInt(4, totalInc);
            ps.setString(5, paymentMethod);
            ps.setInt(6, cashReceived);
            ps.setInt(7, changeGiven);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("No sale id generated");
                }
                return keys.getLong(1);
            }
        }
    }

    private void insertSaleItem(Connection conn, long saleId, SaleLine l,
                                int lineEx, int lineVat, int lineInc) throws Exception {
        String sql = """
            INSERT INTO sale_items(
              sale_id, product_id, name_snapshot, qty, unit_price_ex_cents, vat_rate,
              line_ex_cents, line_vat_cents, line_inc_cents
            ) VALUES (?,?,?,?,?,?,?,?,?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, saleId);
            ps.setInt(2, l.productId);
            ps.setString(3, l.name);
            ps.setInt(4, l.qty);
            ps.setInt(5, l.unitPriceExCents);
            ps.setInt(6, l.vatRate);
            ps.setInt(7, lineEx);
            ps.setInt(8, lineVat);
            ps.setInt(9, lineInc);
            ps.executeUpdate();
        }
    }

    private String fetchSaleTimestamp(Connection conn, long saleId) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT created_at FROM sales WHERE id = ?")) {
            ps.setLong(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return "";
                return rs.getString(1);
            }
        }
    }
}