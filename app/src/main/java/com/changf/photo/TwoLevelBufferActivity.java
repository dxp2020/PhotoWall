package com.changf.photo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.changf.photo.adapter.PhotoWallAdapter;

import java.util.ArrayList;

public class TwoLevelBufferActivity extends Activity {
    private GridView mPhotoWall;
    private PhotoWallAdapter adapter;
    private ArrayList<String> fileNames = new ArrayList<>();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    String[] objects = new String[fileNames.size()];
                    for(int i=0;i<fileNames.size();i++){
                        objects[i] = fileNames.get(i);
                    }
                    refreshPhotoWall();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_photo_wall);
        mPhotoWall = findViewById(R.id.photo_wall);

        mPhotoWall.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(TwoLevelBufferActivity.this,BigPhotoActivity.class);
                intent.putStringArrayListExtra("photos",fileNames);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        adapter = new PhotoWallAdapter(this, fileNames, mPhotoWall);

        loadPhoneAllPhoto();
    }

    private void loadPhoneAllPhoto() {
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

    private void refreshPhotoWall() {
        adapter = new PhotoWallAdapter(this, fileNames, mPhotoWall);
        mPhotoWall.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        Thread.interrupted();
        handler.removeMessages(0);
        super.onDestroy();
    }
}
