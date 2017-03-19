package com.islavstan.wifisetting.model;


import com.google.gson.annotations.SerializedName;

public class Date {
    @SerializedName("date")
    String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
