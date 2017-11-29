package com.leinardi.androidthings.kuman.sm9.ui;

import com.leinardi.androidthings.kuman.sm9.api.GoogleApiClientRepository;

import javax.inject.Inject;

public class MainController {
    private GoogleApiClientRepository mGoogleApiClientRepository;
    //    private PwrA53A mPwrA53A;

    @Inject
    public MainController(GoogleApiClientRepository googleApiClientRepository) {
        mGoogleApiClientRepository = googleApiClientRepository;

        //        try {
        //            mPwrA53A = new PwrA53A();
        //        } catch (IOException e) {
        //            Timber.e(e);
        //        }

    }

    public void clear() {
        mGoogleApiClientRepository.clear();
    }

}
