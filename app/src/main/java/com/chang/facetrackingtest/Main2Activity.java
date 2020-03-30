package com.chang.facetrackingtest;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;


public class Main2Activity extends AppCompatActivity {

    private CameraHelper mCameraHelper;
    private SurfaceView mSurfaceView;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private DrawingHelper mDrawingHelper;
    //贴图开关
    private TextView bitmapSwitch;
//
//    private int mPreviewWidth;
//    private int mPreviewHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if(getSupportActionBar()!=null)
            getSupportActionBar().hide();

        mSurfaceView = findViewById(R.id.surfaceview);
        mHandlerThread = new HandlerThread("drawingThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mDrawingHelper = new DrawingHelper(this);
        mCameraHelper = new CameraHelper(this);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

            }

            @Override
            public void surfaceChanged(final SurfaceHolder surfaceHolder, int i, final int i1,final int i2) {
                Log.d("chang", "surfaceview 宽高："+i1+" "+i2);
//                startCamera(i1,i2);
//                mDrawingHelper.init(surfaceHolder.getSurface(),i1,i2);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //以下顺序不可随意更改
                        mCameraHelper.openCamera();
                        //得到预览尺寸
                        mCameraHelper.setupCamera(i1,i2);
                        mCameraHelper.setSurfaceTexture(null);
                        mCameraHelper.setPreviewCallBack(new Camera.PreviewCallback() {
                            @Override
                            public void onPreviewFrame(byte[] bytes, Camera camera) {
                                Log.d("chang", "draw一帧");
                                mDrawingHelper.draw(bytes);

                            }
                        });
                        //需等待设置surfacetexture才会正式启动
                        mCameraHelper.startPreview();

                        //mCameraHelper.setupCamera(i1,i2)后才可得到mCameraHelper.PREVIEW_HEIGHT
                        mDrawingHelper.setPreviewHeight(mCameraHelper.PREVIEW_HEIGHT);
                        mDrawingHelper.setPreviewWidth(mCameraHelper.PREVIEW_WIDTH);
                        //有了预览尺寸后才可调用init方法
                        mDrawingHelper.init(surfaceHolder.getSurface(),i1,i2);
                        //mDrawingHelper.init后才可调mDrawingHelper.getSurfaceTexture() 正式启动预览
                        mCameraHelper.setSurfaceTexture(mDrawingHelper.getSurfaceTexture());
                    }
                });

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                release();
            }
        });


        bitmapSwitch = findViewById(R.id.bitmapSwitch);
        bitmapSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawingHelper.invertShowBitmap();
            }
        });
    }

    private void startCamera(final int viewWidth,final int viewHeight){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraHelper.openCamera();
                mCameraHelper.setSurfaceTexture(mDrawingHelper.getSurfaceTexture());
                mCameraHelper.setupCamera(viewWidth,viewHeight);
                mDrawingHelper.setPreviewHeight(mCameraHelper.PREVIEW_HEIGHT);
                mDrawingHelper.setPreviewWidth(mCameraHelper.PREVIEW_WIDTH);
                Log.d("chang", " startCamera 宽高："+mCameraHelper.PREVIEW_WIDTH+" "+mCameraHelper.PREVIEW_HEIGHT);
                mCameraHelper.setPreviewCallBack(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Log.d("chang", "draw一帧");
                        mDrawingHelper.draw(bytes);

                    }
                });
                mCameraHelper.startPreview();
            }

        });
    }
    private void release(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCameraHelper.release();
                mDrawingHelper.release();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
