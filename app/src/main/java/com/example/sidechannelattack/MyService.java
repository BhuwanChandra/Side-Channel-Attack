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
import java.util.Date;
import java.util.*;
public class MyService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private static String fileName;
    private static String filePath;
    private static long count = 0;
    private static File f;
    private static String baseDir;
    final static String MY_ACTION = "com.example.sidechannelattack.MyService.MY_ACTION";
    HashMap<String, Float> prev = new HashMap<String, Float>();
    List<Sensor> sensorsList = new ArrayList<Sensor>();
    Intent intent = new Intent(MY_ACTION);
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        File file = new File(android.os.Environment.getExternalStorageDirectory()+"/SCA");
        if(!file.exists())
            file.mkdir();
        //String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        baseDir = file.getAbsolutePath();
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        List<Sensor> availableSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i = 0; i < availableSensors.size(); i++) {
            Sensor currentSensor = availableSensors.get(i);
            String sensorName = currentSensor.getName();
            Sensor sensor = mSensorManager.getDefaultSensor(currentSensor.getType());
            sensorsList.add(sensor);
            prev.put(sensorName, 0.00f);
        }
        super.onCreate();
        count = System.currentTimeMillis();
        Log.d("MyService", "onCreate");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        long time = event.timestamp;
        Date date =java.util.Calendar.getInstance().getTime();
        WriteSensorValue(event);
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyService", "onStartCommand");
        for (int i = 0; i < sensorsList.size(); i++) {
            Sensor currentSensor = sensorsList.get(i);
            if(currentSensor!=null)
                mSensorManager.registerListener(this, currentSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
        }
        return START_STICKY;
    }

    public void WriteSensorValue(SensorEvent event) {
        int sensorType = event.sensor.getType();
        String sensorName = event.sensor.getName();
        float prevValue = prev.get(sensorName);
        float currentValue = event.values[0];
        System.out.println("Name is "+ sensorName + " Value is " + currentValue);
        long time = event.timestamp;
        Date date = java.util.Calendar.getInstance().getTime();
        if (currentValue == prevValue)
            return;
        fileName = sensorName + ".csv";
        filePath = baseDir + File.separator + fileName;
        prev.put(sensorName, prevValue);
        f = new File(filePath);
        try {
            long idxTime = System.currentTimeMillis() - count;
            FileWriter fOut = new FileWriter(filePath, true);
            CSVWriter writer = new CSVWriter(fOut);
            String temp = Float.toString(currentValue);
            String[] data = {temp, Long.toString(time), date.toString(), Long.toString(idxTime)};
            writer.writeNext(data, false);
            writer.close();
        } catch (Exception e) {
        }
    }
}
