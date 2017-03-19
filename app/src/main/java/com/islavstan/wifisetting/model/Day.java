package com.islavstan.wifisetting.model;



public class Day {
    String date;
    String time;
    int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Day(String date, String time, int number) {
        this.date = date;
        this.time = time;
        this.number = number;
    }
}
