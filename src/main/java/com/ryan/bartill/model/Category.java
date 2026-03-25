package com.ryan.bartill.model;

public class Category {
    private final int id;
    private final String name;
    private final String colorHex;

    public Category(int id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getColorHex() { return colorHex; }

    @Override
    public String toString() { return name; }

    public static class ReportWastageRow {
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
}