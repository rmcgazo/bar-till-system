package com.ryan.bartill.model;

public class StockItem {
    private final int id;
    private final String name;
    private final String categoryName;
    private final String baseUnit;
    private final String stockUnitName;
    private final int stockUnitSize;
    private final int currentQtyBaseUnits;
    private final int costPriceCentsPerStockUnit;
    private final int lowStockThresholdBaseUnits;
    private final boolean active;

    public StockItem(int id,
                     String name,
                     String categoryName,
                     String baseUnit,
                     String stockUnitName,
                     int stockUnitSize,
                     int currentQtyBaseUnits,
                     int costPriceCentsPerStockUnit,
                     int lowStockThresholdBaseUnits,
                     boolean active) {
        this.id = id;
        this.name = name;
        this.categoryName = categoryName;
        this.baseUnit = baseUnit;
        this.stockUnitName = stockUnitName;
        this.stockUnitSize = stockUnitSize;
        this.currentQtyBaseUnits = currentQtyBaseUnits;
        this.costPriceCentsPerStockUnit = costPriceCentsPerStockUnit;
        this.lowStockThresholdBaseUnits = lowStockThresholdBaseUnits;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategoryName() { return categoryName; }
    public String getBaseUnit() { return baseUnit; }
    public String getStockUnitName() { return stockUnitName; }
    public int getStockUnitSize() { return stockUnitSize; }
    public int getCurrentQtyBaseUnits() { return currentQtyBaseUnits; }
    public int getCostPriceCentsPerStockUnit() { return costPriceCentsPerStockUnit; }
    public int getLowStockThresholdBaseUnits() { return lowStockThresholdBaseUnits; }
    public boolean isActive() { return active; }

    public double getCurrentStockUnits() {
        if (stockUnitSize <= 0) return 0;
        return (double) currentQtyBaseUnits / stockUnitSize;
    }

    public double getLowStockThresholdStockUnits() {
        if (stockUnitSize <= 0) return 0;
        return (double) lowStockThresholdBaseUnits / stockUnitSize;
    }

    public int getStockValueCents() {
        if (stockUnitSize <= 0) return 0;
        double units = getCurrentStockUnits();
        return (int) Math.round(units * costPriceCentsPerStockUnit);
    }

    public boolean isLowStock() {
        return currentQtyBaseUnits <= lowStockThresholdBaseUnits;
    }

    @Override
    public String toString() {
        return name;
    }
}