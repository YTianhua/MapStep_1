package com.yth520web.mapstep;

import android.Manifest;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PatternMatcher;
import android.os.SystemClock;
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
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    /**杨天华 / 2019 / 6 / 1至2019/6/？
     * 运动APP
     * */
    public LocationClient locationClient;
    private TextView mTxtView;//用于检测是否成功获取地址当前位置
    Chronometer timer;//计时器，参考https://www.cnblogs.com/liushilin/p/5802954.html
    TextView showdistance;//利用百度地图自带的DistanceUtil类计算距离并显示在TextView中
    Double distance=0.0;
     List list_distance =new ArrayList();//存放距离的数组
    int count=0;
    private MapView mapView;//显示地图
    private BaiduMap baiduMap;//将地图移动到我当前位置
    //获取纬度信息
    double latitude;
    //获取经度信息
    double longitude;
    List<LatLng> points = new ArrayList<LatLng>();;//用于存放坐标点，然后绘制折线图
    Button button_start,button_stop;//两个按钮，点击时开始和暂停绘图，stop长按时候停止绘图
    Boolean run_isStart =false,run_isStop=false;//判断是否开始和停止,检测用户是否暂停
    Boolean showMyLocation=true;//用于显示是否展示我的位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //显示地图，需要放在setContentView(R.layout.main_layout);前，否则会报错
        SDKInitializer.initialize(getApplicationContext());
        //转换坐标，不然会发生位置的偏移
        Log.i("原本坐标类型：",SDKInitializer.getCoordType()+"");
        SDKInitializer.setCoordType(CoordType.GCJ02);//默认为BD09LL坐标
        setContentView(R.layout.main_layout);
        mapView = (MapView)findViewById(R.id.bmapView);
        mTxtView = (TextView)findViewById(R.id.mTextView);
        //在MyLocationListener实现开始，暂停和结束的功能
        button_start = (Button)findViewById(R.id.map_start);
        button_stop = (Button)findViewById(R.id.map_stop);
        timer = (Chronometer) findViewById(R.id.timer);//计时器
        showdistance = (TextView) findViewById(R.id.distance);//API自带的计算距离方法DistanceUtil
        initLocationOption();//启动地图，获取位置信息
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 当点击开始时，需要做：
                 * （1）在起点标记一个Marker
                 * （2）计时器
                 * （3）记录行走过的路程并绘制轨迹
                 * （4）实现暂停和继续的效果
                 */
                if (run_isStart==false){
                    run_isStart=true;
                    showMyLocation=false;//开始跑步后便不再显示我的位置
                    Log.i("开始运动：","1");
                    initLocationOption();//再次启动地图，获取位置信息
                    //启动计时器
                    timer.setBase(SystemClock.elapsedRealtime());//计时器清零
                    int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
                    timer.setFormat("0"+String.valueOf(hour)+":%s");
                    timer.start();

                }

            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**点击停止按钮时，需要实现：
                 * （1）停止获得位置信息和轨迹的绘制
                 * （2）在终点打上一个Marker
                 * （3）停止计时器
                 */
                run_isStop=true;
                initLocationOption();//执行initLocationOption中的stop方法
                //locationClient.stop();
                //停止计时器
                timer.stop();
            }
        });

        //将地图移动到我当前位置,定义方法navigeteTo,获取位置时候启动方法navigeteTo
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);//用一个光标显示我的位置

        //申请权限，用RequestPermission这个类来专门放置申请的相关权限
        //申请权限的方法写在MainACtivity的最下方
        RequestPermission rq = new RequestPermission();
        rq.requestPermission(MainActivity.this);
        if (rq.permissionList.isEmpty()==false){
            //启动申请权限的方法
            String permission[] = rq.permissionList.toArray(new String[rq.permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permission, 1);
        }
    }


    /**
     * 初始化定位参数配置
     */
    private void initLocationOption() {
//定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        locationClient = new LocationClient(getApplicationContext());
//声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
//注册监听函数
        locationClient.registerLocationListener(myLocationListener);
//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        //locationOption.setCoorType("gcj02");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(2000);
//可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
//可选，设置是否需要地址描述
        //locationOption.setIsNeedLocationDescribe(true);
//可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
//可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);
//开始定位
        locationClient.start();
    }
    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){
            //当定位方式为网络定位或gps定位，则启动navigateTo将位置移动到我当前位置
            if(location.getLocType()==BDLocation.TypeGpsLocation
                    ||location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
                Log.i("启动navigateTo：",true+"");
                //return;
            }
            //开始运动，启动相关功能
            if (run_isStart==true){
                start_run(location);//绘制轨迹
                start_marker(location);//绘制一个起点
            }
           //暂停运动，启动相关功能
            /**
             * 停止绘制轨迹
             * 开始绘制我所在位置的Marker点
             */
            if(run_isStart==true) {
                locationClient.stop();
            }
            //继续运动，启动相关功能
            if(run_isStart==true) {
                locationClient.restart();
            }
            //停止运动，启动相关功能
            if (run_isStop==true){
                stop_run(location);
                Log.i("停止地图","true");
            }
            //获取纬度信息
            latitude = location.getLatitude();
            //获取经度信息
            longitude = location.getLongitude();
            //获取定位精度，默认值为0.0f
            float radius = location.getRadius();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            String type=null;
            if(location.getLocType()==BDLocation.TypeGpsLocation){
                type = "GPS";
            }
            if(location.getLocType()==BDLocation.TypeNetWorkLocation){
                type = "网络";
            }
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();
            mTxtView.setText(latitude+"\n"+longitude+"\n"+radius+"\n"+type+"\n"+errorCode+"\n");
        }

    }
//将地图移动到我当前位置
private void navigateTo (BDLocation bdlocation){
        //获取目前所在的位置，由于SpanScan方法，该方法2每秒获取一次位置
        LatLng ll =new LatLng(bdlocation.getLatitude(),bdlocation.getLongitude());
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(18f);
        baiduMap.animateMapStatus(update);

         /**
          * 没有跑之前，每隔2秒获取我当前位置，并用一个Marker标出来
          * 同时清除上一个Marker，防止出现地图出现多个Marker的情况
          */
        if (run_isStart==false) {
            baiduMap.clear();
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.mylocation);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(ll)
                    .icon(bitmap);
            Log.i("未开始运动，仅记录位置信息：", ll + "");
            //在地图上添加Marker，并显示
            baiduMap.addOverlay(option);
        }
    /**停止跑步情况
     * 停止绘制轨迹
     * 绘制一个终点Marker
     * 是否需要baiduMap停止运行？
     * 是否需要baiduClient客户端暂停运行？
     */
    }
    public void start_run(BDLocation bdLocation){
        /**
         * 跑步之后，仅绘制一个开始的Marker，清除上面没跑步前的Marker
         * 同时清除上一个Marker，防止出现地图出现多个Marker的情况
         */
        baiduMap.clear();//清除MyLocation，重新绘画
        LatLng ll =new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
        //绘制轨迹最少需要两个点，为防止程序崩溃提前添加两个起点
        if (points.size()<2){
            points.add(ll);
            points.add(ll);
        }
        else{
            points.add(ll);//添加正常的点
        }
        //利用API自带的DistanceUtil计算距离
        distance = DistanceUtil.getDistance(points.get(count), points.get(count+1));
        showdistance.setText(distance+"");
        //用一个数组存放行走的距离
        list_distance.add(distance);
        //Log.i("尝试获取LatLng数组元素》》》",points.get(count)+",count="+count);
        count++;
        //设置折线的属性
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        Log.i("开始运动但未暂停2222222：",ll+"");
        //在地图上绘制折线
        // mPloyline 折线对象
        Overlay mPolyline = baiduMap.addOverlay(mOverlayOptions);
    }
    //点击开始按钮，绘制起点Marker的方法
    public void start_marker(BDLocation bdlocation) {
            LatLng ll = new LatLng(bdlocation.getLatitude(), bdlocation.getLongitude());
            Log.i("启动start_marker", ll + "");
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.point_tart_change);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(points.get(1))//当前位置
                    .icon(bitmap);
            baiduMap.addOverlay(option);

    }
    //点击停止，绘制终点Marker的方法
    public void stop_run(BDLocation bdlocation){
        LatLng ll = new LatLng(bdlocation.getLatitude(), bdlocation.getLongitude());
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.point_stop_change);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(ll)//停止的位置位置
                .icon(bitmap);
        baiduMap.addOverlay(option);
        //mapView.onDestroy();
        //baiduMap.setMyLocationEnabled(false);

        //显示行走过的总距离
        for(int i = 0;i<list_distance.size();i++){
            Log.i("list_distance》》》》",list_distance.get(i)+"");
            distance  = (Double)list_distance.get(i)+distance;

        }
        showdistance.setText("总距离："+distance);
        locationClient.stop();
        Log.i("start_marker：","成功绘制最后一个点并停止locationClient");

    }


    //处理申请权限的方法，在申请时候如果不同意权限就finish()程序
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
        mapView.onDestroy();
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);//退出程序时候，将我当前位置“去掉”*/
    }
}