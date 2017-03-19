package com.islavstan.wifisetting.final_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.islavstan.wifisetting.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import cc.mvdan.accesspoint.WifiApControl;

public class TimeActivity extends AppCompatActivity {
    FloatingActionButton fab;
    Button stopService;
    TextView timer;
    private boolean serviceBound;
    private TimeService timeService;
    // Handler to update the UI every second when the timer is running
    private final Handler mUpdateTimeHandler = new TimeActivity.UIUpdateHandler(this);

    // Message type for the handler
    private final static int MSG_UPDATE_TIME = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        timer = (TextView) findViewById(R.id.timer);
        stopService = (Button) findViewById(R.id.stop);


        fab.setOnClickListener(v -> {

            if (isMobileConnected(TimeActivity.this)) {//если есть интернет то запускаем таймер и вайфай раздачу
                onWifiHotspot();
                if (serviceBound && !timeService.isTimerRunning()) {
                    Log.d("stas", "Starting timer");
                    timeService.startTimer();
                    mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                }


            } else showNo3gpDialog();


        });

        stopService.setOnClickListener(v -> {
            if (serviceBound && timeService.isTimerRunning()) {
                Log.d("stas", "Stopping timer");
                timeService.stopTimer();
                mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            }
        });


    }


    private void onWifiHotspot() {
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

        WifiApControl apControl = WifiApControl.getInstance(this);

        apControl.enable();

    }


    private void showNo3gpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
        builder.setTitle("Важно!")
                .setMessage("Для работы сервиса включите мобильный интернет!")
                .setCancelable(true)
                .setNegativeButton("ОК",
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();


    }


    public boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }


    @Override
    protected void onStop() {
        super.onStop();
        mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        if (serviceBound) {
            // If a timer is active, foreground the service, otherwise kill the service
            if (timeService.isTimerRunning()) {
                timeService.foreground();
            } else {
                stopService(new Intent(this, TimeService.class));
            }
            // Unbind the service
            unbindService(connection);
            serviceBound = false;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("stas", "Starting and binding service");
        Intent i = new Intent(this, TimeService.class);
        startService(i);
        bindService(i, connection, 0);
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimeService.RunServiceBinder binder = (TimeService.RunServiceBinder) service;
            timeService = binder.getService();
            serviceBound = true;
            // Ensure the service is not in the foreground when bound
            timeService.background();
            if (timeService.isTimerRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                Log.d("stas", "sendEmptyMessage from ServiceConnection");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("stas", "Service disconnect");
            serviceBound = false;
        }
    };

    private void updateUITimer() {
        if (serviceBound) {
            if (timeService.getTime() == null) {
                Log.d("stas", "internet = false");
                mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            } else

                timer.setText(timeService.getTime());
        }
    }


   private static class UIUpdateHandler extends Handler {

        private final static int UPDATE_RATE_MS = 1000;
        private final WeakReference<TimeActivity> activity;

        UIUpdateHandler(TimeActivity activity) {

            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (MSG_UPDATE_TIME == message.what) {
                activity.get().updateUITimer();
                sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS);
            }
        }
    }


}
