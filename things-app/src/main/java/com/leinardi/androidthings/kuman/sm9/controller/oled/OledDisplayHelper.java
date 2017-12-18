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

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;

import com.leinardi.androidthings.kuman.sm9.R;
import com.leinardi.androidthings.kuman.sm9.util.SystemHelper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OledDisplayHelper {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 64;
    private static final int TEXT_SIZE = 8;
    private static final int TEXT_PADDING = 2;
    private static final String[] SPINNER = {"|", "/", "-", "\\", "|", "/", "-", "\\"};
    private int mSpinnerIndex;
    private Paint mTextPaint;
    private Paint mBackgroundPaint;
    private Bitmap mTextAsBitmap;
    private Canvas mCanvas;
    private Application mApplication;
    private SystemHelper mSystemHelper;
    private List<String> mNetworkInterfaceAdressList = null;

    @Inject
    public OledDisplayHelper(Application application, SystemHelper systemHelper) {
        mApplication = application;
        mSystemHelper = systemHelper;
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setTypeface(ResourcesCompat.getFont(application, R.font.oled_font));
        mTextPaint.setColor(Color.BLACK);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mNetworkInterfaceAdressList = new ArrayList<>(4);
    }

    public String getTimeText() {
        return mSystemHelper.getTime();
    }

    public String getCpuUsageText() {
        String usageString = "N/A";
        Integer cpuUsage = mSystemHelper.getCpuUsage();
        if (cpuUsage != null) {
            usageString = Integer.toString(cpuUsage);
        }
        return String.format(Locale.getDefault(), "CPU U %s%%", usageString);
    }

    public String getCpuTemperatureText() {
        String tempString = "N/A";
        Integer cpuTemp = mSystemHelper.getCpuTemperature();
        if (cpuTemp != null) {
            tempString = Integer.toString(cpuTemp);
        }
        return String.format(Locale.getDefault(), "CPU T %s°C", tempString);
    }

    private void clearCanvas() {
        mTextAsBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mTextAsBitmap);
        mCanvas.drawPaint(mBackgroundPaint);
    }

    public Bitmap getDisplayBitmap() {
        clearCanvas();
        paintScreen();
        return mTextAsBitmap;
    }

    private void paintScreen() {
        updateNetworkInterfaceAddresses();
        mTextPaint.setTextAlign(Paint.Align.RIGHT);
        mCanvas.drawText(getTimeText(), WIDTH - TEXT_PADDING, getLineYCoordinate(1), mTextPaint);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mCanvas.drawText(getSpinner(), TEXT_PADDING, getLineYCoordinate(1), mTextPaint);
        mCanvas.drawText(getCpuUsageText(), TEXT_PADDING, getLineYCoordinate(2), mTextPaint);
        mCanvas.drawText(getCpuTemperatureText(), TEXT_PADDING, getLineYCoordinate(3), mTextPaint);
        int i = 5;
        for (String networkInterface : mNetworkInterfaceAdressList) {
            mCanvas.drawText(networkInterface, TEXT_PADDING, getLineYCoordinate(i), mTextPaint);
            i++;
        }
    }

    private int getLineYCoordinate(int line) {
        return line * (TEXT_SIZE + TEXT_PADDING);
    }

    private String getSpinner() {
        String spinner = SPINNER[mSpinnerIndex % SPINNER.length];
        mSpinnerIndex++;
        return spinner;
    }

    private void updateNetworkInterfaceAddresses() {
        if (mNetworkInterfaceAdressList.isEmpty() || mSpinnerIndex % SPINNER.length == 0) {
            mNetworkInterfaceAdressList = mSystemHelper.getNetworkInterfaceAdresses();
        }
    }
}
