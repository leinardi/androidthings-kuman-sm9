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

import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import com.leinardi.androidthings.kuman.sm9.controller.MotorServoBoardController;
import com.leinardi.androidthings.kuman.sm9.controller.OledDisplayController;
import com.leinardi.androidthings.kuman.sm9.controller.PwrA53AMotorServoBoardController;
import com.leinardi.androidthings.kuman.sm9.controller.Sh1106OledDisplayController;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AndroidThingsModule {
    @Singleton
    @Provides
    MotorServoBoardController provideGoogleApiClientRepository(@Nullable PwrA53A pwrA53A) {
        return new PwrA53AMotorServoBoardController(pwrA53A);
    }

    @Singleton
    @Provides
    OledDisplayController provideOledDisplayController(@Nullable Sh1106 sh1106) {
        return new Sh1106OledDisplayController(sh1106);
    }
}
