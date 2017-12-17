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

package com.leinardi.androidthings.kuman.sm9.ui;

import android.app.Application;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.leinardi.androidthings.kuman.sm9.R;
import com.leinardi.androidthings.kuman.sm9.common.di.Injectable;
import com.leinardi.androidthings.kuman.sm9.common.ui.BaseActivity;
import com.leinardi.androidthings.kuman.sm9.databinding.MainActivityBinding;
import com.leinardi.androidthings.kuman.sm9.service.CarService;
import timber.log.Timber;

import javax.inject.Inject;

public class MainActivity extends BaseActivity<MainViewModel> implements Injectable {

    @Inject
    Application mApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate");
        MainActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        binding.setViewModel(getViewModel());

        startCarService();
    }

    private void startCarService() {
        Intent startIntent = new Intent(mApplication, CarService.class);
        startIntent.setAction(CarService.ACTION_START_CAR_SERVICE);
        ContextCompat.startForegroundService(mApplication, startIntent);
    }

    private void stopCarService() {
        Intent stopIntent = new Intent(mApplication, CarService.class);
        stopIntent.setAction(CarService.ACTION_STOP_CAR_SERVICE);
        ContextCompat.startForegroundService(mApplication, stopIntent);
    }

    @Override
    protected void onViewModelPropertyChanged(Observable observable, int propertyId) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            stopCarService();
        }
    }
}
