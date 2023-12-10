package com.example.compshop.Models;

import com.example.compshop.Interface.OnMoveToItemsListener;

import java.io.Serializable;

public class category implements Serializable {
    String categoryName;
    String timestamp;
    String Uid;
    String image;

    public category(String categoryNam, String timestamp, String uid, String image) {
        this.categoryName = categoryNam;
        this.timestamp = timestamp;
        this.Uid = uid;
        this.image = image;
    }

    public category() {
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
