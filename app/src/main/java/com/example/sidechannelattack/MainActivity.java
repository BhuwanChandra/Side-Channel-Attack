package com.example.sidechannelattack;



import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity
        extends Activity {


    // TextViews to display current sensor values
    private TextView mTextSensorLight;
    private TextView mTextSensorProximity;
    Intent i;
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    String sensor_error = getResources().getString(R.string.error_no_sensor);
                    String mSensorLight = intent.getStringExtra("light");
                    String mSensorProximity = intent.getStringExtra("proximity");
//                    System.out.println(mSensorProximity);
                    if (mSensorLight == null) {
                        mTextSensorLight.setText(sensor_error);
                    }
                    else{
                        Float l = Float.valueOf(mSensorLight).floatValue();
                        mTextSensorLight.setText(getResources().getString(
                                R.string.label_light, l));
                    }
                    if (mSensorProximity == null) {
                        mTextSensorProximity.setText(sensor_error);
                    }
                    else{
                        Float p = Float.valueOf(mSensorProximity).floatValue();
                        mTextSensorProximity.setText(getResources().getString(
                                R.string.label_proximity, p));
                    }
                }
            };
//    @Override
//    public void onResume(){
//        super.onResume();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(MyService.MY_ACTION);
//        startService(i);
//        registerReceiver(myBroadcastReceiver, intentFilter);
//
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextSensorLight = (TextView) findViewById(R.id.label_light);
        mTextSensorProximity = (TextView) findViewById(R.id.label_proximity);
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
//            ActivityCompat.requestPermissions(MainActivity.this, permission, 0);
//        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            i = new Intent(MainActivity.this, MyService.class);
            startService(i);
            registerReceiver(myBroadcastReceiver, new IntentFilter(MyService.MY_ACTION));

        } else {
            // You can directly ask for the permission.
            String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(MainActivity.this, permission, 0);
        }
        i = new Intent(MainActivity.this, MyService.class);
        startService(i);
        registerReceiver(myBroadcastReceiver, new IntentFilter(MyService.MY_ACTION));

//    public void onPause(){
//        super.onPause();
//        stopService(i);
//        if (myBroadcastReceiver != null)unregisterReceiver(myBroadcastReceiver);
//    }


    }
}
