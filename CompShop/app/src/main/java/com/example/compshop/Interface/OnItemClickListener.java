package com.example.compshop.Interface;

import com.example.compshop.Models.Item;

public interface OnItemClickListener {
    void onItemClick(Item item, int position, ActionType actionType);
}
