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
    private Sensor mSensorProximity;
    private Sensor mSensorLight;
    private Sensor mSensorTemperature;
    private Sensor mSensorPressure;
    private static float currentLightValue = 0;
    private static float currentProxValue = 0;
    private static float currentTempValue = 0;
    private static float currentPressValue = 0;
    private static String fileName;
    private static String filePath;
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
            Sensor sensor=mSensorManager.getDefaultSensor(currentSensor.getType());
            sensorsList.add(sensor);
            prev.put(sensorName, 0.0f);
        }
        super.onCreate();
        Log.d("MyService", "onCreate");
        mSensorProximity =
                mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mSensorPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        long time = event.timestamp;
        Date date =java.util.Calendar.getInstance().getTime();
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                if (Math.abs(currentValue-currentLightValue)<(0.01*currentLightValue))
                    return;
                fileName = "Light.csv";
                filePath = baseDir + File.separator+ fileName;
                currentLightValue = currentValue;
                f = new File(filePath);
                try {
                    FileWriter fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);

                    String light = Float.toString(currentValue);
                    intent.putExtra("light", light);
//                    System.out.println("Light value is " + currentValue + " for time: " + time);

                    String[] data = { light , Long.toString(time),date.toString()};
                    writer.writeNext(data, false);
                    writer.close();
//
                } catch (Exception e) {

                }

                break;
            case Sensor.TYPE_PROXIMITY:
                if (Math.abs(currentValue-currentProxValue)<(0.01*currentProxValue))
                    return;
                fileName = "Proximity.csv";
                filePath = baseDir + File.separator + fileName;
                currentProxValue = currentValue;
                f = new File(filePath);
                try {
                    FileWriter fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);
                    String prox= Float.toString(currentValue);
                    intent.putExtra("proximity", prox);
//                    System.out.println("Light value is " + currentValue + " for time: " + time);
                    String[] data = { prox, Long.toString(time),date.toString()};
                    writer.writeNext(data, false);
                    writer.close();
                } catch (Exception e) {

                }
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                if (Math.abs(currentValue-currentTempValue)<(0.01*currentTempValue))
                    return;
                fileName = "Temperature.csv";
                filePath = baseDir + File.separator + fileName;
                currentTempValue = currentValue;
                f = new File(filePath);
                try {
                    FileWriter fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);
                    String temp = Float.toString(currentValue);
                    intent.putExtra("temperature", temp);
//                    System.out.println("Light value is " + currentValue + " for time: " + time);
                    String[] data = { temp , Long.toString(time),date.toString()};
                    writer.writeNext(data, false);
                    writer.close();
                } catch (Exception e) {

                }
                //Pressure
            case Sensor.TYPE_PRESSURE:
                if (Math.abs(currentValue-currentPressValue)<(0.01*currentPressValue))
                    return;
                fileName = "Pressure.csv";
                filePath = baseDir + File.separator + fileName;
                currentPressValue = currentValue;
                f = new File(filePath);
                try {
                    FileWriter fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);
                    String press = Float.toString(currentValue);
                    intent.putExtra("pressure", press);
//                    System.out.println("Light value is " + currentValue + " for time: " + time);
                    String[] data = { press, Long.toString(time),date.toString()};
                    writer.writeNext(data, false);
                    writer.close();
                } catch (Exception e)
                {
                }
            default:
                WriteSensorValue(event);
                // do nothing
        }
        sendBroadcast(intent);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("MyService", "onStartCommand");
        for (int i = 0; i < sensorsList.size(); i++) {
        Sensor currentSensor=sensorsList.get(i);
        if(currentSensor!=null)
            mSensorManager.registerListener(this, currentSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorProximity != null) {
            mSensorManager.registerListener(this, mSensorProximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorTemperature != null) {
            mSensorManager.registerListener(this, mSensorTemperature, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorPressure != null) {
            mSensorManager.registerListener(this, mSensorPressure, SensorManager.SENSOR_DELAY_NORMAL);
        }
        return START_STICKY;
    }

    public void WriteSensorValue(SensorEvent event) {
        int sensorType = event.sensor.getType();
        String sensorName = event.sensor.getName();
        System.out.println("Name is"+sensorName);
        float prevValue = prev.get(sensorName);
        float currentValue = event.values[0];
        long time = event.timestamp;
        Date date = java.util.Calendar.getInstance().getTime();
        if (currentValue == prevValue)
            return;
        fileName = sensorName + ".csv";
        filePath = baseDir + File.separator + fileName;
        prev.put(sensorName, prevValue);
        f = new File(filePath);
        try {
            FileWriter fOut = new FileWriter(filePath, true);
            CSVWriter writer = new CSVWriter(fOut);
            String temp = Float.toString(currentValue);
            String[] data = {temp, Long.toString(time), date.toString()};
            writer.writeNext(data, false);
            writer.close();
        } catch (Exception e) {
        }

    }
}
