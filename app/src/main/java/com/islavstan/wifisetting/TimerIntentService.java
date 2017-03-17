package com.islavstan.wifisetting;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class TimerIntentService extends IntentService {
    Long startTime;
    long millis;
    int seconds;
    int minutes;
    private volatile long elapsedTime;
   // private final MyBinder myBinder = new MyBinder();
    public static final String ACTION = "com.islavstan.wifisetting.TimerIntentService";

    public TimerIntentService() {
        super("name");
    }


  /*  @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        TimerIntentService getService() {
            return TimerIntentService.this;
        }
    }
*/
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startTime = System.currentTimeMillis();
        this.elapsedTime = -1;
        while (elapsedTime == -1) {
            millis = System.currentTimeMillis() - startTime;
            seconds = (int) (millis / 1000);
            int hours = seconds/3600;
            minutes = seconds / 60;
            seconds = seconds % 60;
            Log.d("stas", String.format("%02d:%02d", minutes, seconds));
            // возвращаем результат
            Intent responseIntent = new Intent();
            responseIntent.setAction(ACTION);
            responseIntent.addCategory(Intent.CATEGORY_DEFAULT);
            responseIntent.putExtra("minutes", minutes);
            responseIntent.putExtra("seconds", seconds);
            sendBroadcast(responseIntent);
            // LocalBroadcastManager.getInstance(this).sendBroadcast(responseIntent);


        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.elapsedTime = 2;
        Log.d("stas", "ondestroy " + elapsedTime);
    }
}
