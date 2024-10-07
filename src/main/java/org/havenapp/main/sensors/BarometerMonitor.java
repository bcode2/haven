package org.havenapp.main.sensors;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.havenapp.main.PreferenceManager;
import org.havenapp.main.model.EventTrigger;
import org.havenapp.main.service.MonitorService;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by n8fr8 on 3/10/17.
 */
public class BarometerMonitor implements SensorEventListener {

    // For shake motion detection.
    private final SensorManager sensorMgr;

    /**
     * Last update of the accelerometer
     */
    private long lastUpdate = -1;

    /**
     * Last accelerometer values
     */
    private float last_accel_values[];


    private int remainingAlertPeriod = 0;
    private boolean alert = false;
    private final static int CHECK_INTERVAL = 1000;

    public BarometerMonitor(Context context) {
        /**
         * Data field used to retrieve application prefences
         */
        PreferenceManager prefs = new PreferenceManager(context);



        context.bindService(new Intent(context,
                MonitorService.class), mConnection, Context.BIND_ABOVE_CLIENT);

        sensorMgr = (SensorManager) context.getSystemService(AppCompatActivity.SENSOR_SERVICE);
        /**
         * Barometer sensor
         */
        Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (sensor == null) {
            Log.i("Pressure", "Warning: no barometer sensor");
        } else {
            sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Safe not to implement

    }

    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();

        // only allow one update every 100ms.
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {

            if ((curTime - lastUpdate) > CHECK_INTERVAL) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                /**
                 * Current accelerometer values
                 */
                float[] accel_values = event.values.clone();

                if (alert && remainingAlertPeriod > 0) {
                    remainingAlertPeriod = remainingAlertPeriod - 1;
                } else {
                    alert = false;
                }

                if (last_accel_values != null) {

                    float diffValue = Math.abs(accel_values[0] - last_accel_values[0]);
                    Log.d("Pressure","diff: " + diffValue);
                    //hPa or mbar
                    int CHANGE_THRESHOLD = 30;
                    boolean logit = (diffValue > CHANGE_THRESHOLD);

                    if (logit) {
						/*
						 * Send Alert
						 */

                        alert = true;
                        /**
                         * Text showing accelerometer values
                         */
                        int maxAlertPeriod = 30;
                        remainingAlertPeriod = maxAlertPeriod;

                        Message message = new Message();
                        message.what = EventTrigger.PRESSURE;
                        message.getData().putString(MonitorService.KEY_PATH, diffValue+"");

                        try {
                            if (serviceMessenger != null) {
                                serviceMessenger.send(message);
                            }
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                last_accel_values = accel_values.clone();
            }
        }
    }

    public void stop(Context context) {
        sensorMgr.unregisterListener(this);
        context.unbindService(mConnection);
    }

    private Messenger serviceMessenger = null;

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i("AccelerometerFragment", "SERVICE CONNECTED");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            serviceMessenger = new Messenger(service);
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.i("AccelerometerFragment", "SERVICE DISCONNECTED");
            serviceMessenger = null;
        }
    };

}
