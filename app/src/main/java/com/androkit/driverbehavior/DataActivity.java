package com.androkit.driverbehavior;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class DataActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Button btnSimpan = findViewById(R.id.btn_simpan);

        btnSimpan.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_simpan) {
            Intent intent = new Intent(DataActivity.this, DetectActivity.class);
            startActivity(intent);
            finish();
        }
    }
}