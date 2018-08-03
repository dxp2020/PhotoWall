package com.changf.photo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.changf.glide.R;
import com.changf.photo.adapter.PhotoWallAdapter;
import com.changf.photo.data.Images;

public class ThreeLevelBufferActivity extends Activity {

    private GridView mPhotoWall;
    private PhotoWallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_photo_wall);
        mPhotoWall = findViewById(R.id.photo_wall);

        adapter = new PhotoWallAdapter(this, Images.imageUrls, mPhotoWall);
        mPhotoWall.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancelAllTasks();
    }

}
