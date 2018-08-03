package com.changf.photo.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class FileCacheUtils {

    public static File getImageDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache/image");
        return cacheDir;
    }

    public static File getNetworkDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache/network");
        return cacheDir;
    }

    public static File getPublishImageDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/publish/image");
        return cacheDir;
    }

    public static File getCacheDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache");
        return cacheDir;
    }

    public static File getPublishAudioDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/publish/audio");
        return cacheDir;
    }
    public static File getAudioDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/cache/audio");
        return cacheDir;
    }

    public static File getApkDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/apk");
        return cacheDir;
    }

    public static void clearCache(File dir) throws IOException {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if (file.isDirectory()) {
                clearCache(file);
            }
            if (!file.delete()) {
//                throw new IOException("failed to delete file: " + file);
            }
        }
    }
    public static void clearCache(File dir, List<String> noClearFiles) throws IOException {
        if (!dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
//            throw new IOException("not a readable directory: " + dir);
        }
        for (File file : files) {
            if(noClearFiles.contains(file.getAbsolutePath()))
            {
                continue;
            }
            if (file.isDirectory()) {
                clearCache(file,noClearFiles);
            }
            if (!file.delete()) {
//                throw new IOException("failed to delete file: " + file);
            }
        }
    }
    public static File getSaveImageDir(Context context) {
        File cacheDir = StorageUtils.getOwnCacheDirectory(context, context.getPackageName() + "/image");
        return cacheDir;
    }
}
