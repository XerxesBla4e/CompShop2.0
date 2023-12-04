package com.example.compshop.Models;

import java.util.Map;

public class ItemOrder {
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String itemPrice;
    private int itemQuantity;
    private int itemTotal;
    private String itemImage;

    // Empty constructor required for Firestore
    public ItemOrder() {
    }

    public ItemOrder(String itemId, String itemName, String itemDescription, String itemPrice,
                     int itemQuantity, int itemTotal, String itemImage) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.itemQuantity = itemQuantity;
        this.itemTotal = itemTotal;
        this.itemImage = itemImage;
    }

    // Getters and setters

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public int getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(int itemTotal) {
        this.itemTotal = itemTotal;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    // Method to create an ItemOrder instance from a Map (useful when reading from Firestore)
    public static ItemOrder fromMap(Map<String, Object> map) {
        ItemOrder itemOrder = new ItemOrder();
        itemOrder.setItemId((String) map.get("itemId"));
        itemOrder.setItemName((String) map.get("itemName"));
        itemOrder.setItemDescription((String) map.get("itemDescription"));
        itemOrder.setItemPrice((String) map.get("itemPrice"));
        itemOrder.setItemQuantity(((Long) map.get("itemQuantity")).intValue());
        itemOrder.setItemTotal(((Long) map.get("itemTotal")).intValue());
        itemOrder.setItemImage((String) map.get("itemImage"));
        return itemOrder;
    }
}
