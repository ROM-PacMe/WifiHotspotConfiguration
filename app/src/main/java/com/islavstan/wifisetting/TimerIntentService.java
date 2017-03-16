package com.islavstan.wifisetting;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;


public class TimerIntentService extends IntentService  {
    Long startTime;
    long millis;
    int seconds;
    int minutes;
    private volatile long elapsedTime;

    public static final String ACTION = "RESPONSE";
    public TimerIntentService() {
        super("name");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startTime = System.currentTimeMillis();
        this.elapsedTime = -1;
       while (elapsedTime == -1) {
            millis = System.currentTimeMillis() - startTime;
            seconds = (int) (millis / 1000);
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


        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        this.elapsedTime = 2;
        Log.d("stas", "ondestroy " + elapsedTime);
    }
}
