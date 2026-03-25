package com.ryan.bartill.model;

public class ReportProductRow {
    private final String name;
    private final int qty;
    private final int revenueCents;
    private final int profitCents;

    public ReportProductRow(String name, int qty, int revenueCents, int profitCents) {
        this.name = name;
        this.qty = qty;
        this.revenueCents = revenueCents;
        this.profitCents = profitCents;
    }

    public String getName() { return name; }
    public int getQty() { return qty; }
    public int getRevenueCents() { return revenueCents; }
    public int getProfitCents() { return profitCents; }
}