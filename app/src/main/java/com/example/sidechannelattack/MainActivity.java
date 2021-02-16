package com.example.sidechannelattack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.*;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    // System sensor manager instance.
    private SensorManager mSensorManager;

    // Proximity and light sensors, as retrieved from the sensor manager.
    private Sensor mSensorProximity;
    private Sensor mSensorLight;

    List<Integer> sensorsList = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get an instance of the sensor manager.
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);

        List<Sensor> availableSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

//        sensorsList.add(Sensor.TYPE_GYROSCOPE);
//        sensorsList.add(Sensor.TYPE_PROXIMITY);
//        sensorsList.add(Sensor.TYPE_AMBIENT_TEMPERATURE);
//        sensorsList.add(Sensor.TYPE_LIGHT);
//        sensorsList.add(Sensor.TYPE_PRESSURE);
//        sensorsList.add(Sensor.TYPE_MAGNETIC_FIELD);

        LinearLayout layout = (LinearLayout) findViewById(R.id.scrollLayout);

        for (int i = 0; i < availableSensors.size(); i++) {
            Sensor currentSensor = availableSensors.get(i);
            String sensorName = currentSensor.getName();
            sensorsList.add(i, currentSensor.getType());
            final Button button = new Button(this);
            button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            button.setText(sensorName);
            button.setTextSize(18);
            button.setWidth(1200);
            button.setId(i + i*2);
            final int finalI = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNewActivity(finalI);
                }
            });
            layout.addView(button);
        }

        mSensorProximity = mSensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);
        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    public void openNewActivity(int i){
        Intent intent = new Intent(this, SensorGraph.class);
        Integer thisSensor = sensorsList.get(i);
        intent.putExtra("currentSensor", thisSensor);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorProximity != null) {
            mSensorManager.registerListener(this, mSensorProximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
