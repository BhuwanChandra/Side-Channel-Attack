package com.example.sidechannelattack;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;

public class PasswordView extends AppCompatActivity {

    PinLockView mPinLockView;
    IndicatorDots mIndicatorDots;

    private static String baseDir;

    public static long date = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_view);
        mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);
        mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mIndicatorDots.setPinLength(6);
        mPinLockView.setPinLength(6);
        mIndicatorDots.setIndicatorType(IndicatorDots.IndicatorType.FILL_WITH_ANIMATION);
        mPinLockView.setPinLockListener(new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                File file = new File(android.os.Environment.getExternalStorageDirectory()+"/SCA");
                if(!file.exists())
                    file.mkdir();
                baseDir = file.getAbsolutePath();
                long milliTime = System.currentTimeMillis();

                String filePath = baseDir + File.separator + "Password.csv";
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
                    Log.d(getClass().getName(), "Pin complete: " + pin);
                    if(pin.equalsIgnoreCase("135790")) {
                        Toast.makeText(PasswordView.this, "Correct Password!!", Toast.LENGTH_LONG).show();
                        data[0] = "Correct";
                    } else {
                        Toast.makeText(PasswordView.this, "Incorrect Password!!", Toast.LENGTH_LONG).show();
                        data[0] = "Incorrect";
                    }

                    writer.writeNext(data, false);
                    writer.close();
                    new android.os.Handler().postDelayed(() -> mPinLockView.resetPinLockView(), 2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onEmpty() {
                Log.d(getClass().getName(), "Pin empty");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                Log.d(getClass().getName(), "Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            }
        });
    }
}