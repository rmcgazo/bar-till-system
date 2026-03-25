package com.ryan.bartill.model;

public class QuickButton {
    public final int id;
    public final int page;
    public final int position;
    public final String type; // PRODUCT or CATEGORY
    public final Integer productId;
    public final Integer categoryId;
    public final String label;
    public final String colorHex;

    public QuickButton(int id, int page, int position, String type,
                       Integer productId, Integer categoryId,
                       String label, String colorHex) {
        this.id = id;
        this.page = page;
        this.position = position;
        this.type = type;
        this.productId = productId;
        this.categoryId = categoryId;
        this.label = label;
        this.colorHex = colorHex;
    }
}