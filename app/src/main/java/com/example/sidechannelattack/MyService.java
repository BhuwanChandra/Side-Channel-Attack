package com.example.sidechannelattack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;

public class MyService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    // Individual light and proximity sensors.
    private Sensor mSensorProximity;
    private Sensor mSensorLight;
    final static String MY_ACTION = "com.example.sidechannelattack.MyService.MY_ACTION";
    Intent intent = new Intent(MY_ACTION);
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
       super.onCreate();
       Log.d("MyService", "onCreate");
       mSensorManager = (SensorManager) getSystemService(
               Context.SENSOR_SERVICE);
       mSensorProximity =
               mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
       mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

   }
    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];

        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
//                mTextSensorLight.setText(getResources().getString(
//                        R.string.label_light, currentValue));
                String light=Float.toString(currentValue);
                intent.putExtra("light", light);
//                System.out.println(intent);
                System.out.println("Light value is" + currentValue);
                break;
            case Sensor.TYPE_PROXIMITY:
//                mTextSensorProximity.setText(getResources().getString(
//                        R.string.label_proximity, currentValue));
                String proxy=Float.toString(currentValue);
                intent.putExtra("proximity", proxy);
//                System.out.println("Proxy value is" + currentValue);
                break;
            default:
                // do nothing
        }
        sendBroadcast(intent);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("MyService", "onStartCommand");
        if (mSensorProximity != null) {
            mSensorManager.registerListener(this, mSensorProximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        return START_STICKY;
    }

}
