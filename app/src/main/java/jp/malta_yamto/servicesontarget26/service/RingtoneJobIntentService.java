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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RingtoneJobIntentService extends JobIntentService {
    @SuppressWarnings("unused")
    private static final String TAG = "RingtoneJobIntent";

    static final int JOB_ID = 29298989;

    public static final String SERVICE_NAME = "RINGTONE_JOB_INTENT_SERVICE";
    public static final String REQUEST_TOAST = SERVICE_NAME + ".REQUEST_TOAST";
    public static final String REQUEST_RINGTONE = SERVICE_NAME + ".REQUEST_RINGTONE";

    public static void enqueueWork(Context context, Intent work) {
        Log.d(TAG, "enqueueWork: work.getAction() = " + work.getAction());
        enqueueWork(context, RingtoneJobIntentService.class, JOB_ID, work);
    }

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.d(TAG, "onHandleWork: intent.getAction() = " + intent.getAction());
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {

            case REQUEST_TOAST: {
                doToast();
            }
            break;

            case REQUEST_RINGTONE: {
                doRingTone();
            }
            break;

        }
    }

    private void doToast() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "doToast: start");
                Toast.makeText(RingtoneJobIntentService.this, "I'm RingtoneJobIntentService!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void doRingTone() {
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
    }

}
