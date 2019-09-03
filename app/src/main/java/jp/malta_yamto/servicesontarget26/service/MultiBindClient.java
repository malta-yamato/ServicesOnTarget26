package jp.malta_yamto.servicesontarget26.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.malta_yamto.servicesontarget26.aidl.IMultiBindService;
import jp.malta_yamto.servicesontarget26.aidl.IMultiBindServiceCallback;

public abstract class MultiBindClient extends Service {

    protected abstract String TAG();

    protected abstract int value();

    IMultiBindService mMultiBindService;
    protected ServiceConnection mConnection = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG(), "onBind: start");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG(), "onCreate: start");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG(), "onStartCommand: start");
        bindMultiBindService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG(), "onDestroy: start");
        super.onDestroy();
    }

    //
    //
    //

    protected void bindMultiBindService() {
        Intent intent = getMultiBindServiceIntent();
        mConnection = createConnection();
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private Intent getMultiBindServiceIntent() {
        return new Intent(this, MultiBindService.class);
    }

    private ServiceConnection createConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG(), "onServiceConnected: ");
                mMultiBindService = IMultiBindService.Stub.asInterface(service);
                prepareCountDown();
                doService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG(), "onServiceDisconnected: ");
                unbindService(mConnection);
            }
        };
    }

    private CountDownLatch mCountDownLatch = null;
    private boolean mResult = true;

    private void prepareCountDown() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCountDownLatch = new CountDownLatch(100);
                try {
                    mCountDownLatch.await(120000, TimeUnit.MILLISECONDS);
                    if (mResult) {
                        Log.d(TAG(), "run: test is good.");
                    } else {
                        Log.d(TAG(), "run: test is bad.");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG(), "run: test is bad.");
                }

                unbindService(mConnection);
            }
        }).start();
    }

    private void doService() {
        if (mMultiBindService == null) {
            throw new IllegalStateException("no service");
        }

        final int myValue = value();
        try {
            for (int i = 0; i < 300; i++) {
                final int iter = i + 1;
                final boolean interruption = new Random().nextInt(10) == 0;

                int id = mMultiBindService.send(myValue, new IMultiBindServiceCallback.Stub() {
                    @Override
                    public void onReply(int value) throws RemoteException {
                        if (interruption) {
                            if (value == myValue || value == myValue + 10000) {
                                Log.d(TAG(), "onReply " + iter + ": success " + value);
                            } else {
                                Log.d(TAG(), "onReply " + iter + ": failure " + value);
                                mResult = false;
                            }
                        } else {
                            if (value == myValue) {
                                Log.d(TAG(), "onReply " + iter + ": success " + value);
                            } else {
                                Log.d(TAG(), "onReply " + iter + ": failure " + value);
                                mResult = false;
                            }
                        }
                        mCountDownLatch.countDown();
                    }
                });

                if (interruption) {
                    mMultiBindService.interrupt(id);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
