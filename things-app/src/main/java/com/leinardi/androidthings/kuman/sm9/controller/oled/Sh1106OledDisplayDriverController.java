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

package com.leinardi.androidthings.kuman.sm9.controller.oled;

import com.leinardi.androidthings.driver.sh1106.BitmapHelper;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import timber.log.Timber;

import java.io.IOException;

public class Sh1106OledDisplayDriverController extends OledDisplayDriverController<Sh1106> {
    private OledDisplayHelper mOledDisplayHelper;

    public Sh1106OledDisplayDriverController(OledDisplayHelper oledDisplayHelper) {
        mOledDisplayHelper = oledDisplayHelper;
    }

    @Override
    public void refreshDisplay() {
        if (isHardwareAvailable()) {
            try {
                getDriver().clearPixels();
                BitmapHelper.setBmpData(getDriver(), 0, 0, mOledDisplayHelper.getDisplayBitmap(), false);
                getDriver().show(); // render the pixel data
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void setPing(int ping) {
        mOledDisplayHelper.setPing(ping);
    }

    @Override
    public void setContrast(int level) {
        if (isHardwareAvailable()) {
            try {
                getDriver().setContrast(level);
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void invertDisplay(boolean invert) {
        if (isHardwareAvailable()) {
            try {
                getDriver().setInvertDisplay(invert);
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }
}
