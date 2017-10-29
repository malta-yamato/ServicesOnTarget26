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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Trigger;

import jp.malta_yamto.servicesontarget26.service.ForegroundDummyService;
import jp.malta_yamto.servicesontarget26.service.RingtoneJobDispatcherService;

public class JobDispatcherDemo extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "JobDispatcherDemo";

    public static final String PREFS_KEY_JOB_TRIG = "prefs_key_job_trig";
    public static final int JOB_TRIG_UNKNOWN = 0;
    public static final int JOB_TRIG_START = 1;
    public static final int JOB_TRIG_STOP = 2;

    public static final String JOB_UNIQUE_TAG = "omaenoka-chandebeso";

    private Handler mHandler;
    private SharedPreferences mSharedPreferences;

    private FirebaseJobDispatcher mJobDispatcher;

    private EditText mIntervalEdit;
    private Button mStartJobButton;
    private Button mStopJobButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        mSharedPreferences = AppFiles.getSharedPreferences(this);

        setContentView(R.layout.activity_job_dispatcher_demo_main);
        mIntervalEdit = findViewById(R.id.edit_interval);
        mStartJobButton = findViewById(R.id.button_start_job);
        mStopJobButton = findViewById(R.id.button_stop_job);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();

        int jobTrig = JOB_TRIG_UNKNOWN;
        try {
            jobTrig = mSharedPreferences.getInt(PREFS_KEY_JOB_TRIG, JOB_TRIG_UNKNOWN);
        } catch (Exception e) {
            mSharedPreferences.edit().putInt(PREFS_KEY_JOB_TRIG, JOB_TRIG_UNKNOWN).commit();
        }

        if (jobTrig == JOB_TRIG_UNKNOWN) {
            mStartJobButton.setEnabled(true);
            mStopJobButton.setEnabled(false);
        } else if (jobTrig == JOB_TRIG_START) {
            mStartJobButton.setEnabled(false);
            mStopJobButton.setEnabled(true);
        } else {
            mStartJobButton.setEnabled(true);
            mStopJobButton.setEnabled(false);
        }
    }

    //
    // job setting
    //
    private void setJobInternal() {
        Log.d(TAG, "setJobInternal: ");
        int jobTrig = mSharedPreferences.getInt(PREFS_KEY_JOB_TRIG, JOB_TRIG_UNKNOWN);
        if (jobTrig == JOB_TRIG_START) {
            startJobInternal();
        } else if (jobTrig == JOB_TRIG_STOP) {
            stopJobInternal();
        }
    }

    private void startJobInternal() {
        Log.d(TAG, "startAlarmInternal: start");

        int[] window = getJobWindow();
        Job job = getJobDispatcher().newJobBuilder().setService(
                RingtoneJobDispatcherService.class) // the JobService that will be called
                .setTag(JOB_UNIQUE_TAG)        // uniquely identifies the job
                .setRecurring(true).setTrigger(Trigger.executionWindow(window[0], window[1]))
                .build();

        getJobDispatcher().mustSchedule(job);
        mStartJobButton.setEnabled(false);
        mStopJobButton.setEnabled(true);
    }

    private void stopJobInternal() {
        Log.d(TAG, "stopJobInternal: start");
        getJobDispatcher().cancel(JOB_UNIQUE_TAG);
        mStartJobButton.setEnabled(true);
        mStopJobButton.setEnabled(false);
    }

    @SuppressWarnings("unused")
    private void scheduleSetJob() {
        Log.d(TAG, "scheduleSetJob: ");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setJobInternal();
            }
        });
    }

    @NonNull
    private FirebaseJobDispatcher getJobDispatcher() {
        if (mJobDispatcher == null) {
            mJobDispatcher =
                    new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));
        }
        return mJobDispatcher;
    }

    private int[] getJobWindow() {
        int interval;
        try {
            interval = Integer.parseInt(mIntervalEdit.getText().toString());
        } catch (Exception e) {
            interval = 60;
        }

        int windowHalfLength = interval / 10;
        return new int[]{interval - windowHalfLength, interval + windowHalfLength};
    }

    //
    // Button Listener
    //

    @SuppressLint("ApplySharedPref")
    public void onStartJobClick(View view) {
        Log.d(TAG, "onStartJobClick: ");
        if (mSharedPreferences.edit().putInt(PREFS_KEY_JOB_TRIG, JOB_TRIG_START).commit()) {
            startJobInternal();
        }
    }

    @SuppressLint("ApplySharedPref")
    public void onStopJobClick(View view) {
        Log.d(TAG, "onStopJobClick: start");
        if (mSharedPreferences.edit().putInt(PREFS_KEY_JOB_TRIG, JOB_TRIG_STOP).commit()) {
            stopJobInternal();
        }
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
