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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.malta_yamto.servicesontarget26.AppFiles;
import jp.malta_yamto.servicesontarget26.NotificationHelper;
import jp.malta_yamto.servicesontarget26.aidl.ITimerService;
import jp.malta_yamto.servicesontarget26.aidl.ITimerServiceCallback;

public class TimerService extends Service {
    private static final String TAG = "Service_A";

    public static final long INVALID_TIME = -1L;

    public static final String PREF_KEY_TIME_ON_CREATE = "pref_key_time_on_create";
    public static final String PREF_KEY_TIME_ON_DESTROY = "pref_key_time_on_destroy";
    public static final String PREF_KEY_TIMER_EXPERIENCE = "pref_key_timer_experience";

    public static final long TIMER_DELAY_MILLISEC = 1000L;
    public static final long TIMER_PERIOD_MILLISEC = 1000L;

    private NotificationHelper mNotificationHelper;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: start");
        super.onCreate();

        // notification helper
        mNotificationHelper = new NotificationHelper(this);

        // check previous termination time
        SharedPreferences prefs = AppFiles.getSharedPreferences(this);
        long timeOnDestroy = prefs.getLong(PREF_KEY_TIME_ON_DESTROY, INVALID_TIME);
        if (timeOnDestroy == INVALID_TIME) {
            Toast.makeText(this, "previous service might have been killed abnormally.",
                    Toast.LENGTH_LONG).show();
        }

        // write current time in preferences.
        SharedPreferences.Editor editor = AppFiles.getSharedPreferences(this, getClass()).edit();
        editor.putLong(PREF_KEY_TIME_ON_CREATE, System.currentTimeMillis());
        editor.putLong(PREF_KEY_TIME_ON_DESTROY, -1L);
        editor.putInt(PREF_KEY_TIMER_EXPERIENCE, 0);
        editor.apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: flags, startId = " + flags + ", " + startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: start");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: start");
        synchronized (this) {
            mCallback = null;
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: start");
        super.onDestroy();

        // release resources ...
        stopTimerService();

        // write current time in preferences.
        SharedPreferences.Editor editor = AppFiles.getSharedPreferences(this, getClass()).edit();
        editor.putLong(PREF_KEY_TIME_ON_DESTROY, System.currentTimeMillis());
        editor.apply();

        // last results
        SharedPreferences prefs = AppFiles.getSharedPreferences(this);
        long timeOnCreate = prefs.getLong(PREF_KEY_TIME_ON_CREATE, INVALID_TIME);
        long timeOnDestroy = prefs.getLong(PREF_KEY_TIME_ON_DESTROY, INVALID_TIME);
        int timerExperience = prefs.getInt(PREF_KEY_TIMER_EXPERIENCE, 0);

        String descTimeOnCreate = "unknown";
        if (timeOnCreate != INVALID_TIME) {
            descTimeOnCreate = "Service start: " +
                    SimpleDateFormat.getDateTimeInstance().format(new Date(timeOnCreate));
        }
        String descTimeOnDestroy = "unknown";
        if (timeOnDestroy != INVALID_TIME) {
            descTimeOnDestroy = "Service end: " +
                    SimpleDateFormat.getDateTimeInstance().format(new Date(timeOnDestroy));
        }
        String descTimerExperience = "Timer Experience: " + String.valueOf(timerExperience);

        // last notification
        String[] lines = new String[]{descTimeOnCreate, descTimeOnDestroy, descTimerExperience};
        NotificationCompat.Builder builder =
                mNotificationHelper.getNotification("Timer Service results", "Summary", lines);
        mNotificationHelper.notify(100, builder);
    }

    //
    // Timer
    // only aimed at a experiment for service.

    private CountUpTimer mTimer;
    private int mLastValue = 0;

    private synchronized void startTimerService() {
        Log.d(TAG, "startTimerService: start");
        if (mTimer == null) {
            mTimer = new CountUpTimer(mLastValue);
            mTimer.setCallback(mTimerCallback);
            mTimer.startTimer(TIMER_DELAY_MILLISEC, TIMER_PERIOD_MILLISEC);
        }
    }

    private synchronized void stopTimerService() {
        Log.d(TAG, "stopTimerService: start");
        if (mTimer != null) {
            mLastValue = mTimer.stopTimer();
            mTimer = null;
        }
    }

    private CountUpTimer.Callback mTimerCallback = new CountUpTimer.Callback() {
        @Override
        public void onCountUp(int value) {
            Log.d(TAG, "onCountUp: mValue = " + value);

            // callback for client.
            if (mCallback != null) {
                try {
                    mCallback.onCountUp(value);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            // if value is 100, write timer experience.
            if (value % 100 == 0) {
                SharedPreferences.Editor editor =
                        AppFiles.getSharedPreferences(TimerService.this).edit();
                editor.putInt(PREF_KEY_TIMER_EXPERIENCE, value / 100);
                editor.apply();
            }
        }
    };

    //
    // Service Stub
    //

    private ITimerServiceCallback mCallback = null;

    private ITimerService.Stub mBinder = new ITimerService.Stub() {
        @Override
        public void registerCallback(ITimerServiceCallback callback) throws RemoteException {
            Log.d(TAG, "registerCallback: start");
            synchronized (this) {
                mCallback = callback;
            }
        }

        @Override
        public void unregisterCallback(ITimerServiceCallback callback) throws RemoteException {
            Log.d(TAG, "unregisterCallback: start");
            synchronized (this) {
                mCallback = null;
            }
        }

        @Override
        public void startTimer() throws RemoteException {
            Log.d(TAG, "startTimer: start");
            startTimerService();
        }

        @Override
        public void stopTimer() throws RemoteException {
            Log.d(TAG, "stopTimer: start");
            stopTimerService();
        }

        @Override
        public int getPid() {
            return android.os.Process.myPid();
        }

        @Override
        public boolean isTimerRunning() throws RemoteException {
            return mTimer != null;
        }

        @Override
        public int getLatestValue() throws RemoteException {
            synchronized (this) {
                if (mTimer != null) {
                    return mTimer.getValue();
                } else {
                    return mLastValue;
                }
            }
        }
    };

}
