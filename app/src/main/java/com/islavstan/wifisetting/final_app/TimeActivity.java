package com.islavstan.wifisetting.final_app;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.islavstan.wifisetting.R;
import com.islavstan.wifisetting.adapter.WifiDaysRecAdapter;
import com.islavstan.wifisetting.model.Day;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.islavstan.wifisetting.model.Point;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import cc.mvdan.accesspoint.WifiApControl;
import de.hdodenhof.circleimageview.CircleImageView;
import io.ghyeok.stickyswitch.widget.StickySwitch;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TimeActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    TextView timer;
    private boolean serviceBound;
    private TimeService timeService;
    // Handler to update the UI every second when the timer is running
    private final Handler mUpdateTimeHandler = new TimeActivity.UIUpdateHandler(this);
    RecyclerView recycler;
    WifiDaysRecAdapter adapter;
    DBMethods dbMethods;
    TextView daysOnline;

    // Message type for the handler
    private final static int MSG_UPDATE_TIME = 0;
    private final static int REMOVE_UPDATE = 1;
    List<Day> dayList = new ArrayList<>();
    boolean fabPressed = false;
    public Socket socket;
    MapView mapView;
    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Circle mCircle;
    Marker mMarker;

    boolean moveCamera = true;

    private ViewGroup infoWindow;
    private TextView infoName;
    CircleImageView photo;

    int myId = 344;
    Map<Integer, Point> pointsList = new HashMap<>();

    private static final int REQUEST_WRITE_SETTINGS = 1;
    private static final int REQUEST_LOCATION = 2;

    RecyclerView graphRecycler;
    GraphicAdapter graphicAdapter;
    List<GraphicModel>graphicModelList = new ArrayList<>();
    StickySwitch stickySwitch;
    WifiManager wifiManager;
    //http://stackoverflow.com/questions/14826345/android-maps-api-v2-change-mylocation-icon

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        stickySwitch = (StickySwitch) findViewById(R.id.fab2);
        stickySwitch.setOnSelectedChangeListener((direction, s) -> {
            if (direction.equals(StickySwitch.Direction.RIGHT)) {
                //on
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(this)) {
                    showPermissionDialog();
                    stickySwitch.setDirection(StickySwitch.Direction.LEFT);
                } else {

                    if (!fabPressed) {

                        if (getMobileInternet()) {//если есть интернет то запускаем таймер и вайфай раздачу

                            if (serviceBound && !timeService.isTimerRunning()) {
                                wifiManager.setWifiEnabled(false);
                                Log.d("stas", "Starting timer");
                                timeService.startTimer();
                                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                                fabPressed = true;
                            }


                        } else {
                            showNo3gpDialog();
                            stickySwitch.setDirection(StickySwitch.Direction.LEFT);
                        }

                    }

                }
            } else {
                //off
                if (serviceBound && timeService.isTimerRunning()) {
                    Log.d("stas", "Stopping timer");
                    timeService.stopTimer();
                    mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
                    fabPressed = false;


                }

            }
        });

        getLocationPermission();
        dbMethods = new DBMethods(this);
        timer = (TextView) findViewById(R.id.timer);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        socket = new MySocket(this).GetSocket();


        this.infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.info_window, null);
        this.infoName = (TextView) infoWindow.findViewById(R.id.name);
        this.photo = (CircleImageView) infoWindow.findViewById(R.id.photo);


        setAdapter();
        setDaysOnline();
        setMap(savedInstanceState);
        setGraphic();


        initSocket();
        socket.on("newLocationWifi", getPoints);
        socket.on("deletePointLocationWiFI", deletePoint);
        getAllLocationWifi();


    }










    private void getLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            List<String> permissions = new ArrayList<>();
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_LOCATION);
            }
        }

    }



    private void setGraphic() {
        graphRecycler = (RecyclerView) findViewById(R.id.graph_recycler);
        graphicAdapter = new GraphicAdapter(graphicModelList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(TimeActivity.this, LinearLayoutManager.HORIZONTAL, false);
        graphRecycler.setLayoutManager(mLayoutManager);
        graphRecycler.setItemAnimator(new DefaultItemAnimator());
        graphRecycler.setAdapter(graphicAdapter);

        dbMethods.getGraphicList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                            graphicModelList.addAll(list);
                        }, error -> Log.d("stas", "getGraphicList error = " + error.getMessage())
                );
    }








    private void showPermissionDialog(){
        new AlertDialog.Builder(this)
                .setMessage("Разрешить чтение и запись настроек системы? Необходимо для работы точки доступа")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivityForResult(intent, REQUEST_WRITE_SETTINGS);
                }).show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_SETTINGS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "Достут получен.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Разрешить чтение и запись настроек системы?", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private boolean getMobileInternet() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        } else
            return false;


    }


    private void initSocket() {


        socket.emit("Vomer syncLogin", myId, 4620700, 8229, 2, (Ack) args -> {
        });

    }

    public void getAllLocationWifi() {

        socket.emit("getAllLocationWifi", myId, (Ack) args -> {
            Log.d("VOMER_DATA", "GetContactAndroid" + args[0].toString());
            String str = args[0].toString();
            JSONObject data = null;
            try {
                data = new JSONObject(str);
                Log.d("stas", "data = " + data);
                Iterator<String> pointList = data.keys();
                for (int i = 0; i < data.length(); i++) {
                    String index = pointList.next();
                    try {
                        JSONObject user = data.getJSONObject(index);

                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        Point point = gson.fromJson(user.toString(), Point.class);

                        String longitude = point.longitude;

                        String latitude = point.latitude;
                        Log.d("stas", longitude + " " + latitude);
                        LatLng latLng = new LatLng(Double.parseDouble(longitude), Double.parseDouble(latitude));
                        final MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(point.fio + "\n" + point.number);
                        markerOptions.snippet(point.path);
                        if (point.userID != myId) {
                            pointsList.put(point.userID, point);
                            runOnUiThread(() -> {
                                Marker m = mMap.addMarker(markerOptions);
                                m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map2));
                            });
                        }

                    } catch (JSONException e) {
                        Log.d("VOMER_DATA", " GetContactAndroid  JSONException = " + e.getMessage());
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private Emitter.Listener getPoints = args -> runOnUiThread(() -> {
        String a = args[0].toString();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Point point = gson.fromJson(a, Point.class);
        int id = point.userID;
        if (id != myId) {
            String longitude = point.longitude;
            String latitude = point.latitude;
            Log.d("stas", longitude + " " + latitude);
            LatLng latLng = new LatLng(Double.parseDouble(longitude), Double.parseDouble(latitude));
            final MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(point.fio + "\n" + point.number);
            markerOptions.snippet(point.path);
            pointsList.put(point.userID, point);
            Marker m = mMap.addMarker(markerOptions);
            m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map2));
        }

    });


    private Emitter.Listener deletePoint = args -> runOnUiThread(() -> {
        String a = args[0].toString();
        try {
            JSONObject object = new JSONObject(a);
            int userId = object.getInt("userID");
            pointsList.remove(userId);
            mMap.clear();
            for (int i = 0; i < pointsList.size(); i++) {
                Point p = pointsList.get(i);
                LatLng latLng = new LatLng(Double.parseDouble(p.longitude), Double.parseDouble(p.latitude));
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(p.fio + "\n" + p.number);
                markerOptions.snippet(p.path);
                Marker m = mMap.addMarker(markerOptions);
                m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map2));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    });


    private void setMap(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    infoName.setText(marker.getTitle());
                    Picasso.with(infoWindow.getContext()).load("https://vomer.com.ua/uploads/min_" + marker.getSnippet()).into(photo);
                    return infoWindow;
                }
            });


            mapView.onResume();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(TimeActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                    mMap.setMyLocationEnabled(true);

                }
            } else {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);


            }


            mMap.setOnMyLocationChangeListener(location -> {

                if (moveCamera) {

                    socket.emit("newLocationWifi", location.getLatitude(), location.getLongitude(), (Ack) args -> {
                        JSONObject data = null;
                        try {
                            data = new JSONObject(args[0].toString());
                        } catch (JSONException e) {
                            Log.d("VOMER_DATA", " syncLogin  JSONException = " + e.getMessage());
                        }
                        if (data != null) {
                            try {
                                Log.d("VOMER_DATA", "syncLogin" + data);
                                String result = data.getString("result");

                                if (result.equals("done")) {
                                    Log.d("stas", "done");

                                }

                            } catch (JSONException e) {
                                Log.d("VOMER_DATA", " syncLogin  JSONException = " + e.getMessage());

                            }
                        }
                    });


                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(11);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);
                    moveCamera = false;

                }





              /*  LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if(mCircle == null || mMarker == null){
                    drawMarkerWithCircle(latLng);
                }else{
                    updateMarkerWithCircle(latLng);
                }

*/


            });
        });


    }


    private void drawMarkerWithCircle(LatLng position) {
        double radiusInMeters = 100.0;
        int strokeColor = 0xffff0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill

        CircleOptions circleOptions = new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCircle = mMap.addCircle(circleOptions);

        MarkerOptions markerOptions = new MarkerOptions().position(position);
        mMarker = mMap.addMarker(markerOptions);
    }

    private void updateMarkerWithCircle(LatLng position) {
        mCircle.setCenter(position);
        mMarker.setPosition(position);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private void setAdapter() {
        dbMethods.getDaysList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> dayList.addAll(list), error -> Log.d("stas", "getDaysList error = " + error.getMessage()));


        recycler = (RecyclerView) findViewById(R.id.recycler);
        adapter = new WifiDaysRecAdapter(dayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(mLayoutManager);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(adapter);


    }


    private void setDaysOnline() {
        daysOnline = (TextView) findViewById(R.id.daysOnline);
        dbMethods.getDaysCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(days -> daysOnline.setText("Дней онлайн: " + days + "/30"), error -> Log.d("stas", "getDaysCount error = " + error.getMessage()));
    }


    private void showNo3gpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TimeActivity.this);
        builder.setTitle("Важно!")
                .setMessage("Для работы сервиса отключите Wi-Fi и включите мобильный интернет!")
                .setCancelable(true)
                .setNegativeButton("ОК",
                        (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();


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
                fabEnable();
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
                mUpdateTimeHandler.sendEmptyMessage(REMOVE_UPDATE);
                fabDisable();

            } else

                timer.setText(timeService.getTime());
        }
    }

    private void fabEnable() {
        fabPressed = true;
        stickySwitch.setDirection(StickySwitch.Direction.RIGHT);

    }

    private void fabDisable() {
        stickySwitch.setDirection(StickySwitch.Direction.LEFT);
        fabPressed = false;
    }


    private static class UIUpdateHandler extends Handler {

        private final static int UPDATE_RATE_MS = 1000;
        private final WeakReference<TimeActivity> activity;

        UIUpdateHandler(TimeActivity activity) {

            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_UPDATE_TIME:
                    activity.get().updateUITimer();
                    sendEmptyMessageDelayed(MSG_UPDATE_TIME, UPDATE_RATE_MS);
                    break;
                case REMOVE_UPDATE:
                    this.removeMessages(0);
                    break;

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.emit("deletePointLocationWiFI", myId);
        socket.disconnect();
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

            WifiApControl apControl = WifiApControl.getInstance(this);

            apControl.enable();
        });


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


    private void getActiveNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Toast.makeText(this, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Toast.makeText(this, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "NONE", Toast.LENGTH_SHORT).show();
        }


    }

    public boolean isMobileConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return ((netInfo != null) && netInfo.isConnected());
    }

    public void checkPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_SETTINGS);

        }

    }


}
