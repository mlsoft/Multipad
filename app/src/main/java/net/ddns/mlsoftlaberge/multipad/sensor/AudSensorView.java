package net.ddns.mlsoftlaberge.multipad.sensor;

/*
*  By Martin Laberge (mlsoft), From March 2016 to november 2016.
*  Licence: Can be shared with anyone, for non profit, provided my name stays in the comments.
*  This is a conglomerate of examples codes found in differents public forums on internet.
*  I just used the public knowledge to fit a special way to use an android phone functions.
*/

/* Copyright 2016 Martin Laberge
*
*        Licensed under the Apache License, Version 2.0 (the "License");
*        you may not use this file except in compliance with the License.
*        You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*
*        Unless required by applicable law or agreed to in writing, software
*        distributed under the License is distributed on an "AS IS" BASIS,
*        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*        See the License for the specific language governing permissions and
*        limitations under the License.
*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by mlsoft on 16-06-25.
 */
// ============================================================================
// class defining the sensor display widget
public class AudSensorView extends TextView {
    private Bitmap mBitmap;
    private Paint mPaint = new Paint();
    private Paint mPaint2 = new Paint();
    private Canvas mCanvas = new Canvas();

    private int mWidth=0;
    private int mHeight=0;

    // initialize the 3 colors, and setup painter
    public AudSensorView(Context context) {
        super(context);
        // text paint
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);
        mPaint.setTextSize(24);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        // line paint
        mPaint2.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setStrokeWidth(2);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setColor(Color.YELLOW);
    }

    // ======= timer section =======
    private Timer timer=null;
    private MyTimer myTimer;

    //private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord=null;
    private int bufferSize=0;

    int BufferElements2Rec = 2048; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    public void stop() {
        if(timer!=null) {
            timer.cancel();
            timer=null;
        }
        if(mAudioRecord!=null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord=null;
        }
    }

    public void start() {
        // start the recording
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);
        mAudioRecord.startRecording();
        // start the timer to eat this stuff and display it
        timer = new Timer("audiowave");
        myTimer = new MyTimer();
        timer.schedule(myTimer, 10L, 10L);
    }

    private short sData[] = new short[BufferElements2Rec];
    private int nbe=0;

    private class MyTimer extends TimerTask {
        public void run() {
            nbe = BufferElements2Rec>mWidth ? mWidth : BufferElements2Rec;
            mAudioRecord.read(sData,0,nbe);
            postInvalidate();
        }
    }

    // =========== textview callbacks =================
    // initialize the bitmap to the size of the view, fill it white
    // init the view state variables to initial values
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.BLACK);
        mWidth = w;
        mHeight = h;
    }

    // draw
    @Override
    public void onDraw(Canvas viewcanvas) {
        synchronized (this) {
            if (mBitmap != null) {
                // clear the surface
                mCanvas.drawColor(Color.BLACK);
                // draw the horizontal line
                mPaint.setColor(Color.WHITE);
                mPaint.setStrokeWidth(1.0f);
                mCanvas.drawLine(0,mHeight/2,mWidth,mHeight/2,mPaint);
                // draw the square
                mPaint.setColor(Color.MAGENTA);
                mPaint.setStrokeWidth(2.0f);
                mCanvas.drawLine(0,0,mWidth,0,mPaint);
                mCanvas.drawLine(0,0,0,mHeight,mPaint);
                mCanvas.drawLine(mWidth-1,mHeight-1,mWidth-1,0,mPaint);
                mCanvas.drawLine(mWidth-1,mHeight-1,0,mHeight-1,mPaint);
                // draw the text
                mPaint.setColor(Color.GREEN);
                mPaint.setStrokeWidth(2.0f);
                mPaint.setAntiAlias(true);
                mPaint.setTextSize(20);
                mPaint.setStyle(Paint.Style.STROKE);
                mCanvas.drawText("Audio",10,20,mPaint);
                // draw the sound wave
                int posx;
                int posy;
                int newx;
                int newy;
                int maxy;
                int yscale;
                maxy=mHeight/2;
                yscale=(32768/maxy)/2;    // increase noise level by 4
                posx=0;
                posy=maxy;
                for(int i=1;i<nbe;++i) {
                    newx=i;
                    newy=maxy+(sData[i]/yscale);
                    mCanvas.drawLine((float)posx,(float)posy,(float)newx,(float)newy,mPaint2);
                    posx=newx;
                    posy=newy;
                }
                // transfer the bitmap to the view
                viewcanvas.drawBitmap(mBitmap, 0, 0, null);
            }
        }
        super.onDraw(viewcanvas);
    }

}

