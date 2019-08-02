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

import android.content.Intent;
import androidx.core.app.NotificationCompat;

import jp.malta_yamto.servicesontarget26.NotificationHelper;

public class ForegroundTimerService extends TimerService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // run on foreground
        NotificationCompat.Builder builder = mNotificationHelper
                .getNotification(NotificationHelper.FOREGROUND_SERVICE_CHANNEL, "Foreground Timer",
                        "running");
        startForeground(NotificationHelper.ID_FOREGROUND_TIMER_SERVICE, builder.build());

        return super.onStartCommand(intent, flags, startId);
    }

}
