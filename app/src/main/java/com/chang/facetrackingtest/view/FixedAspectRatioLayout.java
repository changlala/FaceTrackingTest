package com.chang.facetrackingtest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 固定分辨率为16:9
 *
 * 布局文件中height、width推荐设为match_parent
 */
public class FixedAspectRatioLayout extends FrameLayout {


    private int mWidth = 9;
    private int mHeight = 16;

    public FixedAspectRatioLayout(@NonNull Context context) {
        super(context);
    }

    public FixedAspectRatioLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedAspectRatioLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FixedAspectRatioLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int calWidth,calHeight;
        calWidth = width;
        calHeight = (int)(calWidth * ((float)mHeight/mWidth));

        if(calHeight >height){
            calHeight = height;
            calWidth = (int)(calHeight * ((float)mWidth/mHeight));
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(calWidth,MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(calHeight,MeasureSpec.EXACTLY));
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }
}
