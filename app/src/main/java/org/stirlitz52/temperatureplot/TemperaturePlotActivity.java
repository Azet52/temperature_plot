package org.stirlitz52.temperatureplot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.stirlitz52.temperatureplot.measurements.MeasureEvent;
import org.stirlitz52.temperatureplot.measurements.MeasureEventListener;
import org.stirlitz52.temperatureplot.measurements.MeasureService;
import org.stirlitz52.temperatureplot.measurements.MeasureService.LocalBinder;


public class TemperaturePlotActivity extends Activity implements MeasureEventListener {

    public final String TAG = "TemperaturePlotActivity";

    TextView textCurrentTemperature, textAverageTemperature, textSource, textMeasureTime;
    ImageView imageCurrentTemperature, imageAverageTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_plot);

        textCurrentTemperature = (TextView) findViewById(R.id.textMainValue);
        textAverageTemperature = (TextView) findViewById(R.id.textAverageValue);
        textSource = (TextView) findViewById(R.id.textSource);
        textMeasureTime = (TextView) findViewById(R.id.textAtTime);

        imageCurrentTemperature = (ImageView) findViewById(R.id.imageMainValue);
        imageAverageTemperature = (ImageView) findViewById(R.id.imageAverageValue);

        startService(new Intent(this, MeasureService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MeasureService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
            mMeasureService.registerMeasureEventListener(TemperaturePlotActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.temperature_plot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNewMeasure(final MeasureEvent event) {
        Log.i(TAG, "New measure from " + event.source + " taken at " + event.measured_at + " value=" + event.data[0] + event.unit);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               textSource.setText(event.source);
               textMeasureTime.setText("taken at: " + event.measured_at);
               textCurrentTemperature.setText(event.data[0] + event.unit);
            }
        });


    }



}
