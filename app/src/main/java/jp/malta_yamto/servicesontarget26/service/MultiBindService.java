package jp.malta_yamto.servicesontarget26.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jp.malta_yamto.servicesontarget26.aidl.IMultiBindService;
import jp.malta_yamto.servicesontarget26.aidl.IMultiBindServiceCallback;

public class MultiBindService extends Service {
    private static final String TAG = "MultiBindService";

    private int counter = 0;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, ServiceThread> mServiceThreads = new HashMap<>();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: start");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: start");
        return new Stub();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: start");
        super.onDestroy();
    }

    //
    // ServiceThread
    //

    static class ServiceThread extends Thread {

        private final int id;

        private int value;

        private final IMultiBindServiceCallback callback;

        private final OnTerminateListener onTerminateListener;

        private ServiceThread(int id, int value, IMultiBindServiceCallback callback,
                OnTerminateListener onTerminateListener) {
            this.id = id;
            this.value = value;
            this.callback = callback;
            this.onTerminateListener = onTerminateListener;
        }

        @Override
        public void run() {
            try {
                Random random = new Random();
                int sleepMillis = random.nextInt(10) * 60;
                Log.d(TAG, "run: sleepMillis = " + sleepMillis);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                value += 10000;
                e.printStackTrace();
            } finally {
                try {
                    if (callback != null) {
                        callback.onReply(value);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (onTerminateListener != null) {
                    onTerminateListener.onTerminate(id);
                }
            }
        }

        interface OnTerminateListener {
            void onTerminate(int id);
        }
    }

    //
    // Service Stub
    //

    class Stub extends IMultiBindService.Stub {

        @Override
        public int send(int value, IMultiBindServiceCallback callback) throws RemoteException {
            synchronized (this) {
                int id = counter++;
                Log.d(TAG, "send: id, value = " + id + ", " + value);
                ServiceThread thread = new ServiceThread(id, value, callback,
                        new ServiceThread.OnTerminateListener() {
                            @Override
                            public void onTerminate(int id) {
                                mServiceThreads.remove(id);
                            }
                        });
                mServiceThreads.put(id, thread);
                thread.start();
                return id;
            }
        }

        @Override
        public void interrupt(int id) throws RemoteException {
            synchronized (this) {
                Log.d(TAG, "interrupt: id = " + id);
                ServiceThread thread = mServiceThreads.get(id);
                if (thread != null) {
                    thread.interrupt();
                }
            }
        }
    }

}