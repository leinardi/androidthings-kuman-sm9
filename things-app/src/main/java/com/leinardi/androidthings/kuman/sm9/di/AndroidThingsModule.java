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

import android.support.annotation.Nullable;

import com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1;
import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import com.leinardi.androidthings.kuman.sm9.controller.MotorServoBoardDriverController;
import com.leinardi.androidthings.kuman.sm9.controller.PwrA53AMotorServoBoardDriverController;
import com.leinardi.androidthings.kuman.sm9.controller.oled.OledDisplayDriverController;
import com.leinardi.androidthings.kuman.sm9.controller.oled.OledDisplayHelper;
import com.leinardi.androidthings.kuman.sm9.controller.oled.Sh1106OledDisplayDriverController;
import com.leinardi.androidthings.kuman.sm9.controller.sensor.Lsm9ds1SensorDriverController;
import com.leinardi.androidthings.kuman.sm9.controller.sensor.SensorDriverController;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AndroidThingsModule {
    @Singleton
    @Provides
    MotorServoBoardDriverController provideMotorServoBoardController(@Nullable PwrA53A pwrA53A) {
        PwrA53AMotorServoBoardDriverController controller = new PwrA53AMotorServoBoardDriverController();
        controller.setDriver(pwrA53A);
        return controller;
    }

    @Singleton
    @Provides
    SensorDriverController provideSensorDriverController(@Nullable Lsm9ds1 lsm9ds1) {
        Lsm9ds1SensorDriverController controller = new Lsm9ds1SensorDriverController();
        controller.setDriver(lsm9ds1);
        return controller;
    }

    @Singleton
    @Provides
    OledDisplayDriverController provideOledDisplayController(@Nullable Sh1106 sh1106,
                                                             OledDisplayHelper oledDisplayHelper) {
        Sh1106OledDisplayDriverController controller = new Sh1106OledDisplayDriverController(oledDisplayHelper);
        controller.setDriver(sh1106);
        return controller;
    }
}
