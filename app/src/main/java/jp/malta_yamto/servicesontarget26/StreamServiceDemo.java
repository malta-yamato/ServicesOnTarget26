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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.malta_yamto.servicesontarget26.aidl.IStreamService;
import jp.malta_yamto.servicesontarget26.aidl.IStreamServiceCallback;
import jp.malta_yamto.servicesontarget26.service.StreamService;

public class StreamServiceDemo extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "StreamServiceDemo";

    private Handler mHandler = new Handler();

    IStreamService mStreamService;
    protected ServiceConnection mConnection = null;
    protected boolean mShouldServiceExisting = false;

    private TextView mInfoText;
    private Button mConnectServiceButton;
    private Button mDisconnectServiceButton;
    private Button mKillServiceButton;
    private Button mStartTransmittingServiceButton;
    private Button mStartReceivingServiceButton;
    private Button mInterruptTransmittingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stream_service_demo_main);
        mInfoText = findViewById(R.id.text_info);
        mConnectServiceButton = findViewById(R.id.button_connect_service);
        mDisconnectServiceButton = findViewById(R.id.button_disconnect_service);
        mKillServiceButton = findViewById(R.id.button_kill_service);
        mStartTransmittingServiceButton = findViewById(R.id.button_start_transmitting_service);
        mStartReceivingServiceButton = findViewById(R.id.button_start_receiving_service);
        mInterruptTransmittingButton = findViewById(R.id.button_interrupt_transmitting);

        turnDefaultCondition();
    }

    protected void turnDefaultCondition() {
        mShouldServiceExisting = false;
        mConnection = null;
        mStreamService = null;
        mConnectServiceButton.setEnabled(true);
        mDisconnectServiceButton.setEnabled(false);
        mKillServiceButton.setEnabled(false);
        mStartTransmittingServiceButton.setEnabled(false);
        mStartReceivingServiceButton.setEnabled(false);
        mInterruptTransmittingButton.setEnabled(false);
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

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: start");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (mConnection != null) {
            unbindService(mConnection);
        }
        super.onDestroy();
    }

    //
    // service connection
    //

    public void onConnectServiceClick(View view) {
        Log.d(TAG, "onConnectServiceClick: start");
        generateServiceIfNotExistAndSendStartCommandAndBind();
    }

    public void onDisconnectServiceClick(View view) {
        Log.d(TAG, "onDisconnectServiceClick: start");
        stopService();
    }

    public void onKillServiceClick(View view) {
        Log.d(TAG, "onKillServiceClick: start");
        killService();
    }

    protected void generateServiceIfNotExistAndSendStartCommandAndBind() {
        Intent serviceIntent = new Intent(this, StreamService.class);
        startService(serviceIntent);
        mConnection = createConnection();
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mShouldServiceExisting = true;
    }

    protected void stopService() {
        if (mConnection != null) {
            synchronized (this) {
                if (mTransmittingThread != null) {
                    mTransmittingThread.interrupt();
                }
            }
            unbindService(mConnection);
            Intent serviceIntent = new Intent(this, StreamService.class);
            stopService(serviceIntent);
        }
        turnDefaultCondition();
    }

    protected void killService() {
        if (mConnection != null) {
            int pid = 0;
            try {
                pid = mStreamService.getPid();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (pid > 0) {
                Process.killProcess(pid);
            } else {
                throw new IllegalStateException("failed kill process");
            }
        }
    }

    protected ServiceConnection createConnection() {
        return new ServiceConnection() {
            // Called when the connection with the service is established
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d(TAG, "onServiceConnected: start");
                mStreamService = IStreamService.Stub.asInterface(service);
                try {
                    mStreamService.registerCallback(mCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mConnectServiceButton.setEnabled(false);
                mDisconnectServiceButton.setEnabled(true);
                mKillServiceButton.setEnabled(true);
                mStartTransmittingServiceButton.setEnabled(true);
                mInterruptTransmittingButton.setEnabled(true);
                mStartReceivingServiceButton.setEnabled(true);
            }

            // Called when the connection with the service disconnects unexpectedly
            public void onServiceDisconnected(ComponentName className) {
                Log.d(TAG, "onServiceDisconnected: start");
                unbindService(mConnection);
                turnDefaultCondition();
            }
        };
    }

    IStreamServiceCallback mCallback = new IStreamServiceCallback.Stub() {

        @Override
        public void onReplyDigest(byte[] md5) throws RemoteException {
            Log.d(TAG, "onReplyDigest: start");
            synchronized (StreamServiceDemo.this) {
                if (mCountDownLatch != null) {
                    mMd5 = md5;
                    mCountDownLatch.countDown();
                }
            }
        }
    };

    //
    // transmitting service
    //

    public void onStartTransmittingServiceClick(View view) {
        Log.d(TAG, "onStartTransmittingServiceClick: start");
        if (mStreamService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        performReceiving(mStreamService.getInputStream());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            mStartTransmittingServiceButton.setEnabled(false);
            mStartReceivingServiceButton.setEnabled(false);
        }
    }

    private void performReceiving(ParcelFileDescriptor forInput) {
        // receive buffer
        int SIZE = 65536;
        byte[] buf = new byte[SIZE];
        byte[] unit = new byte[SIZE];
        int ptr = 0;
        int iter = 0;
        // receiving
        byte[] md5 = null;
        BufferedInputStream in = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            in = new BufferedInputStream(new ParcelFileDescriptor.AutoCloseInputStream(forInput));
            while (true) {
                int count = in.read(buf);
                if (count < 0) {
                    break;
                }
                if (count > 0) {
                    if (ptr + count < SIZE) {
                        System.arraycopy(buf, 0, unit, ptr, count);
                        ptr += count;
                    } else {
                        int rem = SIZE - ptr;
                        System.arraycopy(buf, 0, unit, ptr, rem);
                        digest.update(unit);
                        Log.d(TAG, "data No." + ++iter + " received.");
                        int rem2 = count - rem;
                        System.arraycopy(buf, rem, unit, 0, rem2);
                        ptr = rem2;
                    }
                }
            }
            if (ptr > 0) {
                Log.d(TAG, "performReceiving: last memory");
                byte[] lastUnit = new byte[ptr];
                System.arraycopy(unit, 0, lastUnit, 0, ptr);
                digest.update(lastUnit);
            }
            // complete digest
            md5 = digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (in != null) {
                try {
                    Log.d(TAG, "performReceiving: close input");
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStreamService != null) {
                        mStartTransmittingServiceButton.setEnabled(true);
                        mInterruptTransmittingButton.setEnabled(true);
                    }
                }
            });
        }

        // check digest ( if pipe found EOF after service pipe closed, digest must have been set. )
        Log.d(TAG, "performReceiving: check digest");
        boolean checkDigest = false;
        try {
            if (mStreamService != null) {
                checkDigest = mStreamService.checkDigest(md5);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (checkDigest) {
            Log.d(TAG, "performReceiving: receiving has been completed successfully.");
        }

    }

    public void onInterruptTransmittingClick(View view) {
        Log.d(TAG, "onInterruptTransmittingClick: start");
        if (mStreamService != null) {
            try {
                mStreamService.interruptTransmittingThread();
                mStartTransmittingServiceButton.setEnabled(true);
                mStartReceivingServiceButton.setEnabled(false);
                mInterruptTransmittingButton.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    //
    // receiving service
    //

    public void onStartReceivingServiceClick(View view) {
        Log.d(TAG, "onStartReceivingService: start");
        if (mStreamService != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        performTransmitting(mStreamService.getOutputStream());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            mStartTransmittingServiceButton.setEnabled(false);
            mStartReceivingServiceButton.setEnabled(false);
        }
    }

    private Thread mTransmittingThread = null;
    private CountDownLatch mCountDownLatch = null;
    private byte[] mMd5 = null;

    private void performTransmitting(ParcelFileDescriptor forOutput) {
        int UNIT_SIZE = 65536;
        int TRANSMIT_SIZE = 39393;
        int ITERATION = 10000;

        // this thread is transmitting thread
        synchronized (StreamServiceDemo.this) {
            mTransmittingThread = Thread.currentThread();
        }
        // prepare data
        byte[] unit = new byte[UNIT_SIZE];
        for (int i = 0; i < UNIT_SIZE; i++) {
            unit[i] = (byte) i;
        }
        // transmitting
        if (forOutput == null) {
            throw new IllegalStateException("no pipe");
        }
        byte[] md5 = null;
        BufferedOutputStream out = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            out = new BufferedOutputStream(
                    new ParcelFileDescriptor.AutoCloseOutputStream(forOutput));
            for (int i = 0; i < ITERATION; i++) {
                // check interrupt before transmitting
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                Log.d(TAG, "data No." + i + " transmit.");
                // transmit unit
                byte[] transmitUnit = new byte[TRANSMIT_SIZE];
                System.arraycopy(unit, 0, transmitUnit, 0, TRANSMIT_SIZE);
                digest.update(transmitUnit);
                // write
//                out.write(transmitUnit);
                for (int j = 0; j < TRANSMIT_SIZE; j++) {
                    out.write(transmitUnit[j]);
                }
            }
            // complete digest
            md5 = digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (out != null) {
                try {
                    Log.d(TAG, "performTransmitting: close output");
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            synchronized (this) {
                mTransmittingThread = null;
            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mStreamService != null) {
                        mStartReceivingServiceButton.setEnabled(true);
//                        mInterruptReceivingButton.setEnabled(true);
                    }
                }
            });
        }

        // check digest
        Log.d(TAG, "performTransmitting: check digest");
        synchronized (this) {
            if (mCountDownLatch != null) {
                throw new IllegalStateException("countdown latch");
            }
            mCountDownLatch = new CountDownLatch(1);
        }
        try {
            mCountDownLatch.await(60000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized (this) {
            mCountDownLatch = null;
        }
        boolean checkDigest = false;
        Log.d(TAG, "checkDigest: md5, mMd5 = " + StreamService.byte2hexstr(md5) + ", " +
                StreamService.byte2hexstr(mMd5));
        checkDigest = md5 != null && mMd5 != null && Arrays.equals(md5, mMd5);
        if (checkDigest) {
            Log.d(TAG, "performTransmitting: transmitting has been completed successfully.");
        }
    }

}
