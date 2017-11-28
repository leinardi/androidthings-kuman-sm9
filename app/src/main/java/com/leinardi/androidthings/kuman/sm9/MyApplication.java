package com.leinardi.androidthings.kuman.sm9;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by leinardi on 28/11/17.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new DebugTree());
    }

    private static class DebugTree extends Timber.DebugTree {
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return String.format("(%s:%s)", element.getFileName(), element.getLineNumber());
        }
    }

}
