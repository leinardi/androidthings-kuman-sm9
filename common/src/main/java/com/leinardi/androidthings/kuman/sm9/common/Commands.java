package com.leinardi.androidthings.kuman.sm9.common;

public class Commands {
    public static final long MOVE_STOP = 1;
    public static final long MOVE_FORWARD = 1 << 1;
    public static final long MOVE_BACKWORD = 1 << 2;
    public static final long MOVE_LEFT = 1 << 3;
    public static final long MOVE_RIGHT = 1 << 4;

    private Commands() {
    }
}
