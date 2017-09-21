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
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import jp.malta_yamto.servicesontarget26.aidl.ITimerService;
import jp.malta_yamto.servicesontarget26.aidl.ITimerServiceCallback;

public class Service_A extends Service {
    private static final String TAG = "Service_A";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: start");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: flags, startId = " + flags + ", " + startId);
        return super.onStartCommand(intent, flags, startId);
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
        mCallback = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: start");
        stopTimerService();
        super.onDestroy();
    }

    //
    // Timer
    //

    private Timer mTimer;
    private CountUpTimerTask mCountUpTimerTask;

    private int mLastValue = 0;

    // non static class
    private class CountUpTimerTask extends TimerTask {

        int value = 0;

        CountUpTimerTask(Integer value) {
            this.value = value;
        }

        @Override
        public void run() {
            value += 1;
            if (mCallback == null) {
                return;
            }

            try {
                mCallback.onCountUp(value);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NullPointerException ignore) {
            }
        }

    }

    private synchronized void startTimerService() {
        Log.d(TAG, "startTimerService: start");
        if (mTimer == null) {
            mTimer = new Timer();
            mCountUpTimerTask = new CountUpTimerTask(mLastValue);
            mTimer.schedule(mCountUpTimerTask, 1000L, 1000L);
        }
    }

    private synchronized void stopTimerService() {
        Log.d(TAG, "stopTimerService: start");
        if (mTimer != null) {
            mLastValue = mCountUpTimerTask.value;
            mTimer.cancel();
            mCountUpTimerTask = null;
            mTimer = null;
        }
    }

    //
    // Service Stub
    //

    private ITimerServiceCallback mCallback = null;

    private ITimerService.Stub mBinder = new ITimerService.Stub() {
        @Override
        public void registerCallback(ITimerServiceCallback callback) throws RemoteException {
            Log.d(TAG, "registerCallback: start");
            mCallback = callback;
        }

        @Override
        public void unregisterCallback(ITimerServiceCallback callback) throws RemoteException {
            Log.d(TAG, "unregisterCallback: start");
            mCallback = null;
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
    };

}
