/*
 * Copyright $today.yea Roberto Leinardi.
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
import com.leinardi.androidthings.kuman.sm9.common.ui.BaseViewModel;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.NearbyConnectionsStatusUpdate;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

import javax.inject.Inject;

public class MainViewModel extends BaseViewModel<MainViewModelObservable> {
    private static final int POWER_THRESHOLD = 33; // 33 percent
    private GoogleApiClientRepository mGoogleApiClientRepository;

    @Inject
    MainViewModel(GoogleApiClientRepository googleApiClientRepository) {
        mGoogleApiClientRepository = googleApiClientRepository;

        mGoogleApiClientRepository.subscribeToConnectionStatusUpdate(new DisposableObserver<NearbyConnectionsStatusUpdate>() {

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

    public JoyStick.JoyStickListener getJoyStickListener() {
        return new JoyStick.JoyStickListener() {
            int mDirection = Integer.MIN_VALUE;

            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                if (direction != mDirection && (direction == JoyStick.DIRECTION_CENTER || power > POWER_THRESHOLD)) {
                    mGoogleApiClientRepository.sendMessage("Direction " + direction);
                    mDirection = direction;
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
