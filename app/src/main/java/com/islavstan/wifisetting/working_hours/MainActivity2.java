package com.islavstan.wifisetting.working_hours;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.islavstan.wifisetting.R;

public class MainActivity2 extends AppCompatActivity {
    TextView timer;
    FloatingActionButton fab;
    Button stopService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        timer = (TextView)findViewById(R.id.timer);
        stopService = (Button)findViewById(R.id.stop);


    }
}
