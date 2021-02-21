package com.example.sidechannelattack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;


import androidx.annotation.Nullable;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    // Individual light and proximity sensors.
    private Sensor mSensorProximity;
    private Sensor mSensorLight;
    long dateBase ;
//    long timestampBase;
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
        String time ;
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                String fileName = "Light.csv";
                String filePath = baseDir + File.separator + fileName;
                CSVWriter writer;
                System.out.println(filePath);
                File f = new File(filePath);
//                mTextSensorLight.setText(getResources().getString(
//                        R.string.label_light, currentValue));
                    dateBase = (new Date()).getTime();
                    String light = Float.toString(currentValue);
                    intent.putExtra("light", light);
//                System.out.println(intent);
                    System.out.println("Light value is" + currentValue);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    System.out.println(sdf.format(new Date(dateBase)));
//                System.out.println((new Date()).getTime());
                try {
                    if(f.exists()&&!f.isDirectory())
                    {
                        FileWriter mFileWriter = new FileWriter(filePath, true);
                        writer = new CSVWriter(mFileWriter);
                    }
                    else
                    {
                        writer = new CSVWriter(new FileWriter(filePath));
                    }

                    String[] data = { light , sdf.format(new Date(dateBase))};
                    writer.writeNext(data);

                    writer.close();
//
                } catch (IOException e) {

                }

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
