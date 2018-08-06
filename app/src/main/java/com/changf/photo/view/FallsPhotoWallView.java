package com.changf.photo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.changf.photo.PhotoWallApplication;
import com.changf.photo.R;
import com.changf.photo.core.BitmapDecoder;
import com.changf.photo.util.ViewUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FallsPhotoWallView extends ScrollView implements View.OnTouchListener{

    private static final String TAG = FallsPhotoWallView.class.getSimpleName();

    private Map<String,BitmapWorkerTask> taskCollection = new HashMap<>();
    private ArrayList<String> fileNames = new ArrayList<>();
    private LinearLayout firstContainer;
    private LinearLayout secondContainer;
    private LinearLayout thirdContainer;
    private int photoIndex;
    private int itemWidth;
    private int lastY;
    private int dp5 = dip2px(5);
    private OnItemClickListener mOnItemClickListener;

    /**
     * 三个容器中依次添加的imageView
     */
    private List<ImageView> imageViews = new ArrayList<ImageView>();

    public FallsPhotoWallView(Context context) {
        this(context,null);
    }

    public FallsPhotoWallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        itemWidth = (ViewUtils.getScreenWidth(getContext())-dp5*2)/3;

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout);

        firstContainer = new LinearLayout(getContext());
        firstContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(firstContainer,getColumnLayoutParam(0));

        secondContainer = new LinearLayout(getContext());
        secondContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(secondContainer,getColumnLayoutParam(1));

        thirdContainer = new LinearLayout(getContext());
        thirdContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thirdContainer,getColumnLayoutParam(2));

        setOnTouchListener(this);
    }

    private LinearLayout.LayoutParams getColumnLayoutParam(int index){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1);
        if(index == 0 || index == 1 ){
            params.rightMargin = dp5;
        }
        return params;
    }

    private LinearLayout getMinHeightColumn(){
        return (LinearLayout) getMinHeightView(getMinHeightView(firstContainer,secondContainer),thirdContainer);
    }

    private View getMinHeightView(View view1,View view2){
        if(view1.getMeasuredHeight()<=view2.getMeasuredHeight()){
            return view1;
        }else{
            return view2;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public  int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private BitmapDecoder getBitmapDecoder(String imageUrl,int photoWidth){
        return new BitmapDecoder(getContext(), PhotoWallApplication.getInstance().getBitmapMemory(), imageUrl, photoWidth);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if(lastY==getScrollY()){
                        checkVisibility();
                        if(isCanLoadMore()){
                            loadMorePhotos();
                        }
                    }else{
                        //滚动状态停止一切加载行为
                        cancelAllTasks();

                        lastY = getScrollY();
                        //delayMillis值越大越精确，值越小getScrollY可能不准
                        handler.sendEmptyMessageDelayed(0,20);
                    }
                    break;
            }
        }
    };

    private boolean checkIsVisibility(View view){
        int top = view.getTop();
        int bottom = view.getBottom();
        int visibilityTop = getScrollY();
        int visibilityBottom = getHeight()+getScrollY();
        if(bottom<visibilityTop||top>visibilityBottom){
            return false;
        }
        return true;
    }

    private void checkVisibility() {
        for(int i=0;i<imageViews.size();i++){
            ImageView view = imageViews.get(i);
            if(checkIsVisibility(view)){
                String imageUrl = view.getTag().toString();
                BitmapDecoder mBitmapDecoder = getBitmapDecoder(imageUrl,itemWidth);
                Bitmap bitmap = mBitmapDecoder.decode();
                if(bitmap!=null){
                    view.setImageBitmap(bitmap);
                }else{
                    BitmapWorkerTask task = new BitmapWorkerTask(view);
                    task.execute(mBitmapDecoder);
                    taskCollection.put(imageUrl,task);
                }
            }else{
                view.setImageResource(R.mipmap.empty_photo);
            }
        }
    }

    private boolean isCanLoadMore() {
        if(isScrolledBottom()&&fileNames.size()>0&&taskCollection.isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(0,5);
                break;
        }
        return false;
    }

    public boolean isScrolledBottom() {
        View childView = getChildAt(0);
        if(childView.getMeasuredHeight() <= getScrollY() + getHeight()){
            return true;
        }
        return false;
    }

    public void setData(ArrayList<String> fileNames) {
        this.fileNames.addAll(fileNames);
    }

    public void loadMorePhotos() {
        int maxIndex = photoIndex+15;
        if(maxIndex>fileNames.size()){
            maxIndex = fileNames.size();
        }
        for(;photoIndex<maxIndex;photoIndex++){
            String imageUrl = fileNames.get(photoIndex);

            BitmapWorkerTask task = new BitmapWorkerTask();
            task.execute(getBitmapDecoder(imageUrl,itemWidth));

            taskCollection.put(imageUrl,task);
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
        private BitmapDecoder bitmapDecoder;
        private ImageView imageView;

        public BitmapWorkerTask() {
        }

        public BitmapWorkerTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(BitmapDecoder... params) {
            bitmapDecoder = params[0];
            imageUrl = bitmapDecoder.getImageUrl();
            return bitmapDecoder.decode();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(imageView==null){
                imageView = new ImageView(getContext());
                imageView.setImageResource(R.mipmap.empty_photo);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setTag(imageUrl);

                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                addPhotoView(imageView,itemWidth,bitmapDecoder.getOutHeight());
            }else{
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
            taskCollection.remove(imageUrl);
        }
    }

    public void addPhotoView(ImageView view,int outWidth,int outHeight) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(outWidth,outHeight);
        params.bottomMargin = dp5;

        LinearLayout container = getMinHeightColumn();
        container.addView(view,params);
        //添加之后主动测量，避免添加过快，获取的高度不对
        container.measure(0,0);

        imageViews.add(view);

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener!=null){
                    String tag = v.getTag().toString();
                    int position = getPosition(tag);
                    mOnItemClickListener.onItemClick((ImageView) v,position);
                }
            }
        });
    }

    private int getPosition(String tag) {
        for(int i=0;i<fileNames.size();i++){
            if(fileNames.get(i).equals(tag)){
                return i;
            }
        }
        return 0;
    }

    /**
     * 取消所有正在下载或等待下载的任务。
     */
    public void cancelAllTasks() {
        if (taskCollection != null) {
            Iterator iterator = taskCollection.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, BitmapWorkerTask> toEvict = (Map.Entry<String, BitmapWorkerTask>) iterator.next();
                BitmapWorkerTask task = toEvict.getValue();
                task.cancel(false);
            }
            taskCollection.clear();
        }
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(ImageView view,int position);
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAllTasks();
        super.onDetachedFromWindow();
    }

}
