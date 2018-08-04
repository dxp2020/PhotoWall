package com.changf.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.changf.photo.adapter.PhotoWallAdapter;
import com.changf.photo.core.BitmapDecoder;
import com.changf.photo.core.BitmapMemory;
import com.changf.photo.data.Images;

public class MainActivity extends Activity {

    private static final int PHOTOPERMISSION = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void fallsPhotoWall(View view){
        startActivity(new Intent(this,FallsPhotoWallActivity.class));
    }
    public void threeLevelBuffer(View view){
        startActivity(new Intent(this,ThreeLevelBufferActivity.class));
    }

    public void twoLevelBuffer(View view){
        checkReadWritePermission();
    }

    /**
     * 检查读写权限权限
     */
    private void checkReadWritePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this,TwoLevelBufferActivity.class));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PHOTOPERMISSION); // 无权限，去申请权限
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PHOTOPERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(this,TwoLevelBufferActivity.class));
                } else {
                    Toast.makeText(this,"读写权限获取失败!",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


}
