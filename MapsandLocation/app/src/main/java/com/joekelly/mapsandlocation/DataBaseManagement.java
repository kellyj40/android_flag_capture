package com.joekelly.mapsandlocation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by joekelly on 04/11/2017.
 */

public class DataBaseManagement {

    private SQLiteDatabase userDatabase;
    private Cursor c;
    private int flagsCaptured = 0;

    public DataBaseManagement(SQLiteDatabase userDatabase){
        this.userDatabase = userDatabase;
    }

    public int makeLocalFlagTable(){
        try{

            userDatabase.execSQL("CREATE TABLE IF NOT EXISTS flagsCaptured  (flagsNum INT(3))"); //IF NOT EXISTS
            c = userDatabase.rawQuery("SELECT COUNT(*) FROM flagsCaptured;", null);
//
            c.moveToFirst();
            flagsCaptured = c.getInt(0);
            return flagsCaptured;

        }catch (Exception e){

            return -1;
        }

    }
    public void updateLocalFlagTable(){

        try{
            userDatabase.execSQL("INSERT INTO flagsCaptured(flagsNum) VALUES (1)");
        }catch(Exception e){

        }

    }


}
