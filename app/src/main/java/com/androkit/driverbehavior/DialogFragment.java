package com.androkit.driverbehavior;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class DialogFragment extends androidx.fragment.app.DialogFragment implements View.OnClickListener {

    private int from;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        TextView tvMessage = view.findViewById(R.id.tv_dialog_message);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        if (getFrom() == DetectActivity.STOP) {
            tvTitle.setText("Reward");
            tvTitle.setTextColor(Objects.requireNonNull(getActivity()).getColor(R.color.white));
            tvMessage.setText("Yeayy!! congrats, you got 100 points. Keep driving carefully :)");
            tvMessage.setTextColor(getActivity().getColor(R.color.navy));
            btnConfirm.setText("Back");
            btnConfirm.setTextColor(requireActivity().getColor(R.color.white));
            btnConfirm.getBackground().setTint(requireActivity().getColor(R.color.gray));
        }

        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            dismiss();
        }
    }
}