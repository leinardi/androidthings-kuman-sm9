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

package com.leinardi.androidthings.kuman.sm9.controller.sensor;

import com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1;
import timber.log.Timber;

import java.io.IOException;

public class Lsm9ds1SensorDriverController extends SensorDriverController<Lsm9ds1> {
    @Override
    public Float getTemperature() {
        if (isHardwareAvailable()) {
            try {
                return getDriver().readTemperature();
            } catch (IOException e) {
                Timber.e(e);
            }
        }
        return null;
    }
}
