package com.example.compshop.Interface;


import com.example.compshop.Models.Item;

public interface OnQuantityChangeListener {
    void onAddButtonClick(Item food, int position);
    void onRemoveButtonClick(Item food, int position);
}
