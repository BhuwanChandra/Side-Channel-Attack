package com.example.sidechannelattack;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class PatternView extends AppCompatActivity {
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
                Log.d(getClass().getName(), "Pattern complete: " + PatternLockUtils.patternToString(patternLockView, pattern));
                if(PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase("12375")) {
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    Toast.makeText(PatternView.this, "Correct Pattern!!", Toast.LENGTH_LONG).show();
                } else {
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(PatternView.this, "Incorrect Pattern!!", Toast.LENGTH_LONG).show();
                }

                new android.os.Handler().postDelayed( new Runnable() {
                    public void run() {
                        patternLockView.clearPattern();
                    }
                }, 2000);
            }

            @Override
            public void onCleared() {

            }
        });
    }
}
