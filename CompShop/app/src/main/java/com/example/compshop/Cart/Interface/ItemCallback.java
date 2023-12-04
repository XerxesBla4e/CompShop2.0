package com.example.compshop.Cart.Interface;

import com.example.compshop.Models.Item;

import java.util.List;

public interface ItemCallback {
    void onDatabaseTaskComplete(List<Item> itemList);
}
