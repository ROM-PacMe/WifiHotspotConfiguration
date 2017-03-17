package com.islavstan.wifisetting.final_app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.islavstan.wifisetting.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TimeService extends Service {

    private static final String TAG = "stas";

    // Start and end times in milliseconds
    private long endTime;
    long timeInMilliseconds = 0L;
    private long startTime = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    DBMethods dbMethods;
    // Is the service tracking time?
    private boolean isTimerRunning;

    // Foreground notification id
    private static final int NOTIFICATION_ID = 1;

    // Service binder
    private final IBinder serviceBinder = new TimeService.RunServiceBinder();


    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    String date = df.format(c.getTime());


    public class RunServiceBinder extends Binder {
        TimeService getService() {

            return TimeService.this;
        }
    }

    @Override
    public void onCreate() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Creating service");
        }

         dbMethods = new DBMethods(getApplicationContext());
        Log.d("stas","date = "+ date);
        dbMethods.checkDate(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result == 1) {
                       Log.d("stas","result = "+ result);
                        dbMethods.getTime(date)
                                .map(Long::parseLong)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(time -> {
                                    if (time != null)
                                        timeSwapBuff = time;
                                });
                    }else   Log.d("stas","result = "+ result);
                        }
                );


        startTime = 0;
        endTime = 0;
        isTimerRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting service");
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Binding service");
        }
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Destroying service");
        }
    }

    /**
     * Starts the timer
     */
    public void startTimer() {
        if (!isTimerRunning) {
            // startTime = System.currentTimeMillis();
            startTime = SystemClock.uptimeMillis();

            isTimerRunning = true;
        } else {
            Log.e(TAG, "startTimer request for an already running timer");
        }
    }


    public void stopTimer() {
        if (isTimerRunning) {

            timeSwapBuff += timeInMilliseconds;

            dbMethods.writeTimeToDB(date, String.valueOf(timeSwapBuff))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();

            isTimerRunning = false;
        } else {
            Log.e(TAG, "stopTimer request for a timer that isn't running");
        }
    }


    public boolean isTimerRunning() {
        return isTimerRunning;
    }





    public String getTime() {
        timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
        updatedTime = timeSwapBuff + timeInMilliseconds;
        int seconds = (int) (updatedTime / 1000);
        int hours = seconds / 3600;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    /**
     * Place the service into the foreground
     */
    public void foreground() {
        startForeground(NOTIFICATION_ID, createNotification());
    }

    /**
     * Return the service to the background
     */
    public void background() {
        stopForeground(true);
    }

    /**
     * Creates a notification for placing the service into the foreground
     *
     * @return a notification for interacting with the service when in the foreground
     */
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("VOMER-WIFI включен")
                .setContentText("Нажмите для просмотра информации")
                .setSmallIcon(R.mipmap.ic_launcher);

        Intent resultIntent = new Intent(this, TimeActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }
}