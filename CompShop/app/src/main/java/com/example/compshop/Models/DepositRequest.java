package com.example.compshop.Models;

public class DepositRequest {
    private String secret_key;
    private String currency;
    private String phone;
    private String amount;
    private String email;
    private String ref;
    private String callback;

    public DepositRequest(String secret_key, String currency, String phone, String amount, String email, String ref, String callback) {
        this.secret_key = secret_key;
        this.currency = currency;
        this.phone = phone;
        this.amount = amount;
        this.email = email;
        this.ref = ref;
        this.callback = callback;
    }

    public DepositRequest() {
    }

    public String getSecret_key() {
        return secret_key;
    }

    public void setSecret_key(String secret_key) {
        this.secret_key = secret_key;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}
