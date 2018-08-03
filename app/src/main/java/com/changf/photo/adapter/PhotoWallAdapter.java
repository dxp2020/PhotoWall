package com.changf.photo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.changf.photo.PhotoWallApplication;
import com.changf.photo.R;
import com.changf.photo.core.BitmapDecoder;
import com.changf.photo.util.ViewUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GridView的适配器，负责异步从网络上下载图片展示在照片墙上。
 */
public class PhotoWallAdapter extends BaseAdapter implements OnScrollListener {

    private Context context;
    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private Set<BitmapWorkerTask> taskCollection;

    /**
     * GridView的实例
     */
    private GridView mPhotoWall;
    /**
     * 第一张可见图片的下标
     */
    private int mFirstVisibleItem;
    /**
     * 一屏有多少张图片可见
     */
    private int mVisibleItemCount;
    /**
     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。
     */
    private boolean isFirstEnter = true;
    /**
     * 照片的宽度
     */
    private int photoWidth;
    /**
     * 照片的高度
     */
    private int photoHeight;

    /**
     * 图片的url
     */
    private List<String> dataSource = new ArrayList<>();


    private void init(Context context,GridView photoWall){
        this.context = context;
        mPhotoWall = photoWall;
        taskCollection = new HashSet<BitmapWorkerTask>();
        mPhotoWall.setOnScrollListener(this);
        int numColumns = 4;//photoWall.getNumColumns() = -1
        int verticalSpacing = photoWall.getVerticalSpacing();
        photoWidth = (ViewUtils.getScreenWidth(context)-verticalSpacing*numColumns)/numColumns;
        photoHeight = photoWidth;
    }

    private void convertData(String[] objects) {
        for(String data:objects){
            dataSource.add(data);
        }
    }

    public PhotoWallAdapter(Context context, List<String> data, GridView photoWall) {
        dataSource.addAll(data);
        init(context,photoWall);
    }

    public PhotoWallAdapter(Context context, String[] objects,GridView photoWall) {
        convertData(objects);
        init(context,photoWall);
    }

    @Override
    public int getCount() {
        return dataSource.size();
    }

    @Override
    public String getItem(int i) {
        return dataSource.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String url = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_photo, null);
        } else {
            view = convertView;
        }
        final ImageView photo = view.findViewById(R.id.photo);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) photo.getLayoutParams();
        params.width = photoWidth;
        params.height = photoHeight;
        // 给ImageView设置一个Tag，保证异步加载图片时不会乱序
        photo.setTag(url);
        setImageView(url, photo);
        return view;
    }

    /**
     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存，
     * 就给ImageView设置一张默认图片。
     *
     * @param imageUrl
     *            图片的URL地址，用于作为LruCache的键。
     * @param imageView
     *            用于显示图片的控件。
     */
    private void setImageView(String imageUrl, ImageView imageView) {
        Bitmap bitmap = decode(imageUrl);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.mipmap.empty_photo);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 仅当GridView静止时才去下载图片，GridView滑动时取消所有正在下载的任务
        if (scrollState == SCROLL_STATE_IDLE) {
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
        } else {
            cancelAllTasks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        // 下载的任务应该由onScrollStateChanged里调用，但首次进入程序时onScrollStateChanged并不会调用，
        // 因此在这里为首次进入程序开启下载任务。
        if (isFirstEnter && visibleItemCount > 0) {
            loadBitmaps(firstVisibleItem, visibleItemCount);
            isFirstEnter = false;
        }
    }

    /**
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象，
     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。
     *
     * @param firstVisibleItem
     *            第一个可见的ImageView的下标
     * @param visibleItemCount
     *            屏幕中总共可见的元素数
     */
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
                String imageUrl = getItem(i);
                BitmapDecoder bitmapDecoder = getBitmapDecoder(imageUrl);
                Bitmap bitmap = bitmapDecoder.decode();
                if (bitmap == null) {
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task);
                    task.execute(bitmapDecoder);
                } else {
                    ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BitmapDecoder getBitmapDecoder(String imageUrl){
        return new BitmapDecoder(context, PhotoWallApplication.getInstance().getBitmapMemory(), imageUrl, photoWidth, photoHeight);
    }

    private Bitmap decode(String imageUrl){
        return getBitmapDecoder(imageUrl).decode();
    }

    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            for (BitmapWorkerTask task : taskCollection) {
                task.cancel(false);
            }
            taskCollection.clear();
        }
    }

    /**
     * 异步下载图片的任务。
     */
    class BitmapWorkerTask extends AsyncTask<BitmapDecoder, Void, Bitmap> {
        /**
         * 图片的URL地址
         */
        private String imageUrl;

        @Override
        protected Bitmap doInBackground(BitmapDecoder... params) {
            BitmapDecoder mBitmapDecoder = params[0];
            imageUrl = mBitmapDecoder.getImageUrl();
            return mBitmapDecoder.decode();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。
            ImageView imageView = mPhotoWall.findViewWithTag(imageUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

    }

}
