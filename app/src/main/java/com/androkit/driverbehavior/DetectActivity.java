package com.androkit.driverbehavior;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import com.androkit.driverbehavior.ml.AbnormalDriving;

import org.checkerframework.checker.units.qual.A;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

public class DetectActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_NOTIF = "extra_notif";
    public static final int NOTIF = 1;
    public static final int STOP = 2;

    private boolean notif;

    private TextView tvZigZag;
    private TextView tvSleepy;
    private TextView tvBraking;
    private TextView tvAcceleration;

    private boolean started = false;

    ArrayList<Float> ax = new ArrayList<>();
    ArrayList<Float> ay = new ArrayList<>();
    ArrayList<Float> az = new ArrayList<>();
    ArrayList<Float> gx = new ArrayList<>();
    ArrayList<Float> gy = new ArrayList<>();
    ArrayList<Float> gz = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {

                } else {

                }
            });

    Intent foregroundServiceIntent;

    private boolean boundStatus = false;
    private DetectService detectService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            DetectService.DetectBinder detectBinder = (DetectService.DetectBinder) service;
            detectService = detectBinder.getService;
            boundStatus = true;
            getValueFromService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            boundStatus = false;
        }
    };

    private void getValueFromService() {
        final Observer<Integer> zigzagObserver = zigzag -> {
            tvZigZag.setText(String.valueOf(zigzag));
        };
        detectService.zigzagLiveData.observe(this, zigzagObserver);

        final Observer<Integer> sleepyObserver = sleepy -> {
            tvSleepy.setText(String.valueOf(sleepy));
        };
        detectService.sleepyLiveData.observe(this, sleepyObserver);

        final Observer<Integer> brakingObserver = braking -> {
            tvBraking.setText(String.valueOf(braking));
        };
        detectService.brakingLiveData.observe(this, brakingObserver);

        final Observer<Integer> accleerationObserver = acceleration -> {
            tvAcceleration.setText(String.valueOf(acceleration));
        };
        detectService.accelerationLiveData.observe(this, accleerationObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (boundStatus) {
            unbindService(connection);
            boundStatus = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
        }
        else {
            requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS);
        }

        notif = getIntent().getBooleanExtra(EXTRA_NOTIF, false);
        if (notif) showDialogFragment(NOTIF);

        tvZigZag = findViewById(R.id.tv_zigzag_2);
        tvSleepy = findViewById(R.id.tv_sleepy_2);
        tvBraking = findViewById(R.id.tv_sudden_breaking_2);
        tvAcceleration = findViewById(R.id.tv_sudden_acceleration_2);

        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);

        foregroundServiceIntent = new Intent(this, DetectService.class);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void showDialogFragment(int from) {
        DialogFragment mDialogFragment = new DialogFragment();
        mDialogFragment.setFrom(from);

        FragmentManager mFragmentManager = getSupportFragmentManager();
        mDialogFragment.show(mFragmentManager, DialogFragment.class.getSimpleName());
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_start) {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(foregroundServiceIntent);
            } else {
                startService(foregroundServiceIntent);
            }
            bindService(foregroundServiceIntent, connection, BIND_AUTO_CREATE);
            started = true;
        }
        if (view.getId() == R.id.btn_stop) {
            stopService(foregroundServiceIntent);
            if (!notif)
                showDialogFragment(STOP);
            started = false;
        }
    }
}