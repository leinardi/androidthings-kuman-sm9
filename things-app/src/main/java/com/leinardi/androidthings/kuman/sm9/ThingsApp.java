package com.leinardi.androidthings.kuman.sm9;

import android.app.Activity;
import android.app.Application;

import com.leinardi.androidthings.kuman.sm9.common.AwesomeDebugTree;
import com.leinardi.androidthings.kuman.sm9.di.ThingsAppInjector;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import timber.log.Timber;

import javax.inject.Inject;

public class ThingsApp extends Application implements HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> mDispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new AwesomeDebugTree());
        }
        ThingsAppInjector.getInstance().init(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return mDispatchingAndroidInjector;
    }
}
