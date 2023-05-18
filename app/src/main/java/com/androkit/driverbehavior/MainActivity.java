package com.androkit.driverbehavior;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Sensor sensorAcc;
    private Sensor sensorGyro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivCeklisAcc = findViewById(R.id.iv_ceklis_acc);
        ImageView ivCeklisGyroscope = findViewById(R.id.iv_ceklis_gyroscope);
        Button btnNext = findViewById(R.id.btn_next);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (sensorAcc != null) {
            ivCeklisAcc.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.checklist));
        }

        if (sensorGyro != null) {
            ivCeklisGyroscope.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.checklist));
        }

        btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_next) {
            if (sensorAcc == null || sensorGyro == null) {
                Toast.makeText(this, "Sensor not Available", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(MainActivity.this, DataActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}