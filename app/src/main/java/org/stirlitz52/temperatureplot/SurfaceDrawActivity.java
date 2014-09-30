package org.stirlitz52.temperatureplot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import org.stirlitz52.temperatureplot.measurements.MeasureEvent;
import org.stirlitz52.temperatureplot.measurements.MeasureEventList;
import org.stirlitz52.temperatureplot.measurements.MeasureEventListener;
import org.stirlitz52.temperatureplot.measurements.MeasureService;
import org.stirlitz52.temperatureplot.measurements.MeasureService.LocalBinder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class SurfaceDrawActivity extends Activity implements MeasureEventListener {

    protected static final String TAG = "GraphPlotActivity";
    boolean isThread = false;

    public GraphView graphView;
    private final Thread graphMaker = new Thread(new Runnable() {
        public void run() {
            while(true) {
                Date date = new Date();
                date.setTime(date.getTime() - 30000);
                if (mMeasureService.equals(NULL));
                graphView.setToDraw(mMeasureService.getMeasureEventList(date));
                Log.d(SurfaceDrawActivity.TAG, "gv ctor");
                try {
                    graphMaker.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isThread == false) break;
            }
        }
    });

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        graphView = new GraphView(this);
        setContentView(new GraphView(this));

        startService(new Intent(this, MeasureService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MeasureService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        graphMaker.start();
        isThread = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isThread = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            mMeasureService.unregisterMeasureEventListener(this);
            unbindService(mServiceConnection);
            mBound = false;
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MeasureService.class));
    }

    private MeasureService mMeasureService;
    private boolean mBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mMeasureService = binder.getService();
            mBound = true;
            mMeasureService.registerMeasureEventListener(SurfaceDrawActivity.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onNewMeasure(final MeasureEvent event) {
        Log.i(TAG, "New measure from " + event.source + " taken at " + event.measured_at + " value=" + event.data[0] + event.unit);
        };

        public static class GraphView extends View {
        private static Paint paint;
        private int screenW, screenH;
        private float X, Y;
        private Path path;
        private float initialScreenW;
        private float initialX, plusX;
        private float TX;
        private boolean translate;
        private Context context;
        ArrayList<MeasureEvent> lastList = new  ArrayList<MeasureEvent>();


        public GraphView(Context context) {
            super(context);

            Log.d(SurfaceDrawActivity.TAG, "gv ctor" + context);

            this.context=context;

            paint = new Paint();
            paint.setColor(Color.argb(0xff, 0x99, 0x00, 0x00));
            paint.setStrokeWidth(10);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            paint.setShadowLayer(7, 0, 0, Color.RED);


            path= new Path();
            TX=0;
            translate=false;

        }

        @Override
        public void onSizeChanged (int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            screenW = w;
            screenH = h;
            X = 0;
            Y = (screenH/2)+(screenH/4)+(screenH/10);

            initialScreenW=screenW;
            initialX=((screenW/2)+(screenW/4));
            plusX=(screenW/24);

            path.moveTo(X, Y);
        }

        public ArrayList<MeasureEvent> setToDraw (ArrayList<MeasureEvent> mList)
        {
            int s = mList.size();
            lastList.addAll(mList);
            return lastList;
        }



        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //canvas.save();

            path.lineTo(X,Y);
            canvas.translate(-TX, 0);
            if(translate==true)
            {
                TX+=4;
            }

            if(X<initialX)
            {
                X+=8;
            }
            else
            {
                //necessary to change translate to true to begin moving graph

            }

            canvas.drawPath(path, paint);


            //canvas.restore();

            invalidate();
        }
    }

}
