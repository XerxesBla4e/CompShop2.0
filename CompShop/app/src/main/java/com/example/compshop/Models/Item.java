package com.example.compshop.Models;

public class Item {
    private int id;
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

    private boolean isFavorite;

    public Item(String name, String category, String description, String price, String item_Id, String timestamp, String uid, String image,
                int quantity, int total) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.item_Id = item_Id;
        this.timestamp = timestamp;
        this.Uid = uid;
        this.image = image;
        this.quantity = 1;
        this.total = recalculateTotal(); // Calculate total based on initial quantity
        this.isFavorite = false;
    }

    // Update the total based on the provided discount and discount description
    public void updateTotal(String discount, String discountdescription) {
        this.discount = discount;
        this.discountdescription = discountdescription;
    }

    public Item() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getItem_Id() {
        return item_Id;
    }

    public void setItem_Id(String item_Id) {
        this.item_Id = item_Id;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscountdescription() {
        return discountdescription;
    }

    public void setDiscountdescription(String discountdescription) {
        this.discountdescription = discountdescription;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
