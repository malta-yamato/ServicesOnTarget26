/*
 * Copyright (C) 2017 MALTA-YAMATO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.malta_yamto.servicesontarget26;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jp.malta_yamto.servicesontarget26.service.ForegroundDummyService;
import jp.malta_yamto.servicesontarget26.service.RingtoneService;

public class AlarmManagerDemo extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "AlarmManagerDemo";

    public static final String PREFS_KEY_ALARM_TRIG = "prefs_key_alarm_trig";
    public static final int ALARM_TRIG_UNKNOWN = 0;
    public static final int ALARM_TRIG_START = 1;
    public static final int ALARM_TRIG_STOP = 2;

    private Handler mHandler;
    private SharedPreferences mSharedPreferences;

    private EditText mIntervalEdit;
    private Button mStartAlarmButton;
    private Button mStopAlarmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mSharedPreferences = AppFiles.getSharedPreferences(this);

        setContentView(R.layout.activity_alarm_manager_demo_main);
        mIntervalEdit = findViewById(R.id.edit_interval);
        mStartAlarmButton = findViewById(R.id.button_start_alarm);
        mStopAlarmButton = findViewById(R.id.button_stop_alarm);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();

        int alarmTrig = ALARM_TRIG_UNKNOWN;
        try {
            alarmTrig = mSharedPreferences.getInt(PREFS_KEY_ALARM_TRIG, ALARM_TRIG_UNKNOWN);
        } catch (Exception e) {
            mSharedPreferences.edit().putInt(PREFS_KEY_ALARM_TRIG, ALARM_TRIG_UNKNOWN).commit();
        }

        if (alarmTrig == ALARM_TRIG_UNKNOWN) {
            mStartAlarmButton.setEnabled(true);
            mStopAlarmButton.setEnabled(false);
        } else if (alarmTrig == ALARM_TRIG_START) {
            mStartAlarmButton.setEnabled(false);
            mStopAlarmButton.setEnabled(true);
        } else {
            mStartAlarmButton.setEnabled(true);
            mStopAlarmButton.setEnabled(false);
        }

    }

    //
    // alarm setting
    //
    private void setAlarmInternal() {
        Log.d(TAG, "setAlarmInternal: ");
        long alarmTrig = mSharedPreferences.getInt(PREFS_KEY_ALARM_TRIG, ALARM_TRIG_UNKNOWN);
        if (alarmTrig == ALARM_TRIG_START) {
            startAlarmInternal();
        } else if (alarmTrig == ALARM_TRIG_STOP) {
            stopAlarmInternal();
        }
    }

    private void startAlarmInternal() {
        Log.d(TAG, "startAlarmInternal: start");
        long interval;
        try {
            interval = Long.parseLong(mIntervalEdit.getText().toString()) * 1000L;
        } catch (Exception e) {
            interval = 60 * 1000L;
        }
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + interval, interval, getPendingIntent());
        mStartAlarmButton.setEnabled(false);
        mStopAlarmButton.setEnabled(true);
    }

    private void stopAlarmInternal() {
        Log.d(TAG, "stopAlarmInternal: start");
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(getPendingIntent());
        mStartAlarmButton.setEnabled(true);
        mStopAlarmButton.setEnabled(false);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, RingtoneService.class);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    private void scheduleSetAlarm() {
        Log.d(TAG, "scheduleSetAlarm: ");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setAlarmInternal();
            }
        });
    }

    //
    // Button Listener
    //

    @SuppressLint("ApplySharedPref")
    public void onStartAlarmClick(View view) {
        Log.d(TAG, "onStartAlarmClick: ");
        mSharedPreferences.edit().putInt(PREFS_KEY_ALARM_TRIG, ALARM_TRIG_START).commit();
        scheduleSetAlarm();
    }

    @SuppressLint("ApplySharedPref")
    public void onStopAlarmClick(View view) {
        Log.d(TAG, "onStopAlarmClick: start");
        mSharedPreferences.edit().putInt(PREFS_KEY_ALARM_TRIG, ALARM_TRIG_STOP).commit();
        scheduleSetAlarm();
    }

    public void onShowForegroundClick(View view) {
        Intent serviceIntent = new Intent(this, ForegroundDummyService.class);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(serviceIntent);
        } else {
            startForegroundService(serviceIntent);
        }
    }

    public void onDismissForegroundClick(View view) {
        Intent serviceIntent = new Intent(this, ForegroundDummyService.class);
        stopService(serviceIntent);
    }

}
