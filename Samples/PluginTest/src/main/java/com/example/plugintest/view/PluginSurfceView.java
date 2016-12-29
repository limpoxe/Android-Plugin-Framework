package com.example.plugintest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by cailiming on 16/12/29.
 */

public class PluginSurfceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;

    public PluginSurfceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        holder = this.getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //just for test
        new Thread(new MyThread()).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    class MyThread implements Runnable {
        @Override
        public void run() {
            Canvas canvas = holder.lockCanvas(null);
            Paint mPaint = new Paint();
            mPaint.setColor(Color.BLUE);
            canvas.drawRect(new RectF(40,60,80,80), mPaint);
            holder.unlockCanvasAndPost(canvas);
        }
    }
}
