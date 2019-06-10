package com.yth520web.mapstep;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RequestPermission {
    //申请权限的类
    List<String> permissionList;
    public void requestPermission(Context context){
        Log.i("申请权限：","True");
        //启动时候请求权限,同意所有权限后启动requestLocation()
        permissionList = new ArrayList<>();
        //申请权限，否则finish()活动
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ;
        {   //*GPS定位*//*
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) ;
        {  //* 动态申请权限*//*
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ;
        {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        /*if (!permissionList.isEmpty()) {
            String permission[] = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity, permission, 1);
        }*/
    }

}
