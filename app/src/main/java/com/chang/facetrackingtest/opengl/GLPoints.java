package com.chang.facetrackingtest.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLPoints {
    private FloatBuffer vertexBuffer;
    private int bufferLength = 106*2*4;
    private int programId = -1;
    private int aPositionHandle;
    private int iLocation;


    private int[] vertexBuffers;


    private String fragmentShader =
            "uniform highp float i;\n"+
            "void main() {\n" +
                    "if(i== 1.0){" +
                    "gl_FragColor = vec4(1.0,1.0,0.0,1.0);}" +
                    "else{gl_FragColor = vec4(1.0,0.0,0.0,1.0);}\n"+
                    "}";
    private  String vertexShader = "attribute vec2 aPosition;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(aPosition,0.0,1.0);\n" +
            "    gl_PointSize = 10.0;\n"+
            "}";
    public GLPoints(){
        vertexBuffer = ByteBuffer.allocateDirect(bufferLength)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.position(0);
    }
    public void initPoints(){
        programId = ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        iLocation = GLES20.glGetUniformLocation(programId,"i");
        vertexBuffers = new int[1];
        GLES20.glGenBuffers(1,vertexBuffers,0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferLength, vertexBuffer,GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }
    public void setPoints(float[] points){
        vertexBuffer.rewind();
        vertexBuffer.put(points);
        vertexBuffer.position(0);
    }


    public void drawPoints(){

        GLES20.glUseProgram(programId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffers[0]);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER,0,bufferLength,vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false,
                0, 0);


        //黄色点 0~49
        GLES20.glUniform1f(iLocation,1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 50);

        //红色点 50
        GLES20.glUniform1f(iLocation,0.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 50, 1);

        //黄色点 51~105
        GLES20.glUniform1f(iLocation,1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 51, 55);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void release(){
        GLES20.glDeleteProgram(programId);
        GLES20.glDeleteBuffers(1,vertexBuffers,0);
    }
}
