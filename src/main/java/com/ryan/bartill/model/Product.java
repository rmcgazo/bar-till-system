package com.ryan.bartill.model;

public class Product {
    private final int id;
    private final String name;
    private final String barcode;
    private final int priceExCents;
    private final int costPriceCents;
    private final int vatRate;
    private final int stockQty;
    private final boolean active;

    private final Integer categoryId;
    private final String categoryName;

    private final Integer defaultStockItemId;
    private final Integer defaultQtyUsed;

    public Product(int id,
                   String name,
                   String barcode,
                   int priceExCents,
                   int costPriceCents,
                   int vatRate,
                   int stockQty,
                   boolean active,
                   Integer categoryId,
                   String categoryName,
                   Integer defaultStockItemId,
                   Integer defaultQtyUsed) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.priceExCents = priceExCents;
        this.costPriceCents = costPriceCents;
        this.vatRate = vatRate;
        this.stockQty = stockQty;
        this.active = active;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.defaultStockItemId = defaultStockItemId;
        this.defaultQtyUsed = defaultQtyUsed;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public int getPriceExCents() { return priceExCents; }
    public int getCostPriceCents() { return costPriceCents; }
    public int getVatRate() { return vatRate; }
    public int getStockQty() { return stockQty; }
    public boolean isActive() { return active; }
    public Integer getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Integer getDefaultStockItemId() { return defaultStockItemId; }
    public Integer getDefaultQtyUsed() { return defaultQtyUsed; }

    @Override
    public String toString() {
        return name;
    }
}