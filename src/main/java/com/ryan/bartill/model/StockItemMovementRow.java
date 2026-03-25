package com.ryan.bartill.model;

public class StockItemMovementRow {
    private final String createdAt;
    private final String stockItemName;
    private final String stockUnitName;
    private final int stockUnitSize;
    private final String reason;
    private final int qtyChangeBaseUnits;
    private final String staffUsername;

    public StockItemMovementRow(String createdAt,
                                String stockItemName,
                                String stockUnitName,
                                int stockUnitSize,
                                String reason,
                                int qtyChangeBaseUnits,
                                String staffUsername) {
        this.createdAt = createdAt;
        this.stockItemName = stockItemName;
        this.stockUnitName = stockUnitName;
        this.stockUnitSize = stockUnitSize;
        this.reason = reason;
        this.qtyChangeBaseUnits = qtyChangeBaseUnits;
        this.staffUsername = staffUsername;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStockItemName() {
        return stockItemName;
    }

    public String getStockUnitName() {
        return stockUnitName;
    }

    public int getStockUnitSize() {
        return stockUnitSize;
    }

    public String getReason() {
        return reason;
    }

    public int getQtyChangeBaseUnits() {
        return qtyChangeBaseUnits;
    }

    public String getStaffUsername() {
        return staffUsername;
    }
}