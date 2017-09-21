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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.malta_yamto.servicesontarget26.aidl.ITimerService;
import jp.malta_yamto.servicesontarget26.aidl.ITimerServiceCallback;
import jp.malta_yamto.servicesontarget26.service.Service_A;

public class ServiceDemo_A extends AppCompatActivity {
    private static final String TAG = "ServiceDemo_A";

    private Handler mHandler = new Handler();

    private TextView mTimerText;
    private Button mStartTimerButton;
    private Button mStopTimerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.demo_a_main);
        mTimerText = findViewById(R.id.text_timer);
        mStartTimerButton = findViewById(R.id.button_start_timer);
        mStopTimerButton = findViewById(R.id.button_stop_timer);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();

        mStartTimerButton.setEnabled(false);
        mStopTimerButton.setEnabled(false);

        Intent serviceIntent = new Intent(this, Service_A.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: start");
        super.onPause();

        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    //
    // Button Listener
    //

    public void onStartTimerClick(View view) {
        Log.d(TAG, "onStartTimerClick: start");
        if (mITimerService != null) {
            try {
                mITimerService.startTimer();
                mStartTimerButton.setEnabled(false);
                mStopTimerButton.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onStopTimerClick(View view) {
        Log.d(TAG, "onStopTimerClick: start");
        if (mITimerService != null) {
            try {
                mITimerService.stopTimer();
                mStartTimerButton.setEnabled(true);
                mStopTimerButton.setEnabled(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    //
    // Service
    //

    ITimerService mITimerService;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected: start");
            mITimerService = ITimerService.Stub.asInterface(service);
            try {
                mITimerService.registerCallback(mCallback);
                mStartTimerButton.setEnabled(true);
                mStopTimerButton.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "onServiceDisconnected: start");
            mITimerService = null;
        }
    };

    ITimerServiceCallback mCallback = new ITimerServiceCallback.Stub() {
        @Override
        public void onCountUp(final int currentTimeSec) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTimerText.setText(String.valueOf(currentTimeSec));
                }
            });
        }
    };

}
