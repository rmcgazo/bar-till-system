package com.ryan.bartill.inventory;

public class DeliveryRow {
    private final int productId;
    private final String productName;
    private final String categoryName;
    private final int currentStock;
    private final int costPriceCents;

    public DeliveryRow(int productId, String productName, String categoryName, int currentStock, int costPriceCents) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.currentStock = currentStock;
        this.costPriceCents = costPriceCents;
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

    public int getCurrentStock() {
        return currentStock;
    }

    public int getCostPriceCents() {
        return costPriceCents;
    }
}