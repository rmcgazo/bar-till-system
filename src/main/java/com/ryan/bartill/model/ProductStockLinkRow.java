package com.ryan.bartill.model;

public class ProductStockLinkRow {
    private final int productId;
    private final String productName;
    private final String categoryName;
    private final String stockItemName;
    private final Integer qtyUsed;

    public ProductStockLinkRow(int productId, String productName, String categoryName, String stockItemName, Integer qtyUsed) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.stockItemName = stockItemName;
        this.qtyUsed = qtyUsed;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getStockItemName() {
        return stockItemName;
    }

    public Integer getQtyUsed() {
        return qtyUsed;
    }
}