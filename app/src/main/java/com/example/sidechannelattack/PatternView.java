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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PatternView extends AppCompatActivity {

    private static String baseDir;

    public static long date = System.currentTimeMillis();

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
                long milliTime = System.currentTimeMillis();

                String filePath = baseDir + File.separator + "Pattern.csv";
                long nanoTime = System.nanoTime();
                Date currDate = Calendar.getInstance().getTime();

                File f = new File(filePath);
                FileWriter fOut;
                try {
                    fOut = new FileWriter(filePath, true);
                    CSVWriter writer = new CSVWriter(fOut);

                    if(f.length() == 0) {
                        String[] heads = {"Status", "timeMillis", "timeNanos", "time-diff", "Date"};
                        writer.writeNext(heads, false);
                    }
                    String[] data = {"", milliTime + "", nanoTime + "", (milliTime - date) + "", currDate.toString()};
                    Log.d(getClass().getName(), "Pattern complete: " + PatternLockUtils.patternToString(patternLockView, pattern));
                    if (PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase("12375")) {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        Toast.makeText(PatternView.this, "Correct Pattern!!", Toast.LENGTH_LONG).show();
                        data[0] = "Correct";
                    } else {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        Toast.makeText(PatternView.this, "Incorrect Pattern!!", Toast.LENGTH_LONG).show();
                        data[0] = "Incorrect";
                    }

                    writer.writeNext(data, false);
                    writer.close();
                    new android.os.Handler().postDelayed(() -> patternLockView.clearPattern(), 2000);
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
