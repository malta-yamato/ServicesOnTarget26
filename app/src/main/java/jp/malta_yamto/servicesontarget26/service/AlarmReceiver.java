package jp.malta_yamto.servicesontarget26.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by malta on 2017/11/23.
 * AlarmReceiver
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: start");
        RingtoneJobIntentService
                .enqueueWork(context, new Intent(RingtoneJobIntentService.REQUEST_TOAST));
        RingtoneJobIntentService
                .enqueueWork(context, new Intent(RingtoneJobIntentService.REQUEST_RINGTONE));
    }
}
