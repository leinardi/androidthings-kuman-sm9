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
