package com.leinardi.androidthings.kuman.sm9.common;

import android.support.annotation.NonNull;

import timber.log.Timber;

public class AwesomeDebugTree extends Timber.DebugTree {
    @Override
    protected String createStackElementTag(@NonNull StackTraceElement element) {
        return String.format("(%s:%s)", element.getFileName(), element.getLineNumber());
    }
}
