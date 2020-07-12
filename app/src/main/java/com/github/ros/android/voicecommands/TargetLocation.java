package com.github.ros.android.voicecommands;

/** A class that represents a physical location. */
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

    /** Gets x coordinate. */
    public String getX() {
        return x;
    }

    /** Gets y coordinate. */
    public String getY() {
        return y;
    }

    /** Gets z coordinate. */
    public String getZ() {
        return z;
    }

    /** Gets the lcoation's name. */
    public String getName() {
        return name;
    }

    /** Serializes the location to a string. */
    public String toString() {
        return name + SEPARATOR + x + SEPARATOR + y + SEPARATOR + z;
    }
}
