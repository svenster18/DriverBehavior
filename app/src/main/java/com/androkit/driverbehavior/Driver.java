package com.androkit.driverbehavior;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver {
    public final String name;

    public Driver(String name) {
        this.name = name;
    }
}
