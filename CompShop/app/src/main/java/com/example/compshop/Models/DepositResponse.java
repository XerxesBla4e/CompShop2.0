package com.example.compshop.Models;

public class DepositResponse {
    private String message;
    private String status;
    private String ref;
    private int code;

    public DepositResponse() {
    }

    public DepositResponse(String message, String status, String ref, int code) {
        this.message = message;
        this.status = status;
        this.ref = ref;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
