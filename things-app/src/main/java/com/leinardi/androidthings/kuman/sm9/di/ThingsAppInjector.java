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

package com.leinardi.androidthings.kuman.sm9.di;

import com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1;
import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import com.leinardi.androidthings.kuman.sm9.ThingsApp;
import com.leinardi.androidthings.kuman.sm9.common.di.AppInjector;
import timber.log.Timber;

import java.io.IOException;

import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_14_9HZ;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.AccelGyroOutputDataRate.ODR_POWER_DOWN;
import static com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1.MagnetometerSystemOperatingMode.MAG_POWER_DOWN;

public class ThingsAppInjector extends AppInjector<ThingsApp> {
    private static final ThingsAppInjector INSTANCE = new ThingsAppInjector();
    private static final String RPI3_I2C_BUS_NAME = "I2C1";

    private ThingsAppInjector() {
    }

    public static ThingsAppInjector getInstance() {
        return INSTANCE;
    }

    @Override
    protected void injectApplication(ThingsApp application) {
        PwrA53A pwrA53A = null;
        try {
            pwrA53A = new PwrA53A(RPI3_I2C_BUS_NAME);
        } catch (IOException e) {
            Timber.e(e);
        }

        Sh1106 sh1106 = null;
        try {
            sh1106 = new Sh1106(RPI3_I2C_BUS_NAME);
        } catch (IOException e) {
            Timber.e(e);
        }

        Lsm9ds1 lsm9ds1 = null;
        try {
            lsm9ds1 = new Lsm9ds1.Builder(RPI3_I2C_BUS_NAME)
                    .setGyroscopeOdr(ODR_POWER_DOWN)
                    .setAccelerometerOdr(ODR_14_9HZ)
                    .setMagnetometerSystemOperatingMode(MAG_POWER_DOWN)
                    .build();
        } catch (IOException e) {
            Timber.e(e);
        }

        DaggerAppComponent.builder()
                .application(application)
                .pwra53a(pwrA53A)
                .sh1106(sh1106)
                .lsm9ds1(lsm9ds1)
                .build()
                .inject(application);
    }
}
