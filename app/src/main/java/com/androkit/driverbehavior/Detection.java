package com.androkit.driverbehavior;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Detection {
    public int zigZag = 0;
    public int sleepy = 0;
    public int suddenBraking = 0;
    public int suddenAcceleration = 0;
    public boolean normal = false;

    public Detection() {
    }

    public Detection(int zigZag, int sleepy, int suddenBraking, int suddenAcceleration) {
        this.zigZag = zigZag;
        this.sleepy = sleepy;
        this.suddenBraking = suddenBraking;
        this.suddenAcceleration = suddenAcceleration;
        if(zigZag < 7 && sleepy < 7  && suddenBraking < 7 && suddenAcceleration < 7) {
            normal = true;
        }
    }
}
