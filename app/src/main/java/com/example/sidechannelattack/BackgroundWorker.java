package com.example.sidechannelattack;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BackgroundWorker extends Service {

    boolean writeLock = false;
    String CHANNEL_ID = "SCANotification";
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private ArrayList<Reading> data;
    private Thread worker;
    private static long startTime = 0;
    private static ConcurrentHashMap<String, Float> prevValues;

    public BackgroundWorker() {
        super();
    }

    public BackgroundWorker(String name) {
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


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        data = new ArrayList<>();
        final List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        prevValues = new ConcurrentHashMap<>();
        for(int i = 0; i < deviceSensors.size(); i++) {
            Sensor s = deviceSensors.get(i);
            prevValues.put(s.getName(), 0.000f);
        }

        if(startTime == 0) startTime = System.currentTimeMillis();
        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
//                for (int i = 0; i < event.values.length; i++) {
//                    if (!writeLock) {
//                        Reading reading = new Reading(Calendar.getInstance().getTime().toString(), System.nanoTime(), event.sensor.getName(), event.values[i]);
//                        data.add(reading);
//                    }
//                }

                Reading reading = new Reading(Calendar.getInstance().getTime().toString(), System.nanoTime(), event.sensor.getName(), event.values[0]);
                data.add(reading);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        for (Sensor S : deviceSensors) {
            sensorManager.registerListener(listener, S, SensorManager.SENSOR_DELAY_NORMAL);
        }

        worker = new Thread(new Runnable() {
            @Override
            public void run() {
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
            ArrayList<Reading> collectedData = (ArrayList<Reading>) data.clone();
            data.clear();
            System.out.println("Data Remaining: " + data.size());

            for (Iterator<Reading> iterator = collectedData.iterator(); iterator.hasNext();) {
                Reading r = iterator.next();
                String line = r.value + "," + r.date + "," + r.systemNanoSec + "," + r.timeDiff + "\n";
                if(prevValues.get(r.sensorName) == r.value) continue;
                else prevValues.put(r.sensorName, r.value);
                if(currentData.containsKey(r.sensorName)) {
                    currentData.get(r.sensorName).add(line);
                } else {
                    ArrayList<String> arr = new ArrayList<>();
                    arr.add(line);
                    currentData.put(r.sensorName, arr);
                }
                iterator.remove();
            }

            for (HashMap.Entry mapElement : currentData.entrySet()) {
                String key = (String)mapElement.getKey();
                List<String> sensorData = currentData.get(key);
                String str = sensorData.stream().collect(Collectors.joining(""));
                System.out.println("Sensor: " + key.replaceAll(" ", ""));
                if(sensorData.size() > 0) {
                    String fileName = key.replaceAll(" ", "") + ".csv";
                    String filePath = baseDir.getAbsolutePath() + File.separator + fileName;
                    File f = new File(filePath);
                    FileWriter mFileWriter;
                    System.out.println(filePath);
                    // File exist
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

    class Reading {
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
