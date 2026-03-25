package com.ryan.bartill.model;

public class SaleHistoryRow {
    private final long saleId;
    private final String createdAt;
    private final String staffUsername;
    private final String paymentMethod;
    private final int totalIncCents;

    public SaleHistoryRow(long saleId, String createdAt, String staffUsername, String paymentMethod, int totalIncCents) {
        this.saleId = saleId;
        this.createdAt = createdAt;
        this.staffUsername = staffUsername;
        this.paymentMethod = paymentMethod;
        this.totalIncCents = totalIncCents;
    }

    public long getSaleId() {
        return saleId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getStaffUsername() {
        return staffUsername;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public int getTotalIncCents() {
        return totalIncCents;
    }
}