package com.chang.facetrackingtest.opengl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

public class GLCamera {
    private static final String TAG = "GLCamera";
    private float[] mSTMatrix = new float[16];

    private int[] textures;

    private SurfaceTexture mSurfaceTexture;
    public GLCamera() {
//        textures = new int[1];
//        GLES20.glGenTextures(1, textures, 0);
//        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
//                GLES20.GL_NEAREST);
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
//                GLES20.GL_LINEAR);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

//        mSurfaceTexture = new SurfaceTexture(textures[0]);
//        mSurfaceTexture = new SurfaceTexture(0);
    }

    public void initGLCamera(){
        textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        Log.d("changchang", "textures[0] "+textures[0]);
        mSurfaceTexture = new SurfaceTexture(textures[0]);
    }

    /**
     * TransformMatrix
     * 前置时总为：
     *  0,-1,0,0,
     *  1,0,0,0,
     *  0,0,1,0,
     *  0,1,0,1
     */
    public void update(){
        if(mSurfaceTexture != null){
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mSTMatrix);

            for (int i = 0; i < mSTMatrix.length; i++) {
                Log.d(TAG, "TransformMatrix: "+i+" "+mSTMatrix[i]);
            }

        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public int getTexture() {
        return textures[0];
    }
    public float[] getMatrix() {
        return mSTMatrix;
    }
    public void release(){
        GLES20.glDeleteTextures(1,textures,0);
        if(mSurfaceTexture != null ){
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }
}
