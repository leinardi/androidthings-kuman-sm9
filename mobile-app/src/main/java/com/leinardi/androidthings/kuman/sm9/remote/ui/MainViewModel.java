package com.leinardi.androidthings.kuman.sm9.remote.ui;

import com.erz.joysticklibrary.JoyStick;
import com.leinardi.androidthings.kuman.sm9.common.ui.BaseViewModel;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository;
import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.NearbyConnectionsStatusUpdate;
import io.reactivex.observers.DisposableObserver;
import timber.log.Timber;

import javax.inject.Inject;

public class MainViewModel extends BaseViewModel<MainViewModelObservable> {
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

    public void setPermissionsGranted(boolean permissionsGranted) {
        if (permissionsGranted && !getObservable().getPermissionsGranted()) {
            mGoogleApiClientRepository.connectGoogleApiClient();
        }
        getObservable().setPermissionsGranted(permissionsGranted);
    }

    public JoyStick.JoyStickListener getJoyStickListener() {
        return new JoyStick.JoyStickListener() {
            int mDirection = Integer.MIN_VALUE;

            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {
                if (direction != mDirection) {
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
