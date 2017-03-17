package com.islavstan.wifisetting.working_hours;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MyService extends Service {

    Long startTime;
    long millis;
    int seconds;
    int minutes;
    private volatile long elapsedTime;

    private final MyBinder myBinder = new MyBinder();

    public class MyBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
       return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
