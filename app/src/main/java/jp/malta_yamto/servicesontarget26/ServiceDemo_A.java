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

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.malta_yamto.servicesontarget26.service.Service_A;

public class ServiceDemo_A extends AppCompatActivity {
    private static final String TAG = "ServiceDemo_A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_a_main);
        SharedPreferences prefs = AppFiles.getSharedPreferences(this, Service_A.class);
        String beforeText = prefs.getString("TEST", "none");
        Log.d(TAG, "onCreate: beforeText = " + beforeText);
        String afterText = SimpleDateFormat.getDateTimeInstance().format(new Date());
        Log.d(TAG, "onCreate: afterText = " + afterText);
        prefs.edit().putString("TEST", afterText).apply();
    }
}
