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

package jp.malta_yamto.servicesontarget26.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RingtoneJobService extends JobService {
    @SuppressWarnings("unused")
    private static final String TAG = "RingtoneJobService";

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.d(TAG, "onStartJob: ");

        new Thread(new Runnable() {
            @Override
            public void run() {
                jobFinished(doJob(params), false); // false means not to need rescheduling.
            }
        }).start();

        return true; // true means that there are tasks of different threads, so JOB is not stopped yet. Call jobFinished in task thread to tell the manager that it has completed.
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: start");
        return false; // false means not to retry the interrupted JOB.
    }

    private JobParameters doJob(JobParameters params) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);

        Log.d(TAG, "run: ringtone play");
        ringtone.play();
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "run: ringtone stop");
        ringtone.stop();

        return params;
    }

}
