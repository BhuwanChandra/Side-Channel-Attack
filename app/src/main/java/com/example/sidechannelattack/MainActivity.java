package com.example.sidechannelattack;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    Intent i;
    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    public void openPatternView(View view) {
        Intent intent = new Intent(this, PatternView.class);
        startActivity(intent);
    }

    public void openPasswordView(View view) {
        Intent intent = new Intent(this, PasswordView.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager())) {
            i = new Intent(MainActivity.this, MyService.class);
            startService(i);
            registerReceiver(myBroadcastReceiver, new IntentFilter(MyService.MY_ACTION));
        } else
            {
                if(Build.VERSION.SDK_INT >= 30) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    // You can directly ask for the permission.
                    String[] permission = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
                    ActivityCompat.requestPermissions(MainActivity.this, permission, 0);
                }
        }
        i = new Intent(MainActivity.this, MyService.class);
//        new Runnable() {
//            @Override
//            public void run() {
//                startService(i);
//            }
//        };
//        startService(i);
//        startService(i);
        registerReceiver(myBroadcastReceiver, new IntentFilter(MyService.MY_ACTION));
    }
}