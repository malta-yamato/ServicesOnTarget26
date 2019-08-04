/*
 * Copyright (C) 2019 MALTA-YAMATO
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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;

import jp.malta_yamto.servicesontarget26.aidl.IStreamService;
import jp.malta_yamto.servicesontarget26.aidl.IStreamServiceCallback;

public class StreamService extends Service {
    @SuppressWarnings("unused")
    private static final String TAG = "StreamService";

    private Thread mTransmittingThread = null;
    private Thread mReceivingThread = null;

    private byte[] mMd5 = null;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: start");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: flags, startId = " + flags + ", " + startId);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: start");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: start");
        synchronized (this) {
            mCallback = null;
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: start");
        synchronized (this) {
            if (mTransmittingThread != null) {
                mTransmittingThread.interrupt();
                mTransmittingThread = null;
            }
            if (mReceivingThread != null) {
                mReceivingThread.interrupt();
                mReceivingThread = null;
            }
        }
        super.onDestroy();
    }

    //
    // Transmitting
    //

    private ParcelFileDescriptor startTransmitting() throws RemoteException {
        ParcelFileDescriptor forInput = null;
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();

            final ParcelFileDescriptor forOutput = pipe[1];
            if (forOutput == null) {
                throw new RemoteException("failed creating pipe");
            }

            // run transmitting thread
            synchronized (this) {
                if (mTransmittingThread != null) {
                    throw new RemoteException("thread is still available");
                }
                mMd5 = null;
                mTransmittingThread =
                        new TransmittingThread(forOutput, new TransmittingThread.OnCloseListener() {
                            @Override
                            public void onClose(byte[] md5) {
                                // this thread is another thread
                                synchronized (StreamService.this) {
                                    mMd5 = md5;
                                    mTransmittingThread = null;
                                }
                            }
                        });
                mTransmittingThread.start();
            }

            forInput = pipe[0];
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return descriptor
        if (forInput != null) {
            return forInput;
        }

        throw new RemoteException("failed creating pipe");
    }

    private synchronized void interruptTransmitting() {
        if (mTransmittingThread != null) {
            Log.d(TAG, "interruptTransmitting: interrupt!");
            mTransmittingThread.interrupt();
            mTransmittingThread = null;
        }
    }

    private static class TransmittingThread extends Thread {

        int UNIT_SIZE = 65536;
        int TRANSMIT_SIZE = 39393;
        int ITERATION = 10000;

        private ParcelFileDescriptor forOutput;
        private byte[] md5 = null;
        private OnCloseListener onCloseListener;

        private TransmittingThread(ParcelFileDescriptor desc, OnCloseListener listener) {
            forOutput = desc;
            onCloseListener = listener;
        }

        @Override
        public void run() {
            // prepare data
            byte[] unit = new byte[UNIT_SIZE];
            for (int i = 0; i < UNIT_SIZE; i++) {
                unit[i] = (byte) i;
            }
            // transmitting
            if (forOutput == null) {
                throw new IllegalStateException("no pipe");
            }
            BufferedOutputStream out = null;
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                out = new BufferedOutputStream(
                        new ParcelFileDescriptor.AutoCloseOutputStream(forOutput));
                // write 65M
                for (int i = 0; i < ITERATION; i++) {

                    // check interrupt before transmit
                    if (Thread.interrupted()) {
                        throw new InterruptedException("interrupt");
                    }
                    // transmit unit
                    byte[] transmitUnit = new byte[TRANSMIT_SIZE];
                    System.arraycopy(unit, 0, transmitUnit, 0, TRANSMIT_SIZE);
                    digest.update(transmitUnit);

                    // write
//                    out.write(transmitUnit);
                    for (int j = 0; j < TRANSMIT_SIZE; j++) {
                        out.write(transmitUnit[j]);
                    }
                }

                // complete digest
                byte[] byteMd5 = digest.digest();
                Log.d(TAG, "run: strMd5 = " + byte2hexstr(byteMd5));
                md5 = byteMd5;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (onCloseListener != null) {
                    onCloseListener.onClose(md5);
                }
                if (out != null) {
                    try {
                        Log.d(TAG, "run: close output");
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                forOutput = null;
            }
        }

        interface OnCloseListener {
            void onClose(byte[] md5);
        }
    }

    //
    // receiving
    //

    private ParcelFileDescriptor startReceiving() throws RemoteException {
        ParcelFileDescriptor forOutput = null;
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();

            final ParcelFileDescriptor forInput = pipe[0];
            if (forInput == null) {
                throw new RemoteException("failed creating pipe");
            }

            // run receiving thread
            synchronized (this) {
                if (mReceivingThread != null) {
                    throw new RemoteException("thread is still available");
                }
                mMd5 = null;
                mReceivingThread =
                        new ReceivingThread(forInput, new ReceivingThread.OnCloseListener() {
                            @Override
                            public void onClose(byte[] md5) {
                                // this thread is another thread
                                synchronized (StreamService.this) {
                                    mMd5 = md5;
                                    if (mCallback != null) {
                                        try {
                                            mCallback.onReplyDigest(md5);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    mReceivingThread = null;
                                }
                            }
                        });
                mReceivingThread.start();
            }

            forOutput = pipe[1];
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return descriptor
        if (forOutput != null) {
            return forOutput;
        }

        throw new RemoteException("failed creating pipe");
    }

    private void interruptReceiving() {
        if (mReceivingThread != null) {
            Log.d(TAG, "interruptReceiving: interrupt!");
            mReceivingThread.interrupt();
            mReceivingThread = null;
        }
    }

    private static class ReceivingThread extends Thread {

        static int BUF_SIZE = 65536;

        private ParcelFileDescriptor forInput;
        private byte[] md5 = null;
        private OnCloseListener onCloseListener;

        private ReceivingThread(ParcelFileDescriptor desc, OnCloseListener listener) {
            forInput = desc;
            onCloseListener = listener;
        }

        @Override
        public void run() {
            // receiving
            if (forInput == null) {
                throw new IllegalStateException("no pipe");
            }
            BufferedInputStream in = null;
            byte[] buf = new byte[BUF_SIZE];
            try {
                MessageDigest digest = MessageDigest.getInstance("MD5");
                in = new BufferedInputStream(
                        new ParcelFileDescriptor.AutoCloseInputStream(forInput));
                while (true) {
                    // check interrupt before receive
                    if (Thread.interrupted()) {
                        throw new InterruptedException("interrupt");
                    }
                    //
                    int count = in.read(buf);
                    if (count < 0) {
                        break;
                    }
                    if (count > 0) {
                        byte[] receivingUnit = new byte[count];
                        System.arraycopy(buf, 0, receivingUnit, 0, count);
                        digest.update(receivingUnit);
                    }
                }
                // complete digest
                byte[] byteMd5 = digest.digest();
                Log.d(TAG, "run: strMd5 = " + byte2hexstr(byteMd5));
                md5 = byteMd5;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (onCloseListener != null) {
                    onCloseListener.onClose(md5);
                }
                if (in != null) {
                    try {
                        Log.d(TAG, "run: close input");
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                forInput = null;
            }
        }

        interface OnCloseListener {
            void onClose(byte[] md5);
        }

    }

    //
    //
    //

    public static String byte2hexstr(byte[] rawByte) {
        if (rawByte == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rawByte.length; i++) {
            builder.append(Integer.toHexString(0xff & rawByte[i]));
            builder.append("-");
        }
        return builder.substring(0, builder.length() - 1).toUpperCase(Locale.ENGLISH);
    }

    //
    // Service Stub
    //

    private IStreamServiceCallback mCallback = null;

    private IStreamService.Stub mBinder = new IStreamService.Stub() {

        @Override
        public void registerCallback(IStreamServiceCallback callback) throws RemoteException {
            Log.d(TAG, "registerCallback: start");
            synchronized (this) {
                mCallback = callback;
            }
        }

        @Override
        public void unregisterCallback(IStreamServiceCallback callback) throws RemoteException {
            Log.d(TAG, "unregisterCallback: start");
            synchronized (this) {
                mCallback = null;
            }
        }

        @Override
        public int getPid() throws RemoteException {
            return android.os.Process.myPid();
        }

        @Override
        public ParcelFileDescriptor getInputStream() throws RemoteException {
            return startTransmitting();
        }

        @Override
        public ParcelFileDescriptor getOutputStream() throws RemoteException {
            return startReceiving();
        }

        @Override
        public boolean checkDigest(byte[] md5) {
            Log.d(TAG, "checkDigest: md5, mMd5 = " + byte2hexstr(md5) + ", " + byte2hexstr(mMd5));
            return md5 != null && mMd5 != null && Arrays.equals(md5, mMd5);
        }

        @Override
        public void interruptTransmittingThread() throws RemoteException {
            interruptTransmitting();
        }

        @Override
        public void interruptReceivingThread() throws RemoteException {
            interruptReceiving();
        }
    };

}
