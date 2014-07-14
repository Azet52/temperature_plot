package org.stirlitz52.temperatureplot.measurements;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.stirlitz52.temperatureplot.R;

public class MeasureService extends Service implements Runnable {

    private final String TAG = "MeasureService";
    private final IBinder mBinder = new LocalBinder();
    private final Thread mThread = new Thread(null, this, "MeasureService");
    private final Random mGenerator = new Random();

    private final List<MeasureEventListener> mListeners = new ArrayList<MeasureEventListener>();

    /**
     * Class for clients to access. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public MeasureService getService() {
            return MeasureService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        mThread.start();
        Toast.makeText(this, R.string.measure_service_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            mThread.interrupt();
            mThread.join();
        } catch (InterruptedException e) {
        }
        Toast.makeText(this, R.string.measure_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void run() {
        synchronized (mListeners) {
            while (true) {
                try {

                    // wait 0.5s-1.5s
                    int randomWaitTime = mGenerator.nextInt(1000) + 500;
                    mListeners.wait(randomWaitTime);

                    MeasureEvent measure = getDummyMeasure("/therm01/temperature");
                    for (MeasureEventListener listener : mListeners)
                        listener.onNewMeasure(measure);
                } catch (InterruptedException e) {
                    Log.i(TAG, "Service thread interrupted");
                    break;
                }
            }
        }
    }

    public void registerMeasureEventListener(final MeasureEventListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void  unregisterMeasureEventListener(final MeasureEventListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    private MeasureEvent getDummyMeasure(String path) {
        // temperature from -10 to 30
        double tempTemperature = (mGenerator.nextInt(400) - 100) / 10.;
        return new MeasureEvent(MeasureEvent.MeasureEventType.TEMPERATURE,
                                path, new double[] {tempTemperature }, new Date(), "C");

    }
}