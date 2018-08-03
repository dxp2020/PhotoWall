package com.changf.photo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.changf.photo.core.BitmapDecoder;
import com.changf.photo.util.ViewUtils;
import com.changf.photo.view.ZoomImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigPhotoActivity extends Activity {

    /**
     * 记录所有正在下载或等待下载的任务。
     */
    private Map<String,BitmapWorkerTask> taskCollection = new HashMap<>();
    private ViewPager viewPager;
    private List<String> mData;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mData = getIntent().getStringArrayListExtra("photos");
        currentPosition = getIntent().getIntExtra("position",0);
        //设置无标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.layout_big_photo);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new PagerAdapter() {
            private int photoWidth = ViewUtils.getScreenWidth(getApplication());
            private int photoHeight = ViewUtils.getScreenHeight(getApplication());

            private BitmapDecoder getBitmapDecoder(String imageUrl){
                return new BitmapDecoder(getApplication(), PhotoWallApplication.getInstance().getBitmapMemory(), imageUrl, photoWidth, photoHeight);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ZoomImageView zoomImageView = new ZoomImageView(getApplicationContext());
                zoomImageView.setTag(mData.get(position));
                BitmapDecoder bitmapDecoder = getBitmapDecoder(mData.get(position));
                Bitmap bitmap = bitmapDecoder.decode();
                if (bitmap != null) {
                    zoomImageView.setImageBitmap(bitmap);
                } else {
                    zoomImageView.setImageResource(R.mipmap.empty_photo);

                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.put(String.valueOf(position),task);
                    task.execute(bitmapDecoder);
                }
                container.addView(zoomImageView);
                return zoomImageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                BitmapWorkerTask task = taskCollection.get(String.valueOf(position));
                if(task!=null){
                    task.cancel(false);
                    taskCollection.remove(String.valueOf(position));
                }
                container.removeView((View) object);
            }

            @Override
            public int getCount() {
                return mData.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

        });

        viewPager.setCurrentItem(currentPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            ImageView imageView = viewPager.findViewWithTag(imageUrl);
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            taskCollection.remove(this);
        }

    }


}