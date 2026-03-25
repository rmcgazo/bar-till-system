package com.ryan.bartill.model;

public class StockTakeRow {

    private int stockItemId;
    private String name;
    private String categoryName;
    private String stockUnitName;
    private int stockUnitSize;

    private double expectedUnits;
    private int expectedBaseQty;

    private double actualUnits;
    private int actualBaseQty;
    private int actualLooseBaseQty;

    private int costPerUnitCents;
    private String notes;
    private boolean countEntered;

    public StockTakeRow(int stockItemId,
                        String name,
                        String categoryName,
                        String stockUnitName,
                        int stockUnitSize,
                        double expectedUnits,
                        int expectedBaseQty,
                        double actualUnits,
                        int actualBaseQty,
                        int actualLooseBaseQty,
                        int costPerUnitCents) {
        this.stockItemId = stockItemId;
        this.name = name;
        this.categoryName = categoryName;
        this.stockUnitName = stockUnitName;
        this.stockUnitSize = stockUnitSize;
        this.expectedUnits = expectedUnits;
        this.expectedBaseQty = expectedBaseQty;
        this.actualUnits = actualUnits;
        this.actualBaseQty = actualBaseQty;
        this.actualLooseBaseQty = actualLooseBaseQty;
        this.costPerUnitCents = costPerUnitCents;
        this.notes = "";
        this.countEntered = false;
    }

    public int getStockItemId() {
        return stockItemId;
    }

    public String getName() {
        return name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getStockUnitName() {
        return stockUnitName;
    }

    public int getStockUnitSize() {
        return stockUnitSize;
    }

    public double getExpectedUnits() {
        return expectedUnits;
    }

    public int getExpectedBaseQty() {
        return expectedBaseQty;
    }

    public double getActualUnits() {
        return actualUnits;
    }

    public void setActualUnits(double actualUnits) {
        this.actualUnits = actualUnits;
        this.countEntered = true;
    }

    public int getActualBaseQty() {
        return actualBaseQty;
    }

    public void setActualBaseQty(int actualBaseQty) {
        this.actualBaseQty = actualBaseQty;
        this.countEntered = true;
    }

    public int getActualLooseBaseQty() {
        return actualLooseBaseQty;
    }

    public void setActualLooseBaseQty(int actualLooseBaseQty) {
        this.actualLooseBaseQty = actualLooseBaseQty;
        this.countEntered = true;
    }

    public int getVarianceBaseQty() {
        return actualBaseQty - expectedBaseQty;
    }

    public double getVarianceUnits() {
        if (stockUnitSize <= 0) return 0;
        return (double) (actualBaseQty - expectedBaseQty) / stockUnitSize;
    }

    public int getCostPerUnitCents() {
        return costPerUnitCents;
    }

    public int getVarianceValueCents() {
        return (int) Math.round(getVarianceUnits() * costPerUnitCents);
    }

    public int getExpectedValueCents() {
        return (int) Math.round(expectedUnits * costPerUnitCents);
    }

    public int getActualValueCents() {
        return (int) Math.round(actualUnits * costPerUnitCents);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCountEntered() {
        return countEntered;
    }

    public void resetToExpected() {
        this.actualUnits = this.expectedUnits;
        this.actualBaseQty = this.expectedBaseQty;
        this.actualLooseBaseQty = 0;
        this.notes = "";
        this.countEntered = false;
    }
}