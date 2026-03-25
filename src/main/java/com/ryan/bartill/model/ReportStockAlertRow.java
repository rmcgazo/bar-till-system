package com.ryan.bartill.model;

public class ReportStockAlertRow {
    private final String itemName;
    private final String currentUnits;
    private final String avgDailyUse;
    private final String daysLeft;
    private final String suggestion;

    public ReportStockAlertRow(String itemName, String currentUnits, String avgDailyUse, String daysLeft, String suggestion) {
        this.itemName = itemName;
        this.currentUnits = currentUnits;
        this.avgDailyUse = avgDailyUse;
        this.daysLeft = daysLeft;
        this.suggestion = suggestion;
    }

    public String getItemName() { return itemName; }
    public String getCurrentUnits() { return currentUnits; }
    public String getAvgDailyUse() { return avgDailyUse; }
    public String getDaysLeft() { return daysLeft; }
    public String getSuggestion() { return suggestion; }
}