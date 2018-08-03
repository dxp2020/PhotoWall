package com.changf.photo;

import android.app.Application;

import com.changf.photo.core.BitmapMemory;

public class PhotoWallApplication extends Application {

    private static PhotoWallApplication instance;

    /**
     * bitmap缓存操作类
     */
    private BitmapMemory mBitmapMemory;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mBitmapMemory = new BitmapMemory();
    }

    public BitmapMemory getBitmapMemory() {
        return mBitmapMemory;
    }

    public static PhotoWallApplication getInstance() {
        return instance;
    }
}
