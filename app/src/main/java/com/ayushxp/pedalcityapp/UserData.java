package com.ayushxp.pedalcityapp;
public class UserData {
    String Name, PhoneNumber, BirthDate;

    public UserData() {
    }

    public UserData(String Name, String PhoneNumber, String BirthDate) {
        this.Name = Name;
        this.PhoneNumber = PhoneNumber;
        this.BirthDate = BirthDate;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String PhoneNumber) {
        this.PhoneNumber = PhoneNumber;
    }

    public String getBirthDate() {
        return BirthDate;
    }

    public void setBirthDate(String BirthDate) {
        this.BirthDate = BirthDate;
    }
}
