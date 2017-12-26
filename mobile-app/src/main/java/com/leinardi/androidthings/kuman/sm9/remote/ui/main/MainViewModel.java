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

import com.erz.joysticklibrary.JoyStick;
import com.leinardi.androidthings.kuman.sm9.common.api.car.CameraCradlePosition;
import com.leinardi.androidthings.kuman.sm9.common.api.car.CarMovement;
import com.leinardi.androidthings.kuman.sm9.common.api.car.ThingsMessage;
import com.leinardi.androidthings.kuman.sm9.common.ui.BaseViewModel;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.NearbyConnectionsStatusUpdate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class MainViewModel extends BaseViewModel<MainViewModelObservable> {
    private static final int ANGLE_STEP = 10;
    private static final int POWER_STEP = 15;
    private static final int SAMPLE_PERIOD_IN_MILLIS = 50;
    private final PublishSubject<ThingsMessage> mCarThingsMessagePublishSubject;
    private final PublishSubject<ThingsMessage> mCameraThingsMessagePublishSubject;
    private GoogleApiClientRepository mGoogleApiClientRepository;

    @Inject
    MainViewModel(GoogleApiClientRepository googleApiClientRepository) {
        mGoogleApiClientRepository = googleApiClientRepository;

        mGoogleApiClientRepository.subscribeToConnectionStatusUpdate(
                new DisposableObserver<NearbyConnectionsStatusUpdate>() {

                    @Override
                    public void onNext(NearbyConnectionsStatusUpdate payload) {
                        getObservable().setConnectionStatus(payload.getConnectionStatus());
                        getObservable().setConnectionInfo(payload.getConnectionInfo());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

        mCarThingsMessagePublishSubject = PublishSubject.create();
        mCarThingsMessagePublishSubject.sample(SAMPLE_PERIOD_IN_MILLIS, TimeUnit.MILLISECONDS)
                .subscribe(message -> mGoogleApiClientRepository.sendMessage(message), Timber::e);
        mCameraThingsMessagePublishSubject = PublishSubject.create();
        mCameraThingsMessagePublishSubject.sample(SAMPLE_PERIOD_IN_MILLIS, TimeUnit.MILLISECONDS)
                .subscribe(message -> mGoogleApiClientRepository.sendMessage(message), Timber::e);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mGoogleApiClientRepository.clear();
    }

    public void onReconnectClicked() {
        mGoogleApiClientRepository.connect();
    }

    public void setPermissionsGranted(boolean permissionsGranted) {
        if (permissionsGranted && !getObservable().getPermissionsGranted()) {
            mGoogleApiClientRepository.connect();
        }
        getObservable().setPermissionsGranted(permissionsGranted);
    }

    public JoyStick.JoyStickListener getCarJoyStickListener() {
        return new JoyStick.JoyStickListener() {
            int mSteppedAngle = Integer.MIN_VALUE;
            int mSteppedPower = Integer.MIN_VALUE;

            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                int steppedAngle = (int) (Math.round(Math.toDegrees(angle) / ANGLE_STEP) * ANGLE_STEP);
                int steppedPower = (int) (Math.round(power / POWER_STEP) * POWER_STEP);
                if (steppedAngle != mSteppedAngle
                        || steppedPower != mSteppedPower
                        || direction == JoyStick.DIRECTION_CENTER) {
                    Timber.d("angle = %d (%f), power = %d", steppedAngle, angle, steppedPower);
                    ThingsMessage message = new ThingsMessage.Builder()
                            .setCarMovement(new CarMovement(steppedAngle, steppedPower))
                            .build();
                    mCarThingsMessagePublishSubject.onNext(message);
                    mSteppedAngle = steppedAngle;
                    mSteppedPower = steppedPower;
                }
            }

            @Override
            public void onTap() {
            }

            @Override
            public void onDoubleTap() {
            }
        };
    }

    public JoyStick.JoyStickListener getCameraJoyStickListener() {
        return new JoyStick.JoyStickListener() {
            int mVerticalAngle = Integer.MIN_VALUE;
            int mHorizontalAngle = Integer.MIN_VALUE;

            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {

                int verticalAngle =
                        (int) Math.round((Math.sin(angle) * power / 100 * CameraCradlePosition.MAX_ANGLE)
                                / ANGLE_STEP) * ANGLE_STEP;
                int horizontalAngle =
                        (int) Math.round((Math.cos(angle) * power / 100 * CameraCradlePosition.MAX_ANGLE * -1)
                                / ANGLE_STEP) * ANGLE_STEP;
                if (mVerticalAngle != horizontalAngle || mHorizontalAngle != verticalAngle) {
                    Timber.d("verticalAngle = %d, horizontalAngle = %d", verticalAngle, horizontalAngle);
                    ThingsMessage message = new ThingsMessage.Builder().setCameraCradlePosition(
                            new CameraCradlePosition(horizontalAngle, verticalAngle)).build();
                    mCameraThingsMessagePublishSubject.onNext(message);
                    mVerticalAngle = horizontalAngle;
                    mHorizontalAngle = verticalAngle;
                }
            }

            @Override
            public void onTap() {

            }

            @Override
            public void onDoubleTap() {

            }
        };
    }
}
