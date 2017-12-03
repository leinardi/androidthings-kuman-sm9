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

package com.leinardi.androidthings.kuman.sm9.common.ui;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.Observable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;

public abstract class BaseActivity<VM extends BaseViewModel> extends AppCompatActivity {
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private VM mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreate();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        onCreate();
    }

    @SuppressWarnings("unchecked")
    private void onCreate() {
        if (mViewModel == null) {
            Class<VM> vmClass = (Class<VM>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(vmClass);
            mViewModel.getObservable().addOnPropertyChangedCallback(getOnViewModelPropertyChangedCallback());
        }
    }

    protected abstract void onViewModelPropertyChanged(Observable observable, int propertyId);

    @NonNull
    protected Observable.OnPropertyChangedCallback mOnViewModelPropertyChangedCallback = new Observable.OnPropertyChangedCallback() {
        @Override
        public void onPropertyChanged(Observable observable, int propertyId) {
            onViewModelPropertyChanged(observable, propertyId);
        }
    };

    @NonNull
    protected Observable.OnPropertyChangedCallback getOnViewModelPropertyChangedCallback() {
        return mOnViewModelPropertyChangedCallback;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mViewModel != null) {
            mViewModel.getObservable().removeOnPropertyChangedCallback(getOnViewModelPropertyChangedCallback());
        }
    }

    public VM getViewModel() {
        return mViewModel;
    }
}
