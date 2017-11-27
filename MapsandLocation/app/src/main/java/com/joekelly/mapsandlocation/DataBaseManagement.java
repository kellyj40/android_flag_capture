package com.joekelly.mapsandlocation;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by joekelly on 04/11/2017.
 */

public class DataBaseManagement extends SQLiteOpenHelper {



    private int flagsCaptured = 0;
    private int currentFlagCount;


    public DataBaseManagement(Context context) {
        super(context, "flagsCaptured1", null, 4); //updating change version number
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FLAGS_TABLE = "CREATE TABLE IF NOT EXISTS flagsCaptured1 (day VARCHAR PRIMARY KEY, flagsNum INT)";
        try{
            db.execSQL("DROP TABLE IF EXISTS flagsCaptured"); //to get rid of Joes db with no version number
            db.execSQL(CREATE_FLAGS_TABLE);
        }catch(Exception e){
            System.out.println("Did not create");

        }

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed

        System.out.println("upgrading");
        try{
            db.execSQL("DROP TABLE IF EXISTS flagsCaptured1");
        }catch(Exception e){
            System.out.println("failed");

        }


        // Create table again
        onCreate(db);
    }

    void updateLocalFlagTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        long i = new Date().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Date today = Calendar.getInstance().getTime();
        String reportDate = sdf.format(today);

        int x = todaysFlags();
        currentFlagCount = x+1;
        System.out.println("date"+reportDate);
        System.out.println(""+currentFlagCount);

        String sql2 = "INSERT OR REPLACE INTO flagsCaptured1 (day, flagsNum) VALUES ('"+reportDate+"', "+currentFlagCount+")";

        try{
            db.execSQL(sql2);
        }catch(Exception e){
            System.out.println("Did not update table");

            }



    }

     int todaysFlags () {

        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Date today = Calendar.getInstance().getTime();
        String reportDate = sdf.format(today);

        String sql ="SELECT flagsNum FROM flagsCaptured1 WHERE day ='"+reportDate+"'";

        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            flagsCaptured = cursor.getInt(0);

        }else{
            flagsCaptured = 0;
        }
        cursor.close();
        return flagsCaptured;
    }

    public int weeksFlags() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        Date today = Calendar.getInstance().getTime();
        String reportDate = sdf.format(today);
        String sql ="SELECT flagsNum FROM flagsCaptured1 ASC limit 7";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        // looping through all rows and adding values

        int i = 0;
        if(cursor.getCount()>0){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                i += cursor.getInt(0);
                cursor.moveToNext();
        }}
        cursor.close();

        return i;
    }

    public int overallFlags() {

        String selectQuery = "SELECT flagsNum FROM flagsCaptured1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding values

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            i += cursor.getInt(0);
            cursor.moveToNext();
        }
        cursor.close();
        return i;
    }


}
