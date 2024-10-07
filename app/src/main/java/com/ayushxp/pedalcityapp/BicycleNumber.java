package com.ayushxp.pedalcityapp;

public class BicycleNumber {
    private String number;
    private String status;

    public BicycleNumber() {
        // Default constructor required for calls to DataSnapshot.getValue(BicycleNumber.class)
    }

    public BicycleNumber(String number, String status) {
        this.number = number;
        this.status = status;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
