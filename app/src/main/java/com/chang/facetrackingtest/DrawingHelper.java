package com.chang.facetrackingtest;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import com.chang.facetrackingtest.opengl.EGLUtils;
import com.chang.facetrackingtest.opengl.GLBitmap;
import com.chang.facetrackingtest.opengl.GLCamera;
import com.chang.facetrackingtest.opengl.GLFrame;
import com.chang.facetrackingtest.opengl.GLPoints;
import zeusees.tracking.Face;
import zeusees.tracking.FaceTracking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class DrawingHelper {

    private EGLUtils mEGLUtils;
    private GLCamera mGLCamera;
    private GLBitmap mGLBitmap;
    private GLFrame mGLFrame;
    private GLPoints mGLPoints;

    private byte[] mNv21Data;
    private FaceTracking mMultiTrack106;
    private Context mContext;

    private volatile boolean showBitmap = true;
    //width < height，相机能够支持的预览尺寸（相机raw尺寸）
    private int mPreviewWidth;
    private int mPreviewHeight;
    //width < height，实际绘制画面的preview size
    private int mRealPreviewWidth;
    private int mRealPreviewHeight;
    //width<height 控件尺寸，surfaceview的尺寸
    private int mViewWidth;
    private int mViewHeight;

    private boolean first = true;

    private Object lockObj = new Object();
    private static final String TAG = "DrawingHelper";
    public DrawingHelper(Context c) {
        initModelFiles();

        this.mContext = c;
        this.mEGLUtils = new EGLUtils();
        this.mGLCamera = new GLCamera();
        this.mGLBitmap = new GLBitmap(c, getBitmapId());
        this.mGLFrame = new GLFrame();
        this.mGLPoints = new GLPoints();
        this.mMultiTrack106 = new FaceTracking("/sdcard/ZeuseesFaceTracking/models");
    }

    public void init(final Surface surface,final int viewWidth,final int viewHeight){
        Log.d(TAG, "drawinghelper init 实际宽高"+mRealPreviewWidth+mRealPreviewHeight);
        getRealSize(viewWidth,viewHeight);
        mEGLUtils.initEGL(surface);
        mGLCamera.initGLCamera();
        mGLFrame.initFrame();
        mGLFrame.setSize(viewWidth,viewHeight, mPreviewWidth,mPreviewHeight );
        mGLPoints.initPoints();
        mGLBitmap.initFrame(mRealPreviewWidth,mRealPreviewHeight);
        //nv21数组长度与相机raw尺寸相关
        mNv21Data = new byte[mPreviewWidth * mPreviewHeight * 2];

    }

    /**
     * 获得最终预览画面的实际尺寸
     * mRealPreviewWidth
     * mRealPreviewHeight
     *
     * 参数为屏幕宽高比
     */
    public void getRealSize(int screenWidth,int screenHeight){
        float sh = screenWidth*1.0f/screenHeight;//9:16
        float vh = mPreviewWidth *1.0f/ mPreviewHeight;//3:4
        if(sh < vh){
            //预览3:4时走此分支
            Log.d("chang", "rect: sh<vh");
            mRealPreviewWidth = screenWidth;
            mRealPreviewHeight = (int)(mPreviewHeight *1.0f/ mPreviewWidth *mRealPreviewWidth);
        }else{
            mRealPreviewHeight = screenHeight;
            mRealPreviewWidth = (int)(mPreviewWidth *1.0f/ mPreviewHeight *mRealPreviewHeight);
        }
    }
    public void draw(final byte[] data){
        long start = System.currentTimeMillis();
        if (mEGLUtils == null) {
            return;
        }
        synchronized (lockObj) {
            Log.d(TAG, "nv21长度"+mNv21Data.length+"camera "+data.length);
            System.arraycopy(data, 0, mNv21Data, 0, data.length);
        }
        // 1ms ？？？？？
        Log.d(TAG, "复制数组用时 "+ (System.currentTimeMillis()-start));

        long start1 = System.currentTimeMillis();
        ////mNv21Data图片是前置传来的生图，w>h ，但是这里传进去的mPreviewHeight>mPreviewWidth ???
        if(first){
            mMultiTrack106.FaceTrackingInit(mNv21Data,mPreviewWidth,mPreviewHeight);
            first = false;
        }else {
            mMultiTrack106.Update(mNv21Data, mPreviewWidth, mPreviewHeight);
            Log.d(TAG, "人脸识别用时 "+ (System.currentTimeMillis()-start1));
        }


        List<Face> faceActions = mMultiTrack106.getTrackingInfo();
        Log.d(TAG, "draw: 检测到人脸 "+faceActions.size());
        float[] p = null;
        float[] points = null;
        for (Face r : faceActions) {
            points = new float[106 * 2];
            for (int i = 0; i < 106; i++) {
                int x = r.landmarks[i * 2];
                int y = r.landmarks[i * 2 + 1];
                //以下绘制时均用预览尺寸，因为会归一化，所以最后转到控件尺寸后会等比例放大
                points[i * 2] = view2openglX(x, mPreviewWidth);
                points[i * 2 + 1] = view2openglY(y, mPreviewHeight);
                //贴图的坐标点 倒z字顺序
                if (i == 50) {
                    p = new float[8];
                    p[0] = view2openglX(x + 10, mPreviewWidth);
                    p[1] = view2openglY(y - 10, mPreviewHeight);
                    p[2] = view2openglX(x - 10, mPreviewWidth);
                    p[3] = view2openglY(y - 10, mPreviewHeight);
                    p[4] = view2openglX(x + 10, mPreviewWidth);
                    p[5] = view2openglY(y + 10, mPreviewHeight);
                    p[6] = view2openglX(x - 10, mPreviewWidth);
                    p[7] = view2openglY(y + 10, mPreviewHeight);
                    for (int j = 0; j < p.length; j++) {
                        Log.d(TAG, "draw: 贴图数组 "+j+" 为"+p[j]);
                    }

                }
            }
            if (p != null) {
                break;
            }
        }
        int tid = 0;
        if (p != null && showBitmap) {
            //绘制特定点位置贴图bitmap（貌似是绘制在FrameBuffer中）
            mGLBitmap.setPoints(p);
            tid = mGLBitmap.drawFrame();
        }
        //绘制相机回调以及特定位置贴图bitmap（如果有的话）
        mGLCamera.update();
        mGLFrame.drawFrame(tid, mGLCamera.getTexture(), mGLCamera.getMatrix());
        if (points != null) {
            //绘制所有检测点
            mGLPoints.setPoints(points);
            mGLPoints.drawPoints();

        }
        mEGLUtils.swap();
        // 4:3 超不过50 16:9 稳定后100左右，占大头的是mMultiTrack106.Update() 方法
        Log.d(TAG, "draw一帧用时 "+ (System.currentTimeMillis()-start));
    }
    public void release(){
        mGLBitmap.release();
        mGLFrame.release();
        mGLCamera.release();
        mGLPoints.release();
        mEGLUtils.release();

        try {
            mMultiTrack106.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void setPreviewWidth(int mPreviewWidth) {
        this.mPreviewWidth = mPreviewWidth;
    }

    public void setPreviewHeight(int mPreviewHeight) {
        this.mPreviewHeight = mPreviewHeight;
    }

    public SurfaceTexture getSurfaceTexture(){
        return mGLCamera.getSurfaceTexture();
    }
    //改变贴图开关状态
    public void invertShowBitmap() {
        this.showBitmap = !showBitmap;
    }

    private int getBitmapId(){
        return R.drawable.ic_launcher_foreground;
    }

    //归一化坐标[-1,1]
    private float view2openglX(int x, int width) {
        float centerX = width / 2.0f;
        float t = x - centerX;
        return t / centerX;
    }

    private float view2openglY(int y, int height) {
        float centerY = height / 2.0f;
        float s = centerY - y;
        return s / centerY;
    }
    private void initModelFiles()
    {

        String assetPath = "ZeuseesFaceTracking";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + assetPath;
        copyFilesFromAssets(mContext, assetPath, sdcardPath);

    }
    private void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                // directory
                File file = new File(newPath);
                if(file.exists())
                    return;
                if (!file.mkdir())
                {
                    Log.d("mkdir","can't make folder");

                }

                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
