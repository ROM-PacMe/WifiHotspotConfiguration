package com.islavstan.wifisetting.final_app;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "WifiVomer", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createWifiTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    private void createWifiTable(SQLiteDatabase db) {
        db.execSQL("create table wifi ("
                + "id integer primary key autoincrement, date text, time text );");


    }

}

