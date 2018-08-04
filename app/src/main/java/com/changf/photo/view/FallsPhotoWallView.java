package com.changf.photo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.changf.photo.R;
import com.changf.photo.util.ViewUtils;

public class FallsPhotoWallView extends ScrollView {

    private static final String TAG = FallsPhotoWallView.class.getSimpleName();

    private LinearLayout firstContainer;
    private LinearLayout secondContainer;
    private LinearLayout thirdContainer;

    public FallsPhotoWallView(Context context) {
        this(context,null);
    }

    public FallsPhotoWallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout);

        firstContainer = new LinearLayout(getContext());
        firstContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(firstContainer,new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1));

        secondContainer = new LinearLayout(getContext());
        secondContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(secondContainer,new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1));

        thirdContainer = new LinearLayout(getContext());
        thirdContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(thirdContainer,new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1));
    }

    public void addView(int width, int height) {
        width = ViewUtils.getScreenWidth(getContext())/3;
        View view = new View(getContext());
        view.setBackgroundResource(android.R.color.black);

        getMinHeightColumn().addView(view,new LinearLayout.LayoutParams(width, 200));
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

}
