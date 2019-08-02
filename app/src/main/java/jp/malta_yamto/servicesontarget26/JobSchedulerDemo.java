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

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jp.malta_yamto.servicesontarget26.service.ForegroundDummyService;
import jp.malta_yamto.servicesontarget26.service.RingtoneJobService;

public class JobSchedulerDemo extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "JobSchedulerDemo";

    public static final int JOB_ID = 3939;

    private EditText mIntervalEdit;
    private Button mStartJobButton;
    private Button mStopJobButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_job_scheduler_demo_main);
        mIntervalEdit = findViewById(R.id.edit_interval);
        mStartJobButton = findViewById(R.id.button_start_job);
        mStopJobButton = findViewById(R.id.button_stop_job);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mIntervalEdit.setEnabled(false);
            mStartJobButton.setEnabled(false);
            mStopJobButton.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int size = getJobScheduler().getAllPendingJobs().size();
            if (size > 0) {
                mStartJobButton.setEnabled(false);
                mStopJobButton.setEnabled(true);
            } else {
                mStartJobButton.setEnabled(true);
                mStopJobButton.setEnabled(false);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    private JobScheduler getJobScheduler() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            throw new IllegalStateException("cannot get JOB_SCHEDULER_SERVICE");
        }
        return jobScheduler;
    }

    //
    // Button Listener
    //

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onStartJobClick(View view) {
        Log.d(TAG, "onStartJobClick: ");
        ComponentName componentName = new ComponentName(this, RingtoneJobService.class);
        JobInfo jobInfo =
                new JobInfo.Builder(JOB_ID, componentName).setPeriodic(getJobInterval()).build();
        getJobScheduler().schedule(jobInfo);
        mStartJobButton.setEnabled(false);
        mStopJobButton.setEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            long minPeriod = JobInfo.getMinPeriodMillis();
            Log.d(TAG, "onStartJobClick: minPeriod = " + minPeriod);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onStopJobClick(View view) {
        Log.d(TAG, "onStopJobClick: start");
        getJobScheduler().cancel(JOB_ID);
        mStartJobButton.setEnabled(true);
        mStopJobButton.setEnabled(false);
    }

    private long getJobInterval() {
        try {
            return Long.parseLong(mIntervalEdit.getText().toString()) * 1000L;
        } catch (Exception e) {
            return 900 * 1000L;
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
