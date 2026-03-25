package com.ryan.bartill.model;

public class ReportWastageRow {
    private final String name;
    private final int qty;
    private final String reason;
    private final int valueCents;

    public ReportWastageRow(String name, int qty, String reason, int valueCents) {
        this.name = name;
        this.qty = qty;
        this.reason = reason;
        this.valueCents = valueCents;
    }

    public String getName() { return name; }
    public int getQty() { return qty; }
    public String getReason() { return reason; }
    public int getValueCents() { return valueCents; }
}