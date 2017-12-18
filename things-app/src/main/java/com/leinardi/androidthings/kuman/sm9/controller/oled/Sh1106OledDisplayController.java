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

import android.support.annotation.Nullable;

import com.leinardi.androidthings.driver.sh1106.BitmapHelper;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import timber.log.Timber;

import java.io.IOException;

public class Sh1106OledDisplayController implements OledDisplayController {
    private Sh1106 mSh1106;
    private OledDisplayHelper mOledDisplayHelper;

    public Sh1106OledDisplayController(@Nullable Sh1106 sh1106, OledDisplayHelper oledDisplayHelper) {
        mOledDisplayHelper = oledDisplayHelper;
        if (sh1106 == null) {
            throw new IllegalStateException("Unable to get the instance of Sh1106");
        }
        mSh1106 = sh1106;
    }

    @Override
    public void refreshDisplay() {
        try {
            mSh1106.clearPixels();
            BitmapHelper.setBmpData(mSh1106, 0, 0, mOledDisplayHelper.getDisplayBitmap(), false);
            mSh1106.show(); // render the pixel data
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @Override
    public void setContrast(int level) {
        try {
            mSh1106.setContrast(level);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @Override
    public void invertDisplay(boolean invert) {
        try {
            mSh1106.setInvertDisplay(invert);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @Override
    public void close() {
        try {
            mSh1106.close();
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
