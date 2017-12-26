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

package com.leinardi.androidthings.kuman.sm9.controller;

import android.support.annotation.Nullable;

import timber.log.Timber;

import javax.inject.Inject;

public abstract class BaseDriverController<D extends AutoCloseable> {

    private D mDriver;

    public D getDriver() {
        return mDriver;
    }

    @Inject
    public void setDriver(@Nullable D driver) {
        mDriver = driver;
    }

    public boolean isHardwareAvailable() {
        return mDriver != null;
    }

    protected void close() {
        if (isHardwareAvailable()) {
            try {
                mDriver.close();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }
}
