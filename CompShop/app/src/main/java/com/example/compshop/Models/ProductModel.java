package com.example.compshop.Models;

public class ProductModel {

    private String name;
    private String category;
    private String description;
    private String price;
    private String item_Id;
    private String timestamp;
    private String Uid;
    private String discount;
    private String discountdescription;
    private String image;
    private int quantity;
    private int total;

    public ProductModel() {
        // Default constructor required for Firestore
    }

    public ProductModel(String name, String category, String description, String price, String item_Id, String timestamp, String Uid, String discount, String discountdescription, String image) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.item_Id = item_Id;
        this.timestamp = timestamp;
        this.Uid = Uid;
        this.discount = discount;
        this.discountdescription = discountdescription;
        this.image = image;
        this.quantity = 1;
        this.total = recalculateTotal(); // Calculate total based on initial quantity
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getItem_Id() {
        return item_Id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return Uid;
    }

    public String getDiscount() {
        return discount;
    }

    public String getDiscountdescription() {
        return discountdescription;
    }

    public String getImage() {
        return image;
    }
    public int getQuantity() {
        return quantity;
    }

    public int getTotal() {
        return total;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateTotal(); // Recalculate total when quantity changes
    }

    public void setTotal(int total) {
        this.total = total;
    }
    private int recalculateTotal() {
        int originalPrice = Integer.parseInt(price);
        if (discount != null && !discount.isEmpty() && discountdescription != null && !discountdescription.isEmpty()) {
            int discountValue = Integer.parseInt(discount);
            if (discountValue > 0 && discountdescription.contains("%")) {
                double discountPercentage = discountValue / 100.0;
                double newPrice = originalPrice * (1 - discountPercentage);
                total = (int) (newPrice * quantity);
                return total;
            }
        }

        total = originalPrice * quantity;
        return total;
    }
}
