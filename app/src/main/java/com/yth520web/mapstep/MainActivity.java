package com.yth520web.mapstep;

import android.Manifest;
import android.app.AlertDialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    List<LatLng> points = new ArrayList<LatLng>();//用于存放坐标点，然后绘制折线图
    Button button_start,button_stop;//两个按钮，点击时开始和暂停绘图，stop长按时候停止绘图
    Boolean run_isStart =false,run_isStop=false;//判断是否开始和停止,检测用户是否暂停
    Boolean showMyLocation=true;//用于用一个小光标是否展示我的位置，跑步后不再实时显示我当前位置
    TextView myLocation;//在文本框中显示我目前的位置,如“西南财经大学晨曦体育馆”
    Boolean showDia=true;//用于提示用户，如果运动时候未开启GPS则显示一个提示框
    TextView showMrter;//米和公里之间的切换
    TextView avg_v;//显示平均速度
    TextView text_avg_v;//显示平均速度的文本框
    TextView my_data_year;//年月日，用于记录
    TextView my_data_time;//时分，用于记录
    float userHeight=0;//用户身高体重，用于计算消耗的卡路里
    float userWeight=0;
    TextView avg_k;//显示运动的卡路里
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //显示地图，需要放在setContentView(R.layout.main_layout);前，否则会报错
        SDKInitializer.initialize(getApplicationContext());
        //转换坐标，不然会发生位置的偏移
        Log.i("原本坐标类型：",SDKInitializer.getCoordType()+"");
        SDKInitializer.setCoordType(CoordType.GCJ02);//默认为BD09LL坐标
        setContentView(R.layout.main_layout);
        //开启百度地域
        mapView = (MapView)findViewById(R.id.bmapView);
        //开启LitePal数据库,存放用户身高体重
        LitePal.getDatabase();
        //查询数据库中是否有用户身高体重，如果没有，跳转到添加页面，如果有则取出
        List<Db> dbs = DataSupport.findAll(Db.class);
        for (Db b:dbs){
            try{
                userHeight = b.getUserHeight();
                userWeight=b.getUserWeight();

            }catch (Exception e){
                //如果没有数据，跳转到添加身高体重的页面
                e.printStackTrace();
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(MainActivity.this);
                normalDialog.setIcon(R.drawable.dragon);
                normalDialog.setMessage("添加基本信息后可以提供更好的服务"+"\n"+"是否现在就添加信息？");
                normalDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this,AddUserInfor.class);
                                startActivity(intent);
                            }
                        });
                normalDialog.setNegativeButton("取消",null);
                // 显示
                normalDialog.show();
            }
        }
        //mTxtView = (TextView)findViewById(R.id.mTextView);
        //开始和暂停按钮
        button_start = (Button)findViewById(R.id.map_start);
        button_stop = (Button)findViewById(R.id.map_stop);
        timer = (Chronometer) findViewById(R.id.timer);//计时器
        showdistance = (TextView) findViewById(R.id.distance);//API自带的计算距离方法DistanceUtil
        myLocation = (TextView)findViewById(R.id.myLocation);
        //展示平均速度，运动距离，运动速度
        showMrter=(TextView)findViewById(R.id.meter);
        avg_v=(TextView)findViewById(R.id.avg_v);
        text_avg_v=(TextView)findViewById(R.id.text_avg_v);
        //设置年月日时分
        my_data_year=(TextView)findViewById(R.id.my_data_year);
        my_data_time=(TextView)findViewById(R.id.my_data_time);
        initLocationOption();//启动地图，获取位置信息
        avg_k=(TextView)findViewById(R.id.avg_k);//消耗的卡路里
        //添加用户信息
        Button user =(Button)findViewById(R.id.user);
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent add_user = new Intent(MainActivity.this,AddUserInfor.class);
                startActivity(add_user);
            }
        });
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

                    //获取时分秒，年月日
                    Calendar c = Calendar.getInstance();//
                    int mYear = c.get(Calendar.YEAR); // 获取当前年份
                    int mMonth = c.get(Calendar.MONTH) + 1;// 获取当前月份
                    int mDay = c.get(Calendar.DAY_OF_MONTH);// 获取当日期
                    int mWay = c.get(Calendar.DAY_OF_WEEK);// 获取当前日期的星期
                    int mHour = c.get(Calendar.HOUR_OF_DAY);//时
                    int mMinute = c.get(Calendar.MINUTE);//分
                    my_data_year.setText(mYear+"/"+mMonth+"/"+mDay);
                    my_data_time.setText(mHour+":"+mMinute);
                    try{//开始后再次尝试获取用户身高体重信息
                        List<Db> db = DataSupport.findAll(Db.class);
                        for (Db b:db){
                            userHeight = b.getUserHeight();
                            userWeight=b.getUserWeight();
                            Log.i("用户基本信息：",userHeight+"》》"+userWeight);
                        }
                    }catch (Exception e){ }
                    avg_k.setText("计算中");
                }
            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**点击停止按钮时，需要实现：
                 * （1）在终点打上一个Marker
                 * （2）停止获得位置信息和轨迹的绘制
                 * （3）交给MyLocationListener实现，向用户展示自己的运动距离，运动轨迹，最高运动速度
                 * （4）停止计时器
                 * （5）计算卡路里
                 */
                //（1）在终点打上一个Marker
                LatLng ll =new LatLng(latitude,longitude);
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.point_stop_change);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions()
                        .position(ll)//停止的位置位置
                        .icon(bitmap);
                baiduMap.addOverlay(option);
               //（2）停止获得位置信息和轨迹的绘制
                run_isStop=true;
                //mapView.onPause();
                locationClient.stop();
                baiduMap.setMyLocationEnabled(false);
                //(3)停止计时器
                timer.stop();
                //显示平均速度
                Double avg = distance/((int) (SystemClock.elapsedRealtime() - timer.getBase())/1000);
                Log.i("timer的base",(int) (SystemClock.elapsedRealtime() - timer.getBase())/1000+"");
                NumberFormat nf = NumberFormat.getNumberInstance();
                // 保留两位小数
                nf.setMaximumFractionDigits(2);
                avg_v.setText(nf.format(avg));
                text_avg_v.setText("平均速度");
                //计算卡路里，K=体重（kg）*运动时间(小时)*指数K
                //指数K=30/速度（分钟/400米）
                double x=avg;
                float f=(float)x;
                //直接转化会报错
                float user_k = (userWeight * ((int) (SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60) * 30)/f;
                avg_k.setText(user_k+"");
                Toast.makeText(MainActivity.this,"运动完成 消耗"+user_k+"千卡路里",Toast.LENGTH_LONG).show();
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
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(3000);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
       //locationOption.setLocationNotify(false);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
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
        public void onReceiveLocation(BDLocation location) {
            //当定位方式为网络定位或gps定位，则启动navigateTo将位置移动到我当前位置
            if(run_isStop == false){
                if (location.getLocType() == BDLocation.TypeGpsLocation
                        || location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    navigateTo(location);
                    Log.i("启动navigateTo：", true + "");
                    if (showDia==true&&location.getLocType() == BDLocation.TypeNetWorkLocation){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("在室内或未开启GPS会造成定位误差，该软件更适合户外记录"+"\n"+"请开启GPS");
                        builder.setPositiveButton("确定",null);
                        builder.show();
                        showDia=false;//仅提醒一次
                    }
                }
                //开始运动，启动相关功能
                if (run_isStart == true) {
                    start_run(location);//绘制轨迹
                    start_marker(location);//绘制一个起点
                }
                //停止运动，启动相关功能
                //获取纬度信息
                latitude = location.getLatitude();
                //获取经度信息
                longitude = location.getLongitude();

                //显示我的位置，如“西南财经大学晨曦体育馆”

                 final String strLocation=location.getAddrStr();
                 final float mySpeed = location.getSpeed();
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         for (int i=0;i<list_distance.size();i++){
                             distance =(Double)list_distance.get(i)+distance;
                         }
                     }
                   }).start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("我当前位置：",strLocation+"速度："+mySpeed);
                        myLocation.setText(strLocation);
                        //设置速度.mySpeed格式为公里/小时，转化为m/s
                        avg_v.setText(mySpeed*0.36+"");
                        //判断行走的距离，如果<1000米，那么显示格式为“632米”，如果>=1000米，显示格式为"1.32公里"
                        if(distance<1000) {
                            //进行四舍五入，将double转为int
                            //进行四舍五入操作：
                            //Integer.parseInt(new java.text.DecimalFormat("0").format(x))
                            int trans_distance = Integer.parseInt(new java.text.DecimalFormat("0").format(distance));
                            showdistance.setText(trans_distance + "");
                            showMrter.setText("米");
                        }else {
                            //显示格式为“1.32公里”,需要保留一位小数
                            double d = distance/1000;
                            NumberFormat nf = NumberFormat.getNumberInstance();
                            // 保留两位小数
                            nf.setMaximumFractionDigits(2);
                            // 如果不需要四舍五入，可以使用RoundingMode.DOWN
                            //nf.setRoundingMode(RoundingMode.UP);
                            showdistance.setText(nf.format(d) + "");
                            showMrter.setText("公里");
                        }
                    }
                });

            }
        }
    }
//将地图移动到我当前位置
private void navigateTo (BDLocation bdlocation){
        //获取目前所在的位置，由于SpanScan方法，该方法2每秒获取一次位置
        LatLng ll =new LatLng(bdlocation.getLatitude(),bdlocation.getLongitude());
        //MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
        //尝试将我的位置显示移动到屏幕偏上方而非中间
        LatLng ll_text =new LatLng(bdlocation.getLatitude()-0.0013,bdlocation.getLongitude());
        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll_text);

        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(19f);
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
        //showdistance.setText(distance+"");
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
        //mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //mapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);//退出程序时候，将我当前位置“去掉”*/
    }
}