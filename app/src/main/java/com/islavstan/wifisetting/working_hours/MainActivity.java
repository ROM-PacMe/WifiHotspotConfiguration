package com.islavstan.wifisetting.working_hours;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.islavstan.wifisetting.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    MapView mapView;
    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    FloatingActionButton fab;
    Button stopService;
    Intent intent;
    private MyBroadcastReceiver mMyBroadcastReceiver;
    TextView timer;
    private TimerIntentService timerIntentService;
    private boolean bound = false;


   /* private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            TimerIntentService.MyBinder timeBinder = (TimerIntentService.MyBinder) binder;
            timerIntentService = timeBinder.getService();
            bound = true;
            Log.d("stas2","bound = "+bound );
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerIntentService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        timer = (TextView) findViewById(R.id.timer);

        stopService = (Button) findViewById(R.id.stop);

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent != null) {
                    stopService(intent);
                    intent = null;
                }
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MainActivity.this, TimerIntentService.class);
                startService(intent);
            }
        });


        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mapView.onResume();
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);
                }
            }
        });


//http://stackoverflow.com/questions/17152847/create-wifi-hotspot-configuration-in-android
//http://stackoverflow.com/questions/32817552/how-to-enable-mobile-data-on-off-programmatically
      /*  WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


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

        Log.d("stas", isMobileConnected(this) + " internet ");
    }


    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }*/

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int minutes = intent.getIntExtra("minutes", 0);
            int seconds = intent.getIntExtra("seconds", 0);
            Log.d("stas", seconds + "sec in broadcast");
            timer.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }


 /*   private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int minutes = intent.getIntExtra("minutes",0);
            int seconds = intent.getIntExtra("seconds",0);
            Log.d("stas1", seconds +"sec in broadcast2");
            timer.setText(String.format("%02d:%02d", minutes, seconds));
        }
    };*/

    @Override
    protected void onResume() {

        super.onResume();
        mMyBroadcastReceiver = new MyBroadcastReceiver();
        // регистрируем BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(TimerIntentService.ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(mMyBroadcastReceiver, intentFilter);
        // LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        unregisterReceiver(mMyBroadcastReceiver);
        //  LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }

    @Override
    protected void onPause() {

        super.onPause();
        unregisterReceiver(mMyBroadcastReceiver);
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
      /*  if (bound) {
            unbindService(connection);
            bound = false;
        }*/
    }
}



