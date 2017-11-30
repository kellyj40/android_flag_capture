package com.joekelly.mapsandlocation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the steps class that keeps track of all steps motions for when moving into different classes
 */

public class Steps {

    //private variables
    int _id;
    long _time;
    int _steps;
    String _day;

    // Empty constructor
    public Steps(){

    }
    // constructor
    public Steps(int id, long time, int steps, String day){
        this._id = id;
        this._time = time;
        this._steps = steps;
        this._day = day;
    }

    // constructor
    public Steps(int steps){
        long i = new Date().getTime();
        //Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Date today = Calendar.getInstance().getTime();
        String reportDate = sdf.format(today);

        this._time = i;
        this._steps = steps;
        this._day = reportDate;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting Time
    public long getTimestamp(){
        return this._time;
    }

    // setting Time
    public void setTimestamp(long time){
        this._time = time;
    }

    // getting Steps
    public int getSteps(){
        return this._steps;
    }

    // setting Steps
    public void setSteps(int steps){
        this._steps = steps;
    }

    // getting Day
    public String getToday(){
        return this._day;
    }

    // setting Steps
    public void setToday(String day){
        this._day = day;
    }
}

