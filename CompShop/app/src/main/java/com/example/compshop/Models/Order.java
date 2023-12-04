package com.example.compshop.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Order implements Parcelable {
    private String orderID;
    private String orderTime;
    private String orderStatus;
    private String orderTo;
    private String orderBy;

    public Order(String orderID, String orderTime, String orderStatus, String orderTo, String orderBy) {
        this.orderID = orderID;
        this.orderTime = orderTime;
        this.orderStatus = orderStatus;
        this.orderTo = orderTo;
        this.orderBy = orderBy;
    }

    public Order() {
    }

    protected Order(Parcel in) {
        orderID = in.readString();
        orderTime = in.readString();
        orderStatus = in.readString();
        orderTo = in.readString();
        orderBy = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo(String orderTo) {
        this.orderTo = orderTo;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(orderID);
        parcel.writeString(orderTime);
        parcel.writeString(orderStatus);
        parcel.writeString(orderTo);
        parcel.writeString(orderBy);
    }
}
