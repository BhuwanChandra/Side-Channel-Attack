package com.example.sidechannelattack;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class PatternView extends AppCompatActivity {

    private static String baseDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_view);

        PatternLockView patternLockView = findViewById(R.id.patternView);

        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List progressPattern) {

            }

            @Override
            public void onComplete(List pattern) {
                File file = new File(android.os.Environment.getExternalStorageDirectory()+"/SCA");
                if(!file.exists())
                    file.mkdir();
                baseDir = file.getAbsolutePath();
                Date date = java.util.Calendar.getInstance().getTime();

                String filePath = baseDir + File.separator + "Pattern.csv";

                File f = new File(filePath);
                FileWriter fOut = null;
                try {
                    fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);

                    Log.d(getClass().getName(), "Pattern complete: " + PatternLockUtils.patternToString(patternLockView, pattern));
                    if(PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase("12375")) {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        Toast.makeText(PatternView.this, "Correct Pattern!!", Toast.LENGTH_LONG).show();
                        String[] data = {"Correct", date.toString()};
                        writer.writeNext(data, false);
                    } else {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        Toast.makeText(PatternView.this, "Incorrect Pattern!!", Toast.LENGTH_LONG).show();
                        String[] data = {"Incorrect", date.toString()};
                        writer.writeNext(data, false);
                    }

                    writer.close();
                    new android.os.Handler().postDelayed( new Runnable() {
                        public void run() {
                            patternLockView.clearPattern();
                        }
                    }, 2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCleared() {

            }
        });
    }
}
