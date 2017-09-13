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
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ActivityLauncher extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<ActivityInfo> mActivities = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView listView = new ListView(this);
        setContentView(listView);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo("jp.malta_yamto.servicesontarget26",
                    PackageManager.GET_ACTIVITIES);
            mActivities = new ArrayList<>(Arrays.asList(pi.activities));
            for (Iterator<ActivityInfo> it = mActivities.iterator(); it.hasNext(); ) {
                if (getClass().getName().equals(it.next().name)) it.remove();
            }

            listView.setAdapter(
                    new MyAdapter(this, android.R.layout.simple_list_item_1, mActivities));
            listView.setOnItemClickListener(this);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private class MyAdapter extends ArrayAdapter<ActivityInfo> {
        private Context context;
        private int resourceId;

        MyAdapter(Context context, int resourceId, List<ActivityInfo> activities) {
            super(context, resourceId, activities);
            this.context = context;
            this.resourceId = resourceId;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(resourceId, parent, false);
            String name = getItem(position).name;
            String[] spls = name.split("\\.");
            if (spls.length > 0) ((TextView) convertView).setText(spls[spls.length - 1]);
            return convertView;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClassName(this, mActivities.get(position).name);
        startActivity(intent);
    }

}
