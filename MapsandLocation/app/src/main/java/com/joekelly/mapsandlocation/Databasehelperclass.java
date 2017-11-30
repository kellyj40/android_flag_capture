package com.joekelly.mapsandlocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;

/*
    This class is used for all database handling,
    The database used is a local SQLite database
    This deals with the steps and all health base handling of data
 */

class Databasehelperclass extends SQLiteOpenHelper {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
    private Date today = Calendar.getInstance().getTime();
    private String reportDate = sdf.format(today);

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 8;

    // Database Name
    private static final String DATABASE_NAME = "Statistics";

    // Contacts table name
    private static final String TABLE_STATS = "stats";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TIME = "time";
    private static final String KEY_STEPS = "steps";
    private static final String KEY_DAY = "day";


    Databasehelperclass(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STATS_TABLE = "CREATE TABLE " + TABLE_STATS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " LONG, " + KEY_STEPS + " INTEGER, " + KEY_DAY + " STRING"
                + ")";
        db.execSQL("DROP TABLE IF EXISTS stats");
        db.execSQL(CREATE_STATS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding steps
    public void addSteps(Steps steps) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIME, steps.getTimestamp());
        values.put(KEY_STEPS, steps.getSteps());
        values.put(KEY_DAY, steps.getToday()); // Contact Phone

        // Inserting Row
        db.insert(TABLE_STATS, null, values);
        db.close(); // Closing database connection
    }


    public int todaysSteps() {

        String selectQuery = "SELECT steps FROM " + TABLE_STATS +  " WHERE day = '"+reportDate+"'";

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
    public int weeksSteps() {
        long x = new Date().getTime();
        long y = x - 604800000; //1 week previous
        String selectQuery = "SELECT steps FROM " + TABLE_STATS + " WHERE time > "+ y;

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

    public int overallSteps() {

        String selectQuery = "SELECT steps FROM " + TABLE_STATS;

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