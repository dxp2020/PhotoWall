package com.changf.photo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.changf.photo.view.FallsPhotoWallView;

import java.util.Random;

public class FallsPhotoWallActivity extends Activity {

    private FallsPhotoWallView sv_photo_wall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_falls_photo_wall);

        sv_photo_wall = findViewById(R.id.sv_photo_wall);
    }

    public void add(View view){
        int width = new Random().nextInt(200)+200;
        int height = new Random().nextInt(200)+200;
        sv_photo_wall.addView(width,height);
    }

}
