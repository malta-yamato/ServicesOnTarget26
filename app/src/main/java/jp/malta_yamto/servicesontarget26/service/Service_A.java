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

import jp.malta_yamto.servicesontarget26.aidl.ITimerService;
import jp.malta_yamto.servicesontarget26.aidl.ITimerServiceCallback;

public class Service_A extends Service {
    private static final String TAG = "Service_A";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: start");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: flags, startId = " + flags + ", " + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mCallback = null;
        return super.onUnbind(intent);
    }

    //
    // Service Stub
    //

    private ITimerServiceCallback mCallback = null;

    private ITimerService.Stub mBinder = new ITimerService.Stub() {
        @Override
        public void registerCallback(ITimerServiceCallback callback) throws RemoteException {
            mCallback = callback;
        }

        @Override
        public void unregisterCallback(ITimerServiceCallback callback) throws RemoteException {
            mCallback = null;
        }
    };
}
