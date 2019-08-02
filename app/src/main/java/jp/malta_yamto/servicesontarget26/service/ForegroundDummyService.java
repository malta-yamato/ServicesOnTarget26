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
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import jp.malta_yamto.servicesontarget26.NotificationHelper;

public class ForegroundDummyService extends Service {
    @SuppressWarnings("unused")
    private static final String TAG = "ForegroundDummyService";

    private NotificationHelper mNotificationHelper;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: start");
        super.onCreate();

        // notification helper
        mNotificationHelper = new NotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // run on foreground
        NotificationCompat.Builder builder = mNotificationHelper
                .getNotification(NotificationHelper.FOREGROUND_SERVICE_CHANNEL, "Foreground Dummy",
                        "running");
        startForeground(NotificationHelper.ID_FOREGROUND_DUMMY_SERVICE, builder.build());

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
