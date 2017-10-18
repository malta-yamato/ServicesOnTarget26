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

import android.app.IntentService;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class RingtoneService extends IntentService {
    @SuppressWarnings("unused")
    private static final String TAG = "RingtoneService";

    private static final String MY_NAME = "RingtoneIntentService";
    private static final String MY_WAKE_LOCK = MY_NAME + "WakeLock";

    private PowerManager.WakeLock mWakeLock;

    public RingtoneService() {
        super(MY_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MY_WAKE_LOCK);
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire(5 * 60 * 1000L);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: start");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: start");
                if (mWakeLock.isHeld()) {
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
        }).run();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        mWakeLock = null;
    }

}
