package com.changf.photo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.changf.photo.adapter.PhotoWallAdapter;
import com.changf.photo.data.Images;

import java.util.ArrayList;

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

        mPhotoWall.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(ThreeLevelBufferActivity.this,BigPhotoActivity.class);
                intent.putStringArrayListExtra("photos",convert(Images.imageUrls));
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }

    private ArrayList<String> convert(String[] imageUrls) {
        ArrayList<String> list = new ArrayList<>();
        for(String str:imageUrls){
            list.add(str);
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancelAllTasks();
    }

}
