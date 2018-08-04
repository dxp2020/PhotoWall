package com.changf.photo.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.changf.photo.util.FileCacheUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BitmapDecoder {
    private Context context;
    private BitmapMemory bitmapMemory;
    private String remoteUrl;
    private String localUrl;
    private String key;
    private int reqWidth;
    private int reqHeight;
    private Stage stage;

    public BitmapDecoder(Context context, BitmapMemory bitmapMemory,String remoteUrl, int reqWidth, int reqHeight) {
        this.context = context;
        this.bitmapMemory = bitmapMemory;
        this.remoteUrl = remoteUrl;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        this.localUrl = getLocalUrl();
        this.key = getKey();
        this.stage = Stage.CACHE;
    }

    public  Bitmap decode(){
        if (isDecodingFromCache()) {
            return decodeFromCache();
        } else {
            return decodeFromSource();
        }
    }

    /**
     * 本地存在就从本地加载，不存在则从网络加载
     * @return
     */
    private Bitmap decodeFromSource() {
        File  file = new File(localUrl);
        if(file.exists()){
            Bitmap bitmap = decodeFromSource(localUrl,reqWidth,reqHeight);
            bitmapMemory.addBitmapToMemoryCache(key,bitmap);
            return bitmap;
        }else{
            boolean isCached = cacheDataToDisk(load());
            if (isCached) {
                return decode();
            }else{
                return null;
            }
        }
    }

    /**
     * 如果是网络图片，则从网络加载
     * 如果是本地图片，则从本地加载
     * @return
     */
    private Bitmap load(){
        if(remoteUrl.startsWith("http")||remoteUrl.startsWith("HTTP")){
            return loadDataFromNetwork();
        }else{
            return loadDataFromDisk();
        }
    }

    /**
     * 从磁盘加载
     * @return
     */
    private Bitmap loadDataFromDisk() {
        return null;
    }

    /**
     * 从网络加载图片
     * @return
     */
    private Bitmap loadDataFromNetwork() {
        Bitmap bitmap = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(remoteUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(10 * 1000);
            con.setDoInput(true);
            con.setDoOutput(true);
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bitmap;
    }

    private  Bitmap decodeFromCache() {
        Bitmap  bitmap =  bitmapMemory.getBitmapFromMemoryCache(key);
        if(bitmap!=null){
            return bitmap;
        }else{
            if(Looper.myLooper()==Looper.getMainLooper()){
                return null;
            }else{
                stage = Stage.SOURCE;
                return decode();
            }
        }
    }

    private  int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 磁盘缓存图片
     * @param bitmap
     * @return void
     */
    private  boolean cacheDataToDisk(Bitmap bitmap){
        try {
            if(bitmap==null){
                return false;
            }else{
                FileOutputStream fileOutputStream=new FileOutputStream(localUrl);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fileOutputStream);
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 从磁盘加载图片
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private  Bitmap decodeFromSource(String path, int reqWidth, int reqHeight){
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 获取本地图片缓存路径
     * @return
     */
    private  String getLocalUrl() {
        if(remoteUrl.startsWith("http")||remoteUrl.startsWith("HTTP")){
            return FileCacheUtils.getCacheDir(context).getAbsolutePath()+"/"+ new String(Base64.encode(remoteUrl.getBytes(),Base64.DEFAULT))+".png";
        }else{
            return remoteUrl;
        }
    }

    /**
     * 生成内存缓存的KEY
     * @return
     */
    private String getKey() {
        return new String(Base64.encode((remoteUrl).getBytes(),Base64.DEFAULT))+reqWidth+"-"+reqHeight;
    }

    private boolean isDecodingFromCache() {
        return stage == Stage.CACHE;
    }

    public String getImageUrl() {
        return remoteUrl;
    }

    private enum Stage {
        CACHE,
        SOURCE
    }

}
