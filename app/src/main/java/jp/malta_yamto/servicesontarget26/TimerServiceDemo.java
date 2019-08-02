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
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.malta_yamto.servicesontarget26.aidl.ITimerService;
import jp.malta_yamto.servicesontarget26.aidl.ITimerServiceCallback;
import jp.malta_yamto.servicesontarget26.service.TimerService;

public class TimerServiceDemo extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "TimerServiceDemo";

    private Handler mHandler = new Handler();

    ITimerService mTimerService;
    protected ServiceConnection mConnection = null;
    protected boolean mShouldServiceExisting = false;

    private TextView mTimerText;
    private Button mConnectServiceButton;
    private Button mDisconnectServiceButton;
    private Button mStartTimerButton;
    private Button mStopTimerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer_service_demo_main);
        mTimerText = findViewById(R.id.text_timer);
        mConnectServiceButton = findViewById(R.id.button_connect_service);
        mDisconnectServiceButton = findViewById(R.id.button_disconnect_service);
        mStartTimerButton = findViewById(R.id.button_start_timer);
        mStopTimerButton = findViewById(R.id.button_stop_timer);

        turnDefaultCondition();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: start");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();

        mStartTimerButton.setEnabled(false);
        mStopTimerButton.setEnabled(false);
        mTimerText.setText("");

        if (mShouldServiceExisting) {
            generateServiceIfNotExistAndSendStartCommandAndBind();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: start");
        super.onPause();

        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    protected void generateServiceIfNotExistAndSendStartCommandAndBind() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        startService(serviceIntent);
        mConnection = createConnection();
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mShouldServiceExisting = true;
    }

    protected void killService() {
        if (mConnection != null) {
            unbindService(mConnection);
            Intent serviceIntent = new Intent(this, TimerService.class);
            stopService(serviceIntent);
        }
        turnDefaultCondition();
    }

    //
    // Button Listener
    //

    public void onConnectServiceClick(View view) {
        Log.d(TAG, "onConnectServiceClick: start");
        generateServiceIfNotExistAndSendStartCommandAndBind();
    }

    public void onStartTimerClick(View view) {
        Log.d(TAG, "onStartTimerClick: start");
        if (mTimerService != null) {
            try {
                mTimerService.startTimer();
                mStartTimerButton.setEnabled(false);
                mStopTimerButton.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onStopTimerClick(View view) {
        Log.d(TAG, "onStopTimerClick: start");
        if (mTimerService != null) {
            try {
                mTimerService.stopTimer();
                mStartTimerButton.setEnabled(true);
                mStopTimerButton.setEnabled(false);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDisconnectServiceClick(View view) {
        Log.d(TAG, "onDisconnectServiceClick: start");
        killService();
    }

    //
    // Service
    //

    protected ServiceConnection createConnection() {
        return new ServiceConnection() {
            // Called when the connection with the service is established
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d(TAG, "onServiceConnected: start");
                mTimerService = ITimerService.Stub.asInterface(service);
                try {
                    mTimerService.registerCallback(mCallback);
                    if (mTimerService.isTimerRunning()) {
                        mStartTimerButton.setEnabled(false);
                        mStopTimerButton.setEnabled(true);
                    } else {
                        mStartTimerButton.setEnabled(true);
                        mStopTimerButton.setEnabled(false);
                    }
                    mConnectServiceButton.setEnabled(false);
                    mDisconnectServiceButton.setEnabled(true);
                    mTimerText.setText(String.valueOf(mTimerService.getLatestValue()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            // Called when the connection with the service disconnects unexpectedly
            public void onServiceDisconnected(ComponentName className) {
                Log.d(TAG, "onServiceDisconnected: start");
                turnDefaultCondition();
            }
        };
    }

    protected void turnDefaultCondition() {
        mShouldServiceExisting = false;
        mConnection = null;
        mTimerService = null;
        mConnectServiceButton.setEnabled(true);
        mDisconnectServiceButton.setEnabled(false);
        mStartTimerButton.setEnabled(false);
        mStopTimerButton.setEnabled(false);
    }

    ITimerServiceCallback mCallback = new ITimerServiceCallback.Stub() {
        @Override
        public void onCountUp(final int currentTimeSec) throws RemoteException {
            Log.d(TAG, "onCountUp: currentTimeSec = " + currentTimeSec);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTimerText.setText(String.valueOf(currentTimeSec));
                }
            });
        }
    };

}
