package com.ryan.bartill.model;

public class StockMovement {
    private final String createdAt;
    private final String staffUsername;
    private final String reason;
    private final int qtyChange;

    public StockMovement(String createdAt, String staffUsername, String reason, int qtyChange) {
        this.createdAt = createdAt;
        this.staffUsername = staffUsername;
        this.reason = reason;
        this.qtyChange = qtyChange;
    }

    public String getCreatedAt() { return createdAt; }
    public String getStaffUsername() { return staffUsername; }
    public String getReason() { return reason; }
    public int getQtyChange() { return qtyChange; }
}