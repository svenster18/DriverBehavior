package com.androkit.driverbehavior;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase db;
    DatabaseReference driverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Button btnSimpan = findViewById(R.id.btn_simpan);

        db = FirebaseDatabase.getInstance("https://driver-behavior-5f3db-default-rtdb.asia-southeast1.firebasedatabase.app");
        driverRef = db.getReference().child("car").child("driver");

        btnSimpan.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_simpan) {
            Driver driver = new Driver("Corel Athia", "081234567890");
            String id = driver.name.toLowerCase().replace(" ", "-");
            driverRef.child(id).setValue(driver)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplication(), "Driver Data saved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplication(), "Driver Data failed to save", Toast.LENGTH_SHORT).show();
                        }
                    });

            Intent intent = new Intent(DataActivity.this, DetectActivity.class);
            intent.putExtra(DetectActivity.EXTRA_ID, id);
            startActivity(intent);
            finish();
        }
    }
}