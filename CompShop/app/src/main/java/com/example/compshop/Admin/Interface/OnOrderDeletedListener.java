package com.example.compshop.Admin.Interface;

public interface OnOrderDeletedListener{
    void onOrderDeleted();
    void onOrderDeletionFailed(String errorMessage);
}
