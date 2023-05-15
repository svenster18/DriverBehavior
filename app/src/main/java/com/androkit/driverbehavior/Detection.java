package com.androkit.driverbehavior;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Detection {
    public int zigZag;
    public int sleepy;
    public int suddenBraking;
    public int suddenAcceleration;
    public boolean normal = false;

    public Detection() {
    }

    public Detection(int zigZag, int sleepy, int suddenBraking, int suddenAcceleration) {
        this.zigZag = zigZag;
        this.sleepy = sleepy;
        this.suddenBraking = suddenBraking;
        this.suddenAcceleration = suddenAcceleration;
        if(zigZag == 0 && sleepy == 0  && suddenBraking == 0 && suddenAcceleration == 0) {
            normal = true;
        }
    }
}
