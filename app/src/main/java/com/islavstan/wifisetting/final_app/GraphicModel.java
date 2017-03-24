package com.islavstan.wifisetting.final_app;



public class GraphicModel {
    int number;
    String date;
    int status;


    public GraphicModel(int number, String date, int status) {
        this.date = date;
        this.status = status;
        this.number = number;
    }

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
