package com.androkit.driverbehavior;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class PointActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference detectionRef;
    private PointAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        TextView tvPoints = findViewById(R.id.tv_points);
        RecyclerView rvPoints = findViewById(R.id.rv_points);
        Button btnBack = findViewById(R.id.btn_back);

        String id = getIntent().getStringExtra(DetectActivity.EXTRA_ID);

        FirebaseDatabase db = FirebaseDatabase.getInstance("https://driver-behavior-5f3db-default-rtdb.asia-southeast1.firebasedatabase.app");
        detectionRef = db.getReference().child("bike").child("detection").child(id);
        detectionRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                int totalNormal = 0;
                HashMap<String, HashMap<String, Object>> detections = (HashMap<String, HashMap<String, Object>>) task.getResult().getValue();
                Log.d("PointActivity", detections.toString());
                for (HashMap<String, Object> det: detections.values()) {
                    boolean normal = (boolean) det.get("normal");
                    if (normal) totalNormal++;
                }
                int totalPoints = 100 * totalNormal;
                tvPoints.setText(String.valueOf(totalPoints));
            }
        });

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvPoints.setLayoutManager(manager);

        FirebaseRecyclerOptions<Detection> options = new FirebaseRecyclerOptions.Builder<Detection>()
                .setQuery(detectionRef, Detection.class)
                .build();
        adapter = new PointAdapter(options);

        rvPoints.setAdapter(adapter);

        btnBack.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.startListening();
    }

    @Override
    protected void onPause() {
        adapter.stopListening();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) finish();
    }
}