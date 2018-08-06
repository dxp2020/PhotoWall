package com.changf.photo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.changf.photo.data.Images;
import com.changf.photo.view.FallsPhotoWallView;

import java.util.ArrayList;

public class FallsPhotoWallActivity2 extends Activity {

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
                Intent intent = new Intent(FallsPhotoWallActivity2.this,BigPhotoActivity.class);
                intent.putStringArrayListExtra("photos",fileNames);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        loadPhoneAllPhoto();
    }


    private void loadPhoneAllPhoto() {
       //本地图片
       Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                while (cursor.moveToNext()) {
                    byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    fileNames.add(new String(data, 0, data.length - 1));
                }
                cursor.close();

                handler.sendEmptyMessage(0);
            }
        });
        thread.start();
    }

}
