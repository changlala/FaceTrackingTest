package com.chang.facetrackingtest;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraHelper {
    private Camera mCamera;
    private Camera.CameraInfo mCameraInfo;
    private SurfaceTexture mSurfaceTexture;
    //height>width
    public volatile int PREVIEW_WIDTH;
    public volatile int PREVIEW_HEIGHT;
    private Context mContext;
    private Camera.PreviewCallback mPreviewCallBack;
    //控件尺寸 height>width
    private int mScreenWidth;
    private int mScreenHeight;

    public CameraHelper(Context context ) {
        this.mContext = context;
    }
    public void setSurfaceTexture(SurfaceTexture mSurfaceTexture) {
        this.mSurfaceTexture = mSurfaceTexture;
        if(mSurfaceTexture != null){
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void openCamera(){
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
        Camera.CameraInfo cameraInfo =new Camera.CameraInfo();

        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i,cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                mCameraInfo = cameraInfo;
                mCamera = Camera.open(i);
                break;
            }
        }


    }
    public void setupCamera(int screenWidth, int screenHeight){
        if(mCamera == null) {
            return;
        }
        this.mScreenWidth = screenWidth;
        this.mScreenHeight = screenHeight;
        try {

            Camera.Parameters parameters = mCamera.getParameters();
            //关闭闪光灯
            List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            for (String flashMode : supportedFlashModes) {
                if(flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)){
                    parameters.setFlashMode(flashMode);
                }
            }
            //设置预览尺寸 需要获得supportSize
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size :
                    supportedPreviewSizes) {
                Log.d("chang", "支持的相机尺寸"+size.height+" "+size.width);
            }
            Camera.Size size = findProperSize(supportedPreviewSizes);
            //size里的宽大于高
            parameters.setPreviewSize(size.width,size.height);
            PREVIEW_HEIGHT = size.width;
            PREVIEW_WIDTH = size.height;
            Log.d("chang", "预览尺寸 宽高："+size.height+" "+size.width);

            //设置预览方向
            int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay()
                    .getRotation();
            Log.d("chang", "setupCamera: rotaion "+rotation);
            int degrees = 0;
            switch (rotation) {
                //锁定竖屏，所以degrees = 0;
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (mCameraInfo.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (mCameraInfo.orientation - degrees + 360) % 360;
            }
            mCamera.setDisplayOrientation(result);


            //设置对焦模式
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            for (String focus :
                    supportedFocusModes) {
                Log.d("chang", "支持的聚焦"+focus);
            }
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }else{
                parameters.setFocusMode(parameters.FOCUS_MODE_FIXED);
            }

            mCamera.setParameters(parameters);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Camera.PreviewCallback getPreviewCallBack() {
        return mPreviewCallBack;
    }

    public void setPreviewCallBack(Camera.PreviewCallback mPreviewCallBack) {
        this.mPreviewCallBack = mPreviewCallBack;
    }

    public void startPreview(){
        if(mCamera != null && mPreviewCallBack != null){
            mCamera.setPreviewCallback(mPreviewCallBack);
            mCamera.startPreview();
        }
    }
    public void release(){
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.Size findProperSize( List<Camera.Size> sizeList) {

        List<List<Camera.Size>> ratioListList = new ArrayList<>();
        for (Camera.Size size : sizeList) {
            addRatioList(ratioListList, size);
        }

        //这里把宽高交换一下，width>height,和sizeList里的尺寸格式一样
        int temp = mScreenWidth;
        mScreenWidth = mScreenHeight;
        mScreenHeight = temp;

        final float surfaceRatio = (float) mScreenWidth / mScreenHeight;
        //bestRatioList保存所有符合宽高比的分辨率
        List<Camera.Size> bestRatioList = null;
        float ratioDiff = Float.MAX_VALUE;
        for (List<Camera.Size> ratioList : ratioListList) {
            float ratio = (float) ratioList.get(0).width / ratioList.get(0).height;
            float newRatioDiff = Math.abs(ratio - surfaceRatio);
            if (newRatioDiff < ratioDiff) {
                bestRatioList = ratioList;
                ratioDiff = newRatioDiff;
            }
        }

        //返回符合宽高比的分辨率最低的那个（数组最后一个）
        if(bestRatioList != null){
            return bestRatioList.get(bestRatioList.size()-1);
        }else{
            return null;
        }
//        Camera.Size bestSize = null;
//        int diff = Integer.MAX_VALUE;
//        assert bestRatioList != null;
//        for (Camera.Size size : bestRatioList) {
//            int newDiff = Math.abs(size.width - mScreenWidth) + Math.abs(size.height - mScreenHeight);
//            if (size.height >= mScreenHeight && newDiff < diff) {
//                bestSize = size;
//                diff = newDiff;
//            }
//        }
//
//        if (bestSize != null) {
//            return bestSize;
//        }
//
//        diff = Integer.MAX_VALUE;
//        for (Camera.Size size : bestRatioList) {
//            int newDiff = Math.abs(size.width - mScreenWidth) + Math.abs(size.height - mScreenHeight);
//            if (newDiff < diff) {
//                bestSize = size;
//                diff = newDiff;
//            }
//        }
//
//        return bestSize;
    }

    private void addRatioList(List<List<Camera.Size>> ratioListList, Camera.Size size) {
        float ratio = (float) size.width / size.height;
        for (List<Camera.Size> ratioList : ratioListList) {
            float mine = (float) ratioList.get(0).width / ratioList.get(0).height;
            if (ratio == mine) {
                ratioList.add(size);
                return;
            }
        }

        List<Camera.Size> ratioList = new ArrayList<>();
        ratioList.add(size);
        ratioListList.add(ratioList);
    }
}
