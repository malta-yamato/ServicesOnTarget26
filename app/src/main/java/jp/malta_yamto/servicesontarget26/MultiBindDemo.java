package jp.malta_yamto.servicesontarget26;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import jp.malta_yamto.servicesontarget26.service.MultiBindClientA;
import jp.malta_yamto.servicesontarget26.service.MultiBindClientB;
import jp.malta_yamto.servicesontarget26.service.MultiBindClientC;

public class MultiBindDemo extends AppCompatActivity {
    private static final String TAG = "MultiBindDemo";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multi_bind_demo_main);

    }

    public void onStartClick(View view) {
        startA();
        startB();
        startC();
        view.setEnabled(false);
    }

    private void startA() {
        Log.d(TAG, "startA: ");
        Intent intent = new Intent(this, MultiBindClientA.class);
        startService(intent);
    }

    private void startB() {
        Log.d(TAG, "startB: ");
        Intent intent = new Intent(this, MultiBindClientB.class);
        startService(intent);
    }

    private void startC() {
        Log.d(TAG, "startC: ");
        Intent intent = new Intent(this, MultiBindClientC.class);
        startService(intent);
    }

}
