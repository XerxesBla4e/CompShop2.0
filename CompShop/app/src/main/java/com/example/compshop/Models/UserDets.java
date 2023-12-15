package com.example.compshop.Models;

public class UserDets {
    private String uid;
    private String name;
    private String email;
    private String phonenumber;
    private String location;
    private String city;
    private String state;
    private String country;
    private String district;
    private String gender;
    private String DOB;
    private String timestamp;
    private String latitude;
    private String longitude;
    private String accounttype;
    private String online;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDets() {
        // Default constructor required for Firebase
    }

    public UserDets(String uid, String name, String email, String phonenumber, String location, String city, String state, String country, String district, String gender, String DOB, String timestamp, String latitude, String longitude, String accounttype, String online,String token) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phonenumber = phonenumber;
        this.location = location;
        this.city = city;
        this.state = state;
        this.country = country;
        this.district = district;
        this.gender = gender;
        this.DOB = DOB;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accounttype = accounttype;
        this.online = online;
        this.token = token;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(String accounttype) {
        this.accounttype = accounttype;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
