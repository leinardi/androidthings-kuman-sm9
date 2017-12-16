/*
 * Copyright 2017 Roberto Leinardi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leinardi.androidthings.kuman.sm9.remote.ui.main;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.erz.joysticklibrary.JoyStick;
import com.leinardi.androidthings.kuman.sm9.remote.BR;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository;

import javax.inject.Inject;

public class MainViewModelObservable extends BaseObservable {

    private boolean mPermissionsGranted;
    private CharSequence mConnectionInfo;
    @GoogleApiClientRepository.ConnectionStatus
    private int mConnectionStatus;
    private int mCarJoystickType = JoyStick.TYPE_8_AXIS;
    private int mCameraJoystickType = JoyStick.TYPE_8_AXIS;

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

    @Bindable
    public int getCarJoystickType() {
        return mCarJoystickType;
    }

    public void setCarJoystickType(int carJoystickType) {
        mCarJoystickType = carJoystickType;
        notifyPropertyChanged(BR.carJoystickType);
    }

    @Bindable
    public int getCameraJoystickType() {
        return mCameraJoystickType;
    }

    public void setCameraJoystickType(int cameraJoystickType) {
        mCameraJoystickType = cameraJoystickType;
        notifyPropertyChanged(BR.cameraJoystickType);
    }
}
