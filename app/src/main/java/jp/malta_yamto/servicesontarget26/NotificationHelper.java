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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import jp.malta_yamto.servicesontarget26.service.ForegroundLocalTimerService;
import jp.malta_yamto.servicesontarget26.service.ForegroundTimerService;
import jp.malta_yamto.servicesontarget26.service.LocalTimerService;

/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {

    public static int ID_FOREGROUND_TIMER_SERVICE = 1;
    public static int ID_FOREGROUND_LOCAL_TIMER_SERVICE = 3;

    public static int ID_TIMER_RESULT = 100;
    public static int ID_TIMER_RESULT_FOREGROUND = 101;
    public static int ID_TIMER_RESULT_LOCAL = 102;
    public static int ID_TIMER_RESULT_FOREGROUND_LOCAL = 103;

    public static final String DEFAULT_CHANNEL = "default";
    public static final String FOREGROUND_SERVICE_CHANNEL = "foreground_service";

    private NotificationManagerCompat mManager;

    public NotificationHelper(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // default channel
            NotificationChannel primaryChannel = new NotificationChannel(DEFAULT_CHANNEL,
                    getString(R.string.noti_primary_channel), NotificationManager.IMPORTANCE_HIGH);
            primaryChannel.setLightColor(Color.RED);
            primaryChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(primaryChannel);

            // foreground service channel
            NotificationChannel foregroundServiceChannel =
                    new NotificationChannel(FOREGROUND_SERVICE_CHANNEL,
                            getString(R.string.noti_foreground_service_channel),
                            NotificationManager.IMPORTANCE_LOW);
            foregroundServiceChannel.setLightColor(Color.GREEN);
            foregroundServiceChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(foregroundServiceChannel);
        }
    }

    @SuppressWarnings("unused")
    public NotificationCompat.Builder getNotification(String channelId, String title, String body) {
        return new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(title).setContentText(body).setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }

    @SuppressWarnings("unused")
    public NotificationCompat.Builder getNotification(String channelId, String title,
            String subject, String[] lines) {
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (int i = 0; i < lines.length; i++) {
            style.addLine(lines[i]);
        }
        return new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(title).setContentText(subject).setStyle(style)
                .setSmallIcon(getSmallIcon()).setAutoCancel(true);
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    public void notifyTimerResult(Class<? extends Service> clazz,
            NotificationCompat.Builder notification) {
        int id = ID_TIMER_RESULT;
        if (clazz.equals(ForegroundTimerService.class)) {
            id = ID_TIMER_RESULT_FOREGROUND;
            notification.setContentTitle("Foreground Timer Service results");
        } else if (clazz.equals(LocalTimerService.class)) {
            id = ID_TIMER_RESULT_LOCAL;
            notification.setContentTitle("Local Timer Service results");
        } else if (clazz.equals(ForegroundLocalTimerService.class)) {
            id = ID_TIMER_RESULT_FOREGROUND_LOCAL;
            notification.setContentTitle("Foreground Local Timer Service results");
        }
        notify(id, notification);
    }

    private int getSmallIcon() {
        return android.R.drawable.stat_notify_chat;
    }

    private NotificationManagerCompat getManager() {
        if (mManager == null) {
            mManager = NotificationManagerCompat.from(getApplicationContext());
        }
        return mManager;
    }

}
