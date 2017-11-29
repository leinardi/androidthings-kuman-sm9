package com.leinardi.androidthings.kuman.sm9.remote.ui;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.IntDef;

import com.leinardi.androidthings.kuman.sm9.remote.BR;
import com.leinardi.androidthings.kuman.sm9.remote.R;

import javax.inject.Inject;

import static com.leinardi.androidthings.kuman.sm9.remote.ui.MainViewModelObservable.ConnectionStatus.CONNECTED;
import static com.leinardi.androidthings.kuman.sm9.remote.ui.MainViewModelObservable.ConnectionStatus.CONNECTING;
import static com.leinardi.androidthings.kuman.sm9.remote.ui.MainViewModelObservable.ConnectionStatus.DISCONNECTED;

public class MainViewModelObservable extends BaseObservable {

    private boolean mPermissionsGranted;
    private CharSequence mConnectionInfo;
    @ConnectionStatus
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

    @IntDef({DISCONNECTED, CONNECTING, CONNECTED})
    public @interface ConnectionStatus {
        int DISCONNECTED = R.drawable.ic_disconnected;
        int CONNECTING = R.drawable.ic_connecting;
        int CONNECTED = R.drawable.ic_connected;
    }

}
