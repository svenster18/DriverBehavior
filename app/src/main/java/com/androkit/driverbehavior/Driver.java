package com.androkit.driverbehavior;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver {
    public String name;
    public String phone;

    public Driver() {
    }

    public Driver(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}
