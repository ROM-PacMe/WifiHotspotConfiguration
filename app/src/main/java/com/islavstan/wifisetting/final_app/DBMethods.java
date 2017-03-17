package com.islavstan.wifisetting.final_app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import rx.Observable;

public class DBMethods {
    DBHelper dbHelper;
    SQLiteDatabase db;
    Context context;

    public DBMethods(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getDb() {
        return this.db;
    }


    public Observable<Void> writeTimeToDB(String date, String time) {
        return Observable.create(subscriber -> {
            Cursor c = db.rawQuery("SELECT * FROM wifi where date = '" + date + "'", null);
            if (c.moveToNext()) {
                Log.d("stas", "update db");
                String strSQL = "UPDATE wifi SET time = '" + time + "' WHERE date = '" + date + "'";
                db.execSQL(strSQL);
            } else {
                Log.d("stas", "write db");
                ContentValues contentValues = new ContentValues();
                contentValues.put("date", date);
                contentValues.put("time", time);
                db.insert("wifi", null, contentValues);


            }

        });
    }


    public Observable<Integer> checkDate(String date) {
        return Observable.create(subscriber -> {
            Cursor c = db.rawQuery("SELECT * FROM wifi where date = '" + date + "'", null);
            subscriber.onNext(c.moveToFirst() ? 1 : 0);
            c.close();

        });
    }

    public Observable<String> getTime(String date) {
        return Observable.create(subscriber -> {
            Cursor c = db.rawQuery("SELECT * FROM wifi where date = '" + date + "'", null);
            String time;
            subscriber.onNext(c.moveToFirst() ? time = c.getString(c.getColumnIndex("time"))
                    : null);
            c.close();

        });
    }


}
