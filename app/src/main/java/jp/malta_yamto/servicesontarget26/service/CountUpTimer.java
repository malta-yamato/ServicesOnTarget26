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

package jp.malta_yamto.servicesontarget26.service;

import java.util.Timer;
import java.util.TimerTask;

public class CountUpTimer extends Timer {
    private int mValue;
    private Callback mCallback = null;

    public CountUpTimer(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void startTimer(long delay, long period) {
        schedule(new Task(), delay, period);
    }

    public int stopTimer() {
        cancel();
        return mValue;
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            mValue += 1;
            if (mCallback != null) {
                mCallback.onCountUp(mValue);
            }
        }
    }

    interface Callback {
        void onCountUp(int value);
    }

}
