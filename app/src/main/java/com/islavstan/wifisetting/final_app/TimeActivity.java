package com.islavstan.wifisetting.final_app;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.islavstan.wifisetting.R;
import com.islavstan.wifisetting.TimerActivity;

import java.lang.ref.WeakReference;

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


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (serviceBound && !timeService.isTimerRunning()) {
                    Log.d("stas", "Starting timer");
                    timeService.startTimer();
                    mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                }

            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceBound && timeService.isTimerRunning()) {
                    Log.d("stas", "Stopping timer");

                    timeService.stopTimer();
                    mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

                }
            }
        });


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
            timer.setText(timeService.elapsedTime() + " seconds");
        }
    }

    /**
     * When the timer is running, use this handler to update
     * the UI every second to show timer progress
     */
    static class UIUpdateHandler extends Handler {

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
