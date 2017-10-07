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

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import jp.malta_yamto.servicesontarget26.service.ForegroundTimerService;

public class ForegroundTimerServiceDemo extends TimerServiceDemo {

    protected void generateServiceIfNotExistAndSendStartCommandAndBind() {
        Intent serviceIntent = new Intent(this, ForegroundTimerService.class);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startService(serviceIntent);
        } else {
            startForegroundService(serviceIntent);
        }
        mConnection = createConnection();
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mShouldServiceExisting = true;
    }

    protected void killService() {
        if (mConnection != null) {
            unbindService(mConnection);
            Intent serviceIntent = new Intent(this, ForegroundTimerService.class);
            stopService(serviceIntent);
        }
        turnDefaultCondition();
    }

}
