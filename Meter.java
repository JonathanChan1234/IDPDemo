package com.jonathan.idpdemo2;

/**
 * Created by user on 2018-04-14.
 */

public class Meter {


    public String date;
    public int duration;
    public int paid;

    public int getDuration() {
        return duration;
    }

    public int getPaid() {
        return paid;
    }

    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public Meter(){

    }

    public Meter(String date, int duration, int paid){
        this.date = date;
        this.duration = duration;
        this.paid = paid;
    }

}
