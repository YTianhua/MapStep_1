package com.yth520web.mapstep;

import android.Manifest;
import android.app.VoiceInteractor;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.Layout;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**杨天华 / 2019 / 6 / 1至2019/6/？
     * 运动APP
     * */
    public LocationClient mLocationClient;
   //List<String> permissionList;//用于申请权限
    private TextView mTxtView;//用于检测是否成功获取地址当前位置
    private MapView mapView;//显示地图
    private BaiduMap baiduMap;//将地图移动到我
    private boolean isFirstLocate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**记录轨迹，思路如下
         * （1）点与点之间连成线，在百度地图MapView中绘制轨迹
         * （2）获取定位点List<LatLng>：通过百度定位sdk：LocationClient类获取
         * ，户外运动画运动轨迹，要求位置点的精度高，所以我们必须使用gps定位类型的位置结果。
         */
        /**
         * 首先需要到是否能成功获取经纬度信息
         * 自定义一个TextView用于检测
         */

        mLocationClient = new LocationClient(getApplicationContext());
        //MyLocationListener继承至百度的监听接口
        mLocationClient.registerLocationListener(new MyLocationListener());

        SDKInitializer.initialize(getApplicationContext());//初始化操作，须在setContentView前调用
        setContentView(R.layout.main_layout);
        mTxtView = (TextView)findViewById(R.id.mTextView);
        mapView = (MapView)findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();//将地图移动到我i当前位置
        baiduMap.setMyLocationEnabled(true);//用小光标显示我的位置

        Log.i("申请权限：","True");
        //启动时候请求权限,同意所有权限后启动requestLocation()
        List<String> permissionList = new ArrayList<>();
        //申请权限，否则finish()活动
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ;
        {   //*GPS定位*//*
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ;
        {  //* 动态申请权限*//*
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ;
        {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String permission[] = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permission, 1);
        }else {
            Log.i("用户已同意全部权限：","True");
            requestLocation();
            mTxtView.setText("启动requestLocation()---2;");
        }
        requestLocation();
        mTxtView.setText("启动requestLocation()---1;");
    }
    private void requestLocation(){
        initLocation();//定时刷新百度地图
        mLocationClient.start();
    }
    public void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(2000);//2秒刷新下位置信息
        mLocationClient.setLocOption(option);
    }
    private void natigateTo(BDLocation bdLocation){
        if(isFirstLocate){
            LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            MapStatusUpdate  update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate = false;
        }
        //用小光标显示我当前位置
        MyLocationData.Builder builder = new MyLocationData.Builder();
        //获得当前经纬度，并用光标显示
        builder.latitude(bdLocation.getLatitude());
        builder.longitude(bdLocation.getLongitude());
        MyLocationData locationData =builder.build();
        baiduMap.setMyLocationData(locationData);
    }
    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            if(bdLocation.getLocType() ==BDLocation.TypeGpsLocation
                    ||bdLocation.getLocType() ==BDLocation.TypeNetWorkLocation){
                natigateTo(bdLocation);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuffer currentPosition = new StringBuffer();
                    currentPosition.append("纬度：").append(bdLocation.getLatitude()).append("\n");
                    currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
                    currentPosition.append("定位方式：");
                    if(bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else {
                        currentPosition.append("网络");
                    }
                    mTxtView.setText(currentPosition);
                    Log.i("当前经纬度信息是：",currentPosition+"");
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result :grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意全部权限才可使用",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }else{
                    Toast.makeText(this,"发生未知错误",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }
/*// 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
//获取运动后的定位点
        //coordinateConvert();
//设置缩放中点LatLng target，和缩放比例
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(target).zoom(18f);
//地图设置缩放状态
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));*/
/**
 * 配置线段图层参数类： PolylineOptions
 * ooPolyline.width(13)：线宽
 * ooPolyline.color(0xAAFF0000)：线条颜色红色
 * ooPolyline.points(latLngs)：List<LatLng> latLngs位置点，将相邻点与点连成线就成了轨迹了
 */
        /*OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(latLngs);
//在地图上画出线条图层，mPolyline：线条图层
        Polyline mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
        mPolyline.setZIndex(3);*/

        /*
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        //联系viewpager和pageAdapter
        viewPager.setAdapter(pagerAdapter);
        tabLayout = (TabLayout)findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        */
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);//退出程序时候，将我当前位置“去掉”
    }
}


