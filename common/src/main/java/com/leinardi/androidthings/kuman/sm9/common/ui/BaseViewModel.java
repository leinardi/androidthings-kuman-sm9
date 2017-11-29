package com.leinardi.androidthings.kuman.sm9.common.ui;

import android.arch.lifecycle.ViewModel;
import android.databinding.BaseObservable;

import javax.inject.Inject;

public class BaseViewModel<VMO extends BaseObservable> extends ViewModel {
    private VMO mObservable;

    @Inject
    void inject(VMO observable) {
        mObservable = observable;
    }

    public VMO getObservable() {
        return mObservable;
    }
}
