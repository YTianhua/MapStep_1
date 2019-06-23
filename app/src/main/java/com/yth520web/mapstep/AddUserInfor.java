package com.yth520web.mapstep;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

public class AddUserInfor extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_infor);
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(AddUserInfor.this);
        normalDialog.setIcon(R.drawable.dragon);
        normalDialog.setTitle("提示:");
        normalDialog.setMessage("添加信息后能更好的安排计划..");
        normalDialog.setPositiveButton("我知道了",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        normalDialog.show();
        Button btn_userInfor = (Button)findViewById(R.id.btn_userInfor);
        btn_userInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获得身高体重，不为空时添加到数据库
                EditText userHeight =(EditText)findViewById(R.id.userHeight);
                EditText userWeight =(EditText)findViewById(R.id.userWeight);
                if (userHeight.getText().toString()!=null&&userWeight.getText().toString()!=null){
                    //尝试转化为ufloat，转化失败则说明输入无效字符
                    try{
                        float h=Float.parseFloat(userHeight.getText().toString());
                        float w=Float.parseFloat(userWeight.getText().toString());
                        //先清空数据库
                        DataSupport.deleteAll(Db.class);
                        //添加数据到数据库
                        Db db = new Db();
                        db.setUserHeight(h);
                        db.setUserWeight(w);
                        db.save();//保存到数据库
                        Intent intent = new Intent(AddUserInfor.this,MainActivity.class);
                        startActivity(intent);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(AddUserInfor.this,"输入无效字符",Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(AddUserInfor.this,"不能输入空数值",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
