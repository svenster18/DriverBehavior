package com.androkit.driverbehavior;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Detection {
    public boolean normal = false;

    public Detection() {
    }

    public Detection(int zigZag, int sleepy, int suddenBraking, int suddenAcceleration) {
        if(zigZag == 0 && sleepy == 0  && suddenBraking == 0 && suddenAcceleration == 0) {
            normal = true;
        }
    }
}
