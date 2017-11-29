package com.leinardi.androidthings.kuman.sm9.common.api;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseRepository {
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public void clear() {
        getCompositeDisposable().clear();
    }
}
