package com.leinardi.androidthings.kuman.sm9.di;

import com.leinardi.androidthings.kuman.sm9.ThingsApp;
import com.leinardi.androidthings.kuman.sm9.common.di.AppInjector;

public class ThingsAppInjector extends AppInjector<ThingsApp> {
    private static final ThingsAppInjector INSTANCE = new ThingsAppInjector();

    private ThingsAppInjector() {
    }

    public static ThingsAppInjector getInstance() {
        return INSTANCE;
    }

    @Override
    protected void injectApplication(ThingsApp application) {
        DaggerAppComponent.builder().application(application).build().inject(application);
    }
}
