package com.example.amy01.mystudio;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyMap extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    static final int MIN_TIME = 5000;
    static final float MIN_DIST = 0;
    LocationManager mgr; //定位管理員
    private final static String TAG = "MapsActivity";
    public static final String LOG_TAG = "MyService";
    private Button btnStart,btn_start;
    private MyService mMyService = null;
    boolean isGPSEnabled; //GPS是否可用
    boolean isNetworkEnabled;
    double Latitude, Longitude;
    private Marker markerMe;
    private ArrayList<LatLng> traceOfMe;
    private LocationManager locationMgr;
    String where = "",timerecod,provider;
    long startTime = 0;
    GoogleMap map;
    LatLng currPoint;
    TextView txv,tv_step,timerTextView;
    private SensorManager sManager;
    private Sensor mSensorAccelerometer;
    private int step = 0;   //步數
    private double oriValue = 0,lstValue = 0,curValue = 0;
    private boolean motiveState = true,processState = false,buttonState;   //是否處於運動狀態/標記當前是否已經在計步/
    private Intent it;
    private Location lastLocation;
    private DrawerLayout drawerLayout;
    private NavigationView navigation_view;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);
        Intent intent = new Intent(this,MyService.class);
        startService(intent);
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkPermission();
        txv = (TextView) findViewById(R.id.timer);
        txv.setBackgroundColor(Color.argb(155, 0, 255, 0));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        timerTextView = (TextView) findViewById(R.id.timer);
        btnStart = (Button) findViewById(R.id.button);
        btnStart.setOnClickListener(StartOnClkLis);
        //計步器
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorAccelerometer = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       // sManager.registerListener((SensorEventListener) this,mSensorAccelerometer,SensorManager.SENSOR_DELAY_UI);
        bindViews();

        //選單開始
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigation_view = (NavigationView) findViewById(R.id.navigation_view);

        // 為navigatin_view設置點擊事件
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawer(GravityCompat.START);                // 點選時收起選單
                int id = item.getItemId();                // 取得選項id

                // 依照id判斷點了哪個項目並做相應事件
                if (id == R.id.action_friend) {
                    Intent gologin = new Intent(MyMap.this,Friend.class);
                    MyMap.this.startActivity(gologin);
                  //  Toast.makeText(MainActivity.this, "首頁", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else if (id == R.id.action_list) {
                    Intent gologin = new Intent(MyMap.this,List.class);
                    MyMap.this.startActivity(gologin);
                    return true;
                }
                else if (id == R.id.action_self) {
                    Intent gologin = new Intent(MyMap.this,Self.class);
                    MyMap.this.startActivity(gologin);
                    return true;
                }
                else if (id == R.id.action_project) {
                    Intent gologin = new Intent(MyMap.this,Project.class);
                    MyMap.this.startActivity(gologin);
                    return true;
                }
                else if (id == R.id.action_setting) {
                    Intent gologin = new Intent(MyMap.this,Setting.class);
                    MyMap.this.startActivity(gologin);
                    return true;
                }
                return false;
            }
        });
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerTextView.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
            timerecod = String.format("%d:%02d", minutes, seconds);
        }
    };

    private void bindViews() {
        btnStart.setOnClickListener(StartOnClkLis);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void  Notify(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Corgi Adventure")
                .setContentTitle("正在運動中")
                .setContentText("目前運動時間"+timerecod)
                //.setUsesChronometer(true) //設置顯示時間計時
                .setOngoing(true); //表示點即通知不會消失
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notificationManager.cancel(0); // 移除id值為0的通知
        notificationManager.notify(0, notification);
    }

    public void onSensorChanged(SensorEvent event) {
        double range = 12;   //設定一個精度範圍
        float[] value = event.values;
        curValue = magnitude(value[0], value[1], value[2]);   //計算當前的模
        //向上加速的状态
        if (motiveState == true) {
            if (curValue >= lstValue) lstValue = curValue;
            else {
                //檢測到一次峰值
                if (Math.abs(curValue - lstValue) > range) {
                    oriValue = curValue;
                    motiveState = false;
                }
            }
        }
        //向下加速的状态
        if (motiveState == false) {
            if (curValue <= lstValue) lstValue = curValue;
            else {
                if (Math.abs(curValue - lstValue) > range) {
                    //檢測到一次峰值
                    oriValue = curValue;
                    if (processState == true) {
                        step++;  //步數+ 1
                        if (processState == true) {
                            tv_step.setText(step + "");    //更新
                        }
                    }
                    motiveState = true;
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    //向量求模
    public double magnitude(float x, float y, float z) {
        double magnitude = 0;
        magnitude = Math.sqrt(x * x + y * y + z * z);
        return magnitude;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sManager.unregisterListener((SensorEventListener) this);
    }
    //計步器結束

    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder)
        {
            // TODO Auto-generated method stub
            mMyService = ((MyService.LocalBinder)serviceBinder).getService();
        }

        public void onServiceDisconnected(ComponentName name)
        {
            // TODO Auto-generated method stub
            Log.d(LOG_TAG, "onServiceDisconnected()" + name.getClassName());
        }
    };

    private View.OnClickListener StartOnClkLis
            = new View.OnClickListener()
    {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void onClick(View v)
        {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    try
                    {
                        if (b.getText().equals("stop")) { //按下結束按鈕
                            timerHandler.removeCallbacks(timerRunnable);
                            b.setText("start");
                            buttonState = false;
                            mMyService = null;
                            Toast.makeText(MyMap.this,"結束記錄你的運動路線",Toast.LENGTH_SHORT).show();
                            ShowMsg(String.format("您運動的時間為%s，已行動了%s步",timerecod,step));
                            unbindService(mServiceConnection);
                            stopService(it); //結束Service
                        } else { //開始時的按鈕
                            startTime = System.currentTimeMillis();
                            timerHandler.postDelayed(timerRunnable, 0);
                            b.setText("stop");
                        }
                    }
                    catch (Exception e)
                    {
                        return;
                    }
                }
            });
            buttonState = true;
            mMyService = null;
            Toast.makeText(MyMap.this,"開始記錄你的運動路線",Toast.LENGTH_SHORT).show();
            it = new Intent(MyMap.this, MyService.class);
            it.putExtra("traceOfMe",traceOfMe);
            bindService(it, mServiceConnection, BIND_AUTO_CREATE); //綁定Service
            startService(it); //開始Service
            Notify();
          //  trackToMe(lastLocation.getLatitude(),lastLocation.getLongitude());//按下按鈕後紀錄一次當下位置
        }
    };

    public void ShowMsg(String Msg){
        AlertDialog.Builder MyDialog = new AlertDialog.Builder(this);
        MyDialog.setTitle("運動紀錄");
        MyDialog.setMessage(Msg);
        MyDialog.show();
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }

    public void onRequestPermissionsResult(int requsetCode,String[] permissions, int[] grantResults) {
        if (requsetCode == 200) {
            if (grantResults.length >= 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "程式需要定位權限才可以運作", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        enableLocationUpdates(true);
    }

    protected void onPause() {
        super.onPause();
        enableLocationUpdates(false);
        Intent intent = new Intent(this,MyService.class);
        stopService(intent);
    }

    private void enableLocationUpdates(boolean isTurnOm) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (isTurnOm) {
                isGPSEnabled = mgr.isProviderEnabled((LocationManager.GPS_PROVIDER));
                isNetworkEnabled = mgr.isProviderEnabled((LocationManager.NETWORK_PROVIDER));

                if (!isGPSEnabled && !isNetworkEnabled) {
                    Toast.makeText(this, "請確認已開啟定位功能", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "取得定位資訊中", Toast.LENGTH_LONG).show();
                    if (isGPSEnabled)
                        mgr.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME, MIN_DIST, this);
                    if (isNetworkEnabled)
                        mgr.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME, MIN_DIST, this);
                }
            } else {
                mgr.removeUpdates(this);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //紀錄
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();
            lastLocation = location; //紀錄上次的location
            updateWithNewLocation(location);
            if (buttonState == true) {
                currPoint = new LatLng(location.getLatitude(),
                        location.getLongitude());
            }
            if (map != null && currPoint != null) {
                map.animateCamera(CameraUpdateFactory.newLatLng(currPoint));
            }
        } else
            txv.setText("無法取得定位資訊");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.d(TAG, "GPS_EVENT_STARTED");
                    Toast.makeText(MyMap.this, "GPS_EVENT_STARTED", Toast.LENGTH_SHORT).show();
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.d(TAG, "GPS_EVENT_STOPPED");
                    Toast.makeText(MyMap.this, "GPS_EVENT_STOPPED", Toast.LENGTH_SHORT).show();
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.d(TAG, "GPS_EVENT_FIRST_FIX");
                    Toast.makeText(MyMap.this, "GPS_EVENT_FIRST_FIX", Toast.LENGTH_SHORT).show();
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.d(TAG, "GPS_EVENT_SATELLITE_STATUS");
                    break;
            }
        }
    };

    private void showMarkerMe(double lat, double lng){
        if (markerMe != null) {
            markerMe.remove();
        }
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title("起點");
        markerMe = map.addMarker(markerOpt);
        Toast.makeText(this,String.format("您所在位置為【經度:%.5f度；緯度:%.5f度】",lat,lng) , Toast.LENGTH_SHORT).show();
    }

    private void cameraFocusOnMe(double lat, double lng){
        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }
    PolylineOptions polylineOpt = new PolylineOptions();
    private void trackToMe(double lat, double lng){
        if (traceOfMe == null) {
            traceOfMe = new ArrayList<LatLng>();
            showMarkerMe(lat, lng);
        }
       /* for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }*/
        if (buttonState == true) {
            traceOfMe.add(new LatLng(lat, lng));
            polylineOpt.add(new LatLng(lat, lng));
            polylineOpt.color(Color.BLACK);
            Polyline line = map.addPolyline(polylineOpt);
            line.setWidth(10);
        }
    }

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            //經度
            double lng = location.getLongitude();
            //緯度
            double lat = location.getLatitude();
            //速度
            float speed = location.getSpeed();
            //時間
            long time = location.getTime();
            String timeString = getTimeString(time);
            where = "經度: " + lng +
                    "\n緯度: " + lat +
                    "\n速度: " + speed +
                    "\n時間: " + timeString +
                    "\nProvider: " + provider;
            //"我"
            cameraFocusOnMe(lat, lng);
            trackToMe(lat, lng);
        }else{
            where = "No location found.";
        }
        //顯示資訊
        //txtOutput.setText(where);
    }

    private String getTimeString(long timeInMilliseconds){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }

        @Override
    protected void onStop() {
        super.onStop();
        //locationMgr.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(String s) {

    }


    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;
        MarkerOptions markerOpt = new MarkerOptions();
        LatLng sydney = new LatLng(Latitude, Longitude);
        markerOpt.position(sydney);
        markerOpt.title("你的位置");
        markerOpt.draggable(true); //可移動標記點
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)); //標記點的顏色
        markerOpt.visible(true);
        markerOpt.anchor(0.5f, 0.5f);//設為圖片中心

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap(); //取得地圖物件
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //地圖類型設定
        map.setMyLocationEnabled(true); // 右上角的定位功能；這行會出現紅色底線，不過仍可正常編譯執行
        map.getUiSettings().setZoomControlsEnabled(true);  // 右下角的放大縮小功能
        map.getUiSettings().setCompassEnabled(true);       // 左上角的指南針，要兩指旋轉才會出現
        map.getUiSettings().setMapToolbarEnabled(true);    // 右下角的導覽及開啟 Google Map功能

        Log.d(TAG, "最高放大層級："+map.getMaxZoomLevel());
        Log.d(TAG, "最低放大層級："+map.getMinZoomLevel());

        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //設定地圖為普通街道
        map.moveCamera(CameraUpdateFactory.zoomTo(18)); //地圖縮放級數
    }

}
