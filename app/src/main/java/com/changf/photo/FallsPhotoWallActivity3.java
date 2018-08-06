package com.changf.photo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.changf.photo.data.Images;
import com.changf.photo.view.FallsPhotoWallView;

import java.util.ArrayList;

public class FallsPhotoWallActivity3 extends Activity {

    private FallsPhotoWallView sv_photo_wall;
    private ArrayList<String> fileNames = new ArrayList<>();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    sv_photo_wall.setData(fileNames);
                    sv_photo_wall.loadMorePhotos();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_falls_photo_wall);

        sv_photo_wall = findViewById(R.id.sv_photo_wall);
        sv_photo_wall.setOnItemClickListener(new FallsPhotoWallView.OnItemClickListener() {
            @Override
            public void onItemClick(ImageView view, int position) {
                Intent intent = new Intent(FallsPhotoWallActivity3.this,BigPhotoActivity.class);
                intent.putStringArrayListExtra("photos",fileNames);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        loadPhoneAllPhoto();
    }


    private void loadPhoneAllPhoto() {
        //网络图片
        for(String url:Images.imageUrls){
            fileNames.add(url);
        }
        handler.sendEmptyMessage(0);
    }

}
