package com.ryan.bartill.model;

public class RecipeRow {
    private final String productName;
    private final String stockItemName;
    private final int qtyBaseUnitsUsed;

    public RecipeRow(String productName, String stockItemName, int qtyBaseUnitsUsed) {
        this.productName = productName;
        this.stockItemName = stockItemName;
        this.qtyBaseUnitsUsed = qtyBaseUnitsUsed;
    }

    public String getProductName() {
        return productName;
    }

    public String getStockItemName() {
        return stockItemName;
    }

    public int getQtyBaseUnitsUsed() {
        return qtyBaseUnitsUsed;
    }
}