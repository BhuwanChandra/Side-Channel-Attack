package com.example.sidechannelattack;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

public class PasswordView extends AppCompatActivity {

    PinLockView mPinLockView;
    IndicatorDots mIndicatorDots;
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
                Log.d(getClass().getName(), "Pin complete: " + pin);
                if(pin.equalsIgnoreCase("135790")) {
                    Toast.makeText(PasswordView.this, "Correct Password!!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PasswordView.this, "Incorrect Password!!", Toast.LENGTH_LONG).show();
                }
                new android.os.Handler().postDelayed( new Runnable() {
                    public void run() {
                        mPinLockView.resetPinLockView();
                    }
                }, 2000);
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