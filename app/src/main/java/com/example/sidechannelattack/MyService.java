package com.example.sidechannelattack;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MyService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    // Individual light and proximity sensors.
    private Sensor mSensorProximity;
    private Sensor mSensorLight;
    private static float currentLightValue = 0;
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
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        long time = event.timestamp;
        if (currentValue == currentLightValue) return;
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                String fileName = "Light.csv";
                String filePath = baseDir + File.separator + fileName;
                currentLightValue = currentValue;
                File f = new File(filePath);
                try {
                    FileWriter fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);

                    String light = Float.toString(currentValue);
                    intent.putExtra("light", light);
                    System.out.println("Light value is " + currentValue + " for time: " + time);

                    String[] data = { light , Long.toString(time)};
                    writer.writeNext(data, false);
                    writer.close();
//
                } catch (Exception e) {

                }

                break;
            case Sensor.TYPE_PROXIMITY:
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
