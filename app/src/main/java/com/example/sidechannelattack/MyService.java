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
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.*;
public class MyService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private static long count = 0;
    private static File baseDir;
    final static String MY_ACTION = "com.example.sidechannelattack.MyService.MY_ACTION";
    HashMap<String, Float> prev = new HashMap<>();
    List<Sensor> sensorsList = new ArrayList<>();
    Intent intent = new Intent(MY_ACTION);
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        File file = new File(android.os.Environment.getExternalStorageDirectory()+"/SCA");
        boolean dirCreated = file.mkdirs();
        if(!file.exists()) {
            System.out.println("Directory created!!: " + dirCreated);
        }
        baseDir = file;
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
        String sensorName = event.sensor.getName();
        if(!baseDir.exists()) {
            if(!baseDir.mkdirs()) return;
        }
        float currentValue = event.values[0];
        long time = event.timestamp;
        Date date = java.util.Calendar.getInstance().getTime();

        long idxTime = System.currentTimeMillis() - count;

        if(sensorName.toLowerCase().contains("acceleration") || sensorName.toLowerCase().contains("gyroscope")) {
            String[] dirs = { "-x", "-y", "-z" };
            double mod = 0.0;
            for(int i = 0; i < 3; i++) {
                mod = Math.pow(event.values[i], 2);
                String[] data = {Float.toString(event.values[i]), Long.toString(time), date.toString(), Long.toString(idxTime), Long.toString(System.nanoTime())};

                writeInFile(event.values[i], sensorName + dirs[i], data);
            }
            currentValue = (float) Math.sqrt(mod);
        }
        String[] data = {Float.toString(currentValue), Long.toString(time), date.toString(), Long.toString(idxTime), Long.toString(System.nanoTime())};
        writeInFile(currentValue, sensorName, data);
    }

    public void writeInFile(Float curValue, String sensorName, String[] data) {
        if(!prev.containsKey(sensorName)) prev.put(sensorName, 0.00f);
        if (curValue.equals(prev.get(sensorName)))
            return;
        String fileName = sensorName + ".csv";
        String filePath = baseDir.getAbsolutePath() + File.separator + fileName;
        prev.put(sensorName, curValue);
        File f = new File(filePath);
        try {
            FileWriter fOut = new FileWriter(f, true);
            CSVWriter writer = new CSVWriter(fOut);
            Log.d("MyService", "=====##### Name is "+ sensorName + " &&& Value is " + data[0] + " #####=====");
            if(f.length() == 0) {
                String[] heads = {"Value", "timestamp", "DateString", "time-diff", "systemNanoTime"};
                writer.writeNext(heads, false);
            }
            writer.writeNext(data, false);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
