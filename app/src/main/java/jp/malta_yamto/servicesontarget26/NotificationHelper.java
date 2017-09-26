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
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Helper class to manage notification channels, and create notifications.
 */
class NotificationHelper extends ContextWrapper {

    public static final String PRIMARY_CHANNEL = "primary";
    public static final String SECONDARY_CHANNEL = "secondary";

    private NotificationManagerCompat mManager;

    public NotificationHelper(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // primary channel
            NotificationChannel primaryChannel = new NotificationChannel(PRIMARY_CHANNEL,
                    getString(R.string.noti_primary_channel), NotificationManager.IMPORTANCE_HIGH);
            primaryChannel.setLightColor(Color.RED);
            primaryChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(primaryChannel);

            // secondary channel
            NotificationChannel secondaryChannel = new NotificationChannel(SECONDARY_CHANNEL,
                    getString(R.string.noti_secondary_channel),
                    NotificationManager.IMPORTANCE_DEFAULT);
            secondaryChannel.setLightColor(Color.GREEN);
            secondaryChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(secondaryChannel);
        }
    }

    public NotificationCompat.Builder getNotification(String title, String body) {
        return new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL)
                .setContentTitle(title).setContentText(body).setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }

    @SuppressWarnings("unused")
    public NotificationCompat.Builder getSecondaryNotification(String title, String body) {
        return new NotificationCompat.Builder(getApplicationContext(), SECONDARY_CHANNEL)
                .setContentTitle(title).setContentText(body).setSmallIcon(getSmallIcon())
                .setAutoCancel(true);
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
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
