package com.androkit.driverbehavior;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class PointAdapter extends FirebaseRecyclerAdapter<Detection, PointAdapter.PointViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     */
    public PointAdapter(@NonNull FirebaseRecyclerOptions<Detection> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PointAdapter.PointViewHolder holder, int position, @NonNull Detection model) {
        if (!model.normal) holder.itemView.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public PointAdapter.PointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_points, parent, false);
        return new PointViewHolder(view);
    }

    public static class PointViewHolder extends RecyclerView.ViewHolder {

        public PointViewHolder(View view) {
            super(view);
        }
    }
}
