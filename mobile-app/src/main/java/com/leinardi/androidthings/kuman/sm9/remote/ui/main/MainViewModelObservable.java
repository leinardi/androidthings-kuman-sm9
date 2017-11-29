package com.leinardi.androidthings.kuman.sm9.remote.ui.main;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.leinardi.androidthings.kuman.sm9.remote.BR;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository;

import javax.inject.Inject;

public class MainViewModelObservable extends BaseObservable {

    private boolean mPermissionsGranted;
    private CharSequence mConnectionInfo;
    @GoogleApiClientRepository.ConnectionStatus
    private int mConnectionStatus;

    @Inject
    public MainViewModelObservable() {
    }

    @Bindable
    public boolean getPermissionsGranted() {
        return mPermissionsGranted;
    }

    public void setPermissionsGranted(boolean permissionsGranted) {
        mPermissionsGranted = permissionsGranted;
        notifyPropertyChanged(BR.permissionsGranted);
    }

    @Bindable
    public CharSequence getConnectionInfo() {
        return mConnectionInfo;
    }

    public void setConnectionInfo(CharSequence connectionInfo) {
        mConnectionInfo = connectionInfo;
        notifyPropertyChanged(BR.connectionInfo);
    }

    @Bindable
    public int getConnectionStatus() {
        return mConnectionStatus;
    }

    public void setConnectionStatus(int connectionStatus) {
        mConnectionStatus = connectionStatus;
        notifyPropertyChanged(BR.connectionStatus);
    }

}
