package com.yth520web.mapstep;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public LocationClient baidu_locationClient;//使用百度地图的locationClient
    private TextView position_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        //getApplicationContext为安卓自带API
        baidu_locationClient = new LocationClient(getApplicationContext());
        //MylocationListener为自定义类
        baidu_locationClient.registerLocationListener(new MylocationListener());
        position_text = (TextView)findViewById(R.id.position_text_view);

        //让用户同意权限
        List<String> permissionList = new ArrayList<>();
        /*PackageManager.PERMISSION_GRANTED的意思是：
         Permission check result: this is returned by checkPermission(String, String)
         if the permission has been granted to the given package.
         检测权限是否存在，不存在则申请，否则finish程序*/
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED);
        {   /*GPS高精度定位*/
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED);
        {  /* 动态申请权限*/
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED);
        {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String permission[] = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permission,1);
        }else{
            requestLocation();//自定义方法，启动LocationClient;
        }

    }
    private void requestLocation(){
        baidu_locationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result :grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            /*要同意全部权限，否则finish*/
                            Toast.makeText(this,"必须同意全部权限才可使用",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                            /*不理解*/
                        }
                    }
                    requestLocation();
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
    /*BDLocationListener是百度地图自带的接口，要重写onReceiveLocation方法*/
    public class MylocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度：").append(bdLocation.getLatitude())
                            .append("\n");
                    currentPosition.append("经度：").append(bdLocation.getLongitude())
                            .append("\n");
                    currentPosition.append("定位方式：");
                    if(bdLocation.getLocType()==bdLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络定位");
                    }
                    position_text.setText(currentPosition);
                }

            });

        }
        /*@Override
        public void onConnectHotStopMessage(String s,int i){
            
        }*/
    }
}
