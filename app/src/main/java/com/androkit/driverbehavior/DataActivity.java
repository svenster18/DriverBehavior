package com.androkit.driverbehavior;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference driverRef;
    EditText edNama;
    EditText edNoTelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Button btnSimpan = findViewById(R.id.btn_simpan);
        edNama = findViewById(R.id.edt_nama_pengguna);
        edNoTelp = findViewById(R.id.edt_no_telepon);

        FirebaseDatabase db = FirebaseDatabase.getInstance("https://driver-behavior-5f3db-default-rtdb.asia-southeast1.firebasedatabase.app");
        driverRef = db.getReference().child("bike").child("driver");

        btnSimpan.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_simpan) {
            String name = edNama.getText().toString();
            String phone = edNoTelp.getText().toString();

            if (name.isEmpty()) {
                edNama.setError("Name cannot be empty");
                return;
            }
            if (phone.isEmpty()) {
                edNoTelp.setError("Phone cannot be empty");
                return;
            }

            Driver driver = new Driver(name);
            String id = driver.name.toLowerCase().replace(" ", "-");
            driverRef.child(id).setValue(driver)
                    .addOnSuccessListener(unused -> Toast.makeText(getApplication(), "Driver Data saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getApplication(), "Driver Data failed to save", Toast.LENGTH_SHORT).show());

            Intent intent = new Intent(DataActivity.this, DetectActivity.class);
            intent.putExtra(DetectActivity.EXTRA_ID, id);
            startActivity(intent);
            finish();
        }
    }
}