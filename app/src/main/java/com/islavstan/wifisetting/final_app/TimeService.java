package com.islavstan.wifisetting.final_app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.islavstan.wifisetting.R;
import com.islavstan.wifisetting.api.ApiClient;
import com.islavstan.wifisetting.points.Point;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.os.Handler;

import cc.mvdan.accesspoint.WifiApControl;
import rx.Observable;
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
    private final static int MSG_CHECK_INTERNET = 100;
    private final static int REMOVE_CHECK_INTERNET = 200;

    // Service binder
    private final IBinder serviceBinder = new TimeService.RunServiceBinder();


    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
    String date = df.format(c.getTime());


    private final Handler mUpdateTimeHandler = new TimeService.CheckHandler(this);
    boolean internet = true;
    WifiApControl apControl;

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
        Log.d("stas", "date = " + date);
       /* dbMethods.checkDate(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result == 1) {
                        Log.d("stas", "result = " + result);
                        dbMethods.getTime(date)
                                .map(Long::parseLong)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(time -> {
                                    if (time != null)
                                        timeSwapBuff = time;
                                });
                    } else Log.d("stas", "result = " + result);
                        }
                );*/


        startTime = 0;
        endTime = 0;
        isTimerRunning = false;
    }


    private String getTodayDate() {
        final String[] date = new String[1];
        final Point apiService = ApiClient.getRxRetrofit().create(Point.class);
        apiService.retrieveDate("e65837978381fdb0634e294698dca5d6", "getDate")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dateResponse -> {
                    if (dateResponse.isSuccessful()) {
                        com.islavstan.wifisetting.model.Date d = dateResponse.body();
                        date[0] = d.getDate();

                    }


                }, error -> Log.d("stas", "getTodayDate error = " + error.getMessage()));


        return date[0];
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


    public void startTimer() {
        if (!isTimerRunning) {
//если приложение свернуть и наступит новый день дата останется прежней как и timeSwapBuff, нужно и дату здесь получать заново

            dbMethods.checkDate(date)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                                if (result == 1) {
                                    Log.d("stas", "result = " + result);
                                    dbMethods.getTime(date)
                                            .map(Long::parseLong)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(time -> {
                                                if (time != null)
                                                    timeSwapBuff = time;
                                            });
                                } else Log.d("stas", "result = " + result);
                            }
                    );


            onWifiHotspot()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> Log.d("stas", "onWifiHotspot start"),
                            error -> Log.d("stas", "onWifiHotspot error = " + error.getMessage()));


            startTime = SystemClock.uptimeMillis();
            isTimerRunning = true;
            internet = true;
            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_CHECK_INTERNET, 5000);
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
            mUpdateTimeHandler.removeMessages(MSG_CHECK_INTERNET);
        } else {
            Log.e(TAG, "stopTimer request for a timer that isn't running");
        }
    }


    public boolean isTimerRunning() {
        return isTimerRunning;
    }


    private void checkInternet() {
        Log.d("stas", "checkInternet");


        if (!isMobileConnected(getApplicationContext()) || !apControl.isEnabled()) {
            Log.d("stas", "stop timer from checkInternet");
            stopTimer();
            mUpdateTimeHandler.sendEmptyMessage(REMOVE_CHECK_INTERNET);
            internet = false;

        }


    }


    public boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }


    public String getTime() {
        if (!internet) {
            return null;
        } else {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int seconds = (int) (updatedTime / 1000);
            int hours = seconds / 3600;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }


    private Observable<Void> onWifiHotspot() {
        return Observable.create(subscriber -> {


            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = "VOMER";
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            try {
                Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

                Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
                while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
                }
                ;
                Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
                int apstate = (Integer) getWifiApStateMethod.invoke(wifiManager);
                Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
                netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
                Log.d("stas", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");

            } catch (Exception e) {
                Log.e(this.getClass().toString(), "", e);
            }

            apControl = WifiApControl.getInstance(this);
            apControl.enable();

        });

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


    private static class CheckHandler extends Handler {
        private final static int UPDATE_RATE_MS = 10000;
        private final WeakReference<TimeService> service;

        CheckHandler(TimeService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_INTERNET:
                    service.get().checkInternet();
                    sendEmptyMessageDelayed(MSG_CHECK_INTERNET, UPDATE_RATE_MS);
                    break;
                case REMOVE_CHECK_INTERNET:
                    this.removeMessages(MSG_CHECK_INTERNET);
                    break;


            }

        }
    }
}