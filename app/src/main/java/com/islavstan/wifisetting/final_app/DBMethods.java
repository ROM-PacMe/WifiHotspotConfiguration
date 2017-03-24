package com.islavstan.wifisetting.final_app;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.islavstan.wifisetting.model.Day;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class DBMethods {
    DBHelper dbHelper;
    SQLiteDatabase db;
    Context context;
    private final int HOUR = 3600000;

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
            String strSQL = null;
            if (c.moveToNext()) {
                Log.d("stas", "update db");

                if (Integer.parseInt(time) >= HOUR)
                    strSQL = "UPDATE wifi SET time = '" + time + "' , status = '1' WHERE date = '" + date + "'";
                else
                    strSQL = "UPDATE wifi SET time = '" + time + "' , status = '2' WHERE date = '" + date + "'";

                db.execSQL(strSQL);
            } else {
                Log.d("stas", "write db");
                ContentValues contentValues = new ContentValues();
                contentValues.put("date", date);
                contentValues.put("time", time);
                if (Integer.parseInt(time) >= HOUR)
                    contentValues.put("status", 1);
                else
                    contentValues.put("status", 2);
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

    public Observable<List<Day>> getDaysList() {
        return Observable.create(subscriber -> {
            List<Day> list = new ArrayList<>();
            Cursor c = db.rawQuery("SELECT * FROM wifi", null);
            if (c.moveToFirst()) {
                do {
                    String date = c.getString(c.getColumnIndex("date"));
                    String time = c.getString(c.getColumnIndex("time"));
                    int number = c.getInt(c.getColumnIndex("id"));
                    Day day = new Day(date, time, number);
                    list.add(0, day);


                } while (c.moveToNext());

            }
            c.close();
            subscriber.onNext(list);

        });

    }

    public Observable<List<GraphicModel>> getGraphicList() {
        return Observable.create(subscriber -> {
            List<GraphicModel> list = new ArrayList<>();
            Cursor c = db.rawQuery("SELECT * FROM wifi", null);
            boolean isLast = false;
            int redDay = 0;
            int greenCount = 0;
            int orangeCount = 0;
            GraphicModel graph;
            int npp = 0; //номер по порядку
            for (int i = 1; i < 361; i++) {
                npp++;

                if (c.moveToNext() && !isLast) {
                    String date = c.getString(c.getColumnIndex("date"));
                    String time = c.getString(c.getColumnIndex("time"));
                    int status = c.getInt(c.getColumnIndex("status"));

                    Log.d("stas", c.isLast() + " = c.islast");
                    isLast = c.isLast();

                    if (status == 1)
                        greenCount++;

                    if (c.isLast()) {
                        orangeCount = 30 - greenCount;//кол-во оранжевых
                        Log.d("stas", "orange = " + orangeCount);
                        graph = new GraphicModel(npp, date, 3);


                    } else {
                        graph = new GraphicModel(npp, date, status);

                    }
                    list.add(graph);


                    if (status == 2) {
                        redDay = i;//с какого дня отсчитывать оранжевые
                        i = 0;
                        greenCount = 0;
                        npp = 0;
                    }

                } else if (isLast) {

                    if (orangeCount > 0) {
                        graph = new GraphicModel(npp, "0", 3);
                        list.add(graph);
                        orangeCount--;
                    } else {


                        graph = new GraphicModel(npp, "0", 0);
                        list.add(graph);
                    }
                }

            }


            c.close();
            subscriber.onNext(list);

        });

    }


    public Observable<Integer> getDaysCount() {
        return Observable.create(subscriber -> {
            Cursor c = db.rawQuery("SELECT * FROM wifi", null);
            int cnt = c.getCount();
            c.close();
            subscriber.onNext(cnt);
        });

    }

}


