package com.leinardi.androidthings.kuman.sm9.remote.di;

import com.leinardi.androidthings.kuman.sm9.common.di.AppInjector;
import com.leinardi.androidthings.kuman.sm9.remote.MobileApp;

public class MobileAppInjector extends AppInjector<MobileApp> {
    private static final MobileAppInjector INSTANCE = new MobileAppInjector();

    private MobileAppInjector() {
    }

    public static MobileAppInjector getInstance() {
        return INSTANCE;
    }

    @Override
    protected void injectApplication(MobileApp application) {
        DaggerAppComponent.builder().application(application).build().inject(application);
    }
}
