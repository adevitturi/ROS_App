package com.github.ros.android.helloros;

import java.io.Serializable;

public class TargetLocation {
    private static final String SEPARATOR = "@";
    private final String name;
    private final String x;
    private final String y;
    private final String z;

    public TargetLocation(String name, String x, String y, String z) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public TargetLocation(String[] params) {
        this(params[0], params[1], params[2], params[3]);
    }

    public TargetLocation(String bundle) {
        this(bundle.split(SEPARATOR));
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getZ() {
        return z;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name + SEPARATOR + x + SEPARATOR + y + SEPARATOR + z;
    }
}
