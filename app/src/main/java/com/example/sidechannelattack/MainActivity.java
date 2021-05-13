package com.example.sidechannelattack;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    Intent i;

    public void openPatternView(View view) {
        Intent intent = new Intent(this, PatternView.class);
        startActivity(intent);
    }

    public void openPasswordView(View view) {
        Intent intent = new Intent(this, PasswordView.class);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent serviceIntent = new Intent(this, BackgroundWorker.class);
        stopService(serviceIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager())) {
            Intent serviceIntent = new Intent(this, BackgroundWorker.class);
            System.out.println("SERVICE STARTED!!!");
            ContextCompat.startForegroundService(this, serviceIntent);
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

                Intent serviceIntent = new Intent(this, BackgroundWorker.class);
                System.out.println("SERVICE STARTED!!!");
                ContextCompat.startForegroundService(this, serviceIntent);
        }
    }
}