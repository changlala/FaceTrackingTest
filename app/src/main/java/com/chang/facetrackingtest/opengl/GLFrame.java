package com.chang.facetrackingtest.opengl;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLFrame {
    private int width,height,screenWidth,screenHeight;

    private final float[] vertexData = {
            1f, -1f, 0f,
            -1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f
    };
    private final float[] textureVertexData = {
            //a
//            1f, 0f,
//            0f, 0f,
//            1f, 1f,
//            0f, 1f

              //b
//            1f, 1f,
//            0f, 1f,
//            1f, 0f,
//            0f, 0f

            // c 前置 顺90
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f

//            0f, 1f,
//            0f, 0f,
//            1f, 1f,
//            1f, 0f
    };

    private final float[] bitmapTextureVertexData = {
            //bitmap贴图用coord
            1f, 1f,
            0f, 1f,
            1f, 0f,
            0f, 0f

//            1f, 0f,
//            0f, 0f,
//            1f, 1f,
//            0f, 1f
    };
    private FloatBuffer vertexBuffer;

    private FloatBuffer textureVertexBuffer;
    private FloatBuffer bitmapTextureVertexBuffer;

    private int programId = -1;
    private int aPositionHandle;

    private int uTextureSamplerHandle;
    private int iTextureSamplerHandle;
    private int aTextureCoordHandle;
    private int bitmapTexCoordHandle;

    private int uSTMMatrixHandle;

    private int sHandle;
    private int hHandle;
    private int lHandle;

    private int iHandle;

    private int[] vertexBuffers;

    private String fragmentShader = "#extension GL_OES_EGL_image_external : require\n" +
            "varying highp vec2 vTexCoord;\n" +
            "varying highp vec2 bTexCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "uniform sampler2D iTexture;\n" +
            "uniform highp mat4 uSTMatrix;\n" +
            "uniform highp float S;\n" +
            "uniform highp float H;\n"+
            "uniform highp float L;\n"+
            "uniform highp float i;\n"+
            "highp vec3 rgb2hsv(highp vec3 c){\n" +
            "    highp vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);\n" +
            "    highp vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));\n" +
            "    highp vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));\n" +
            "    highp float d = q.x - min(q.w, q.y);\n" +
            "    highp float e = 1.0e-10;\n" +
            "    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);\n" +
            "}\n" +
            "highp vec3 hsv2rgb(highp vec3 c){\n" +
            "    highp vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);\n" +
            "    highp vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);\n" +
            "    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);\n" +
            "}"+
            "void main() {\n" +

            //以下配合a正常
//            "    highp vec2 tx_transformed = (uSTMatrix * vec4(vTexCoord, 0, 1.0)).xy;\n" +

            //以下配合c 显示效果正确
            "    highp vec2 tx_transformed = vTexCoord;\n" +

            "    highp vec4 video = texture2D(sTexture, tx_transformed);\n" +
            "    highp vec4 rgba;\n"+
            "    if(i == 0.0){\n" +
            "       rgba = video;\n" +
            "    }\n"+
            "    else{\n" +
            "       highp vec4 image = texture2D(iTexture, bTexCoord);\n" +
            "       rgba = mix(video,image,image.a);\n"+
            "    }\n"+
//            "    highp vec3 hsl = rgb2hsv(rgba.xyz);\n"+
//            "    if(H != 0.0)hsl.x = H;\n" +
//            "    if(hsl.x<0.0)hsl.x = hsl.x+1.0;\n" +
//            "    else if(hsl.x>1.0)hsl.x = hsl.x-1.0;\n"+
//            "    if(S != 1.0)hsl.y = hsl.y*S;\n"+
//            "    highp vec3 rgb = hsv2rgb(hsl);\n" +
//            "    if (L < 0.0) rgb = rgb + rgb * vec3(L);\n"+
//            "    else rgb = rgb + (1.0 - rgb) * vec3(L);\n"+
//            "    gl_FragColor = vec4(rgb,rgba.w);\n" +
            "    gl_FragColor = vec4(rgba);\n" +
            "}";
    private  String vertexShader = "attribute vec4 aPosition;\n" +
            "attribute vec2 aTexCoord;\n" +
            "attribute vec2 bitmapTexCoord;\n" +
            "varying vec2 vTexCoord;\n" +
            "varying vec2 bTexCoord;\n" +
            "void main() {\n" +
            "    vTexCoord = aTexCoord;\n" +
            "    bTexCoord = bitmapTexCoord;\n" +
            "    gl_Position = aPosition;\n" +
            "}";
    public GLFrame(){
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        bitmapTextureVertexBuffer = ByteBuffer.allocateDirect(bitmapTextureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(bitmapTextureVertexData);
        bitmapTextureVertexBuffer.position(0);
    }
    public void initFrame(){
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "sTexture");
        iTextureSamplerHandle = GLES20.glGetUniformLocation(programId, "iTexture");
        aTextureCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");
        bitmapTexCoordHandle = GLES20.glGetAttribLocation(programId, "bitmapTexCoord");
        sHandle =GLES20.glGetUniformLocation(programId,"S");
        hHandle =GLES20.glGetUniformLocation(programId,"H");
        lHandle =GLES20.glGetUniformLocation(programId,"L");

        //i标识是否有贴图tex
        iHandle =GLES20.glGetUniformLocation(programId,"i");

        //生成两个顶点buffer，保存到vertexBuffers[0] vertexBuffers[1]
        vertexBuffers = new int[3];
        GLES20.glGenBuffers(3,vertexBuffers,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length*4, vertexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureVertexData.length*4, textureVertexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[2]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bitmapTextureVertexData.length*4, bitmapTextureVertexBuffer,GLES20.GL_STATIC_DRAW);

        //结束对顶点buffer的处理
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private float s = 1.0f;
    public void setS(float s){
        this.s = s;
    }
    private float h = 0.0f;
    public void setH(float h) {
        this.h = h;
    }
    private float l = 1.0f;
    public void setL(float l) {
        this.l = l;
    }


    private Rect rect = new Rect();
    public void setSize(int screenWidth,int screenHeight,int videoWidth,int videoHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.width = videoWidth;
        this.height = videoHeight;
        rect();
    }


    /**
     * 规定绘制范围，根据分辨率的大小随之改变 自动居中
     */
    private void rect(){
        int left,top,viewWidth,viewHeight;
        float sh = screenWidth*1.0f/screenHeight;//9:16
        float vh = width *1.0f/ height;//3:4
        if(sh < vh){
            //预览3:4时走此分支
            Log.d("chang", "rect: sh<vh");
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int)(height *1.0f/ width *viewWidth);
            top = (screenHeight - viewHeight)/2;
        }else{
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int)(width *1.0f/ height *viewHeight);
            left = (screenWidth - viewWidth)/2;
        }
        rect.left = left;
        rect.top = top;
        rect.right = viewWidth;
        rect.bottom = viewHeight;
    }

    public void drawFrame(int tId,int textureId,float[] STMatrix){
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(rect.left, rect.top, rect.right, rect.bottom);
        GLES20.glUseProgram(programId);

        //关联属性与顶点buffer
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false,
                0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[1]);
        GLES20.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES20.glVertexAttribPointer(aTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[2]);
        GLES20.glEnableVertexAttribArray(bitmapTexCoordHandle);
        GLES20.glVertexAttribPointer(bitmapTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //关联相机预览tex 激活纹理单元0，并将其绑定到纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        Log.d("changchang", "textureId "+textureId);
        GLES20.glUniform1i(uTextureSamplerHandle,0);
        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, STMatrix, 0);

        //关联贴图tex 激活纹理单元1，并将其绑定到纹理单元1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tId);
        GLES20.glUniform1i(iTextureSamplerHandle,1);

        GLES20.glUniform1f(sHandle,s);
        GLES20.glUniform1f(hHandle,h);
        GLES20.glUniform1f(lHandle,l);
        GLES20.glUniform1f(iHandle,tId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void release(){
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(2,vertexBuffers,0);
    }
}
