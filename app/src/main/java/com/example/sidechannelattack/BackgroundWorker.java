package com.example.sidechannelattack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BackgroundWorker extends Service {

    boolean writeLock = false;
    String CHANNEL_ID = "SCANotification";
    private ArrayList<Reading> data;
    private Thread worker;
    private static long startTime = 0;
    private static ConcurrentHashMap<String, Float> prevValues;

    public BackgroundWorker() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Android System")
                .setContentText("Sensor data is being collected..")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        data = new ArrayList<>();
        final List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        prevValues = new ConcurrentHashMap<>();
        for(int i = 0; i < deviceSensors.size(); i++) {
            Sensor s = deviceSensors.get(i);
            prevValues.put(s.getName(), 0.000f);
        }

        if(startTime == 0) startTime = System.currentTimeMillis();
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float currentValue = event.values[0];
                String sensorName = event.sensor.getName();
                String lowerName = sensorName.toLowerCase();
                if(lowerName.contains("acceleration") || lowerName.contains("gyroscope") || lowerName.contains("accelerometer")) {
                    String[] dirs = { "-x", "-y", "-z" };
                    double mod = 0.0;
                    for(int i = 0; i < 3; i++) {
                        mod += Math.pow(event.values[i], 2);
                        Reading reading = new Reading(Calendar.getInstance().getTime().toString(), System.nanoTime(), sensorName + dirs[i], event.values[i]);
                        data.add(reading);
                    }
                    currentValue = (float) Math.sqrt(mod);
                }

                Reading reading = new Reading(Calendar.getInstance().getTime().toString(), System.nanoTime(), sensorName, currentValue);
                data.add(reading);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        for (Sensor S : deviceSensors) {
            sensorManager.registerListener(listener, S, SensorManager.SENSOR_DELAY_NORMAL);
        }

        worker = new Thread(() -> {
            while (true) {
                writeLock = true;
                writeFile();
                writeLock = false;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        worker.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        worker.interrupt();
    }

    public void writeFile() {
        try {
            File baseDir = new File(android.os.Environment.getExternalStorageDirectory()+"/SCA");
            if(!baseDir.exists()) {
                boolean dirCreated = baseDir.mkdirs();
                System.out.println("Directory created!!: " + dirCreated);
            }

            HashMap<String, ArrayList<String>> currentData = new HashMap<>();
            ArrayList<Reading> collectedData = new ArrayList<>(data);
            data.clear();
            System.out.println("Data Remaining: " + data.size());

            for (Iterator<Reading> iterator = collectedData.iterator(); iterator.hasNext();) {
                Reading r = iterator.next();
                String line = r.value + "," + r.date + "," + r.systemNanoSec + "," + r.timeDiff + "\n";
                if(prevValues.containsKey(r.sensorName) && Objects.equals(prevValues.get(r.sensorName), r.value)) continue;
                else prevValues.put(r.sensorName, r.value);
                if(currentData.containsKey(r.sensorName)) {
                    Objects.requireNonNull(currentData.get(r.sensorName)).add(line);
                } else {
                    ArrayList<String> arr = new ArrayList<>();
                    arr.add(line);
                    currentData.put(r.sensorName, arr);
                }
                iterator.remove();
            }

            for (java.util.Map.Entry<String, ArrayList<String>> mapElement : currentData.entrySet()) {
                String key = mapElement.getKey();
                List<String> sensorData = currentData.get(key);
                assert sensorData != null;
                String str = sensorData.stream().collect(Collectors.joining(""));
                System.out.println("SensorName ===>>>>>" + key.replaceAll(" ", "_"));
                if(sensorData.size() > 0) {
                    String fileName = key.replaceAll(" ", "_") + ".csv";
                    String filePath = baseDir.getAbsolutePath() + File.separator + fileName;
                    File f = new File(filePath);
                    FileWriter mFileWriter;
                    if (f.exists() && !f.isDirectory()) {
                        mFileWriter = new FileWriter(filePath, true);
                    } else {
                        mFileWriter = (new FileWriter(filePath));
                        mFileWriter.write("Value,Time,SystemNanoSec,timeDiff\n");
                    }
                    mFileWriter.write(str);
                    mFileWriter.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    static class Reading {
        String date;
        long systemNanoSec;
        String sensorName;
        long timeDiff;
        float value;

        public Reading(String date, long systemNanoSec, String sensorName, float value) {
            this.date = date;
            this.systemNanoSec = systemNanoSec;
            this.sensorName = sensorName;
            this.timeDiff = System.currentTimeMillis() - startTime;
            this.value = value;
        }
    }
}
