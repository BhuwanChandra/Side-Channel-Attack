package com.example.sidechannelattack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import java.util.*;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.MenuItem;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

/**
 * SensorListeners demonstrates how to gain access to sensors (here, the light
 * and proximity sensors), how to register sensor listeners, and how to
 * handle sensor events.
 */

public class SensorGraph extends AppCompatActivity
        implements SensorEventListener {

    // System sensor manager instance.
    private SensorManager mSensorManager;

    // Proximity and light sensors, as retrieved from the sensor manager.
    private Sensor mSensorProximity;

    Cartesian cartesian;
    AnyChartView anyChartView;
    Line series1;
    List<DataEntry> seriesData = new ArrayList<>();
    Set set;
    int time = 10;
    float previousValue = 0;
    String sensorName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_graph);

        Intent intent = getIntent();
        int sensorValue = intent.getIntExtra("currentSensor", 0);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensorProximity = mSensorManager.getDefaultSensor(sensorValue);

        sensorName = mSensorProximity.getName();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(sensorName);

        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar));

        cartesian = AnyChart.line();
        cartesian.padding(8d, 8d, 8d, 8d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Sensor Value Change Graph");

        cartesian.yAxis(0).title("Sensor value");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        for (int i = 0; i < 70; i++) {
            seriesData.add(new CustomDataEntry(++time + "", 0));
        }

        updateChart();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorProximity != null) {
            mSensorManager.registerListener(this, mSensorProximity,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    private void updateChart() {
        set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        series1 = cartesian.line(series1Mapping);
        series1.name(sensorName);
//        series1.hovered().markers().enabled(true);
//        series1.hovered().markers()
//                .type(MarkerType.CIRCLE)
//                .size(4d);
//        series1.tooltip()
//                .position("right")
//                .anchor(Anchor.LEFT_CENTER)
//                .offsetX(5d)
//                .offsetY(5d);

        cartesian.autoRedraw(true);
        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);
        if (anyChartView != null) anyChartView.setChart(cartesian);
    }


    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value) {
            super(x, value);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        int sensorType = sensorEvent.sensor.getType();

        float currentValue = sensorEvent.values[0];

        if (previousValue != currentValue) {
            String currentTime = ++time + "";
            seriesData.add(new CustomDataEntry(currentTime, currentValue));
            if (seriesData.size() > 70) seriesData.remove(0);
            set.data(seriesData);
            cartesian.removeAllSeries();
            series1 = cartesian.line(set.mapAs("{ x: 'x', value: 'value' }"));
            series1.name(sensorName + " value: " + currentValue);
            previousValue = currentValue;
            System.out.println("currentValue " + currentValue + "  " + currentTime);
        }
    }

    /**
     * Abstract method in SensorEventListener.  It must be implemented, but is
     * unused in this app.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}