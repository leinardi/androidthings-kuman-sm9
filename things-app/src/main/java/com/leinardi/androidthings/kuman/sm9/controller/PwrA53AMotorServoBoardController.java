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

import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import timber.log.Timber;

import java.io.IOException;

public class PwrA53AMotorServoBoardController implements MotorServoBoardController {
    private static final int ANGLE_OFFSET = 90;
    private static final int DEFAULT_VERTICAL_ANGLE = 0;
    private static final int DEFAULT_HORIZONTAL_ANGLE = 0;
    private static final int POWER_THRESHOLD = 33;
    private PwrA53A mPwrA53A;

    public PwrA53AMotorServoBoardController(@Nullable PwrA53A pwrA53A) {
        if (pwrA53A == null) {
            throw new IllegalStateException("Unable to get the instance of PwrA53A");
        }
        mPwrA53A = pwrA53A;
        moveCamera(DEFAULT_HORIZONTAL_ANGLE, DEFAULT_VERTICAL_ANGLE);
    }

    @Override
    public void moveCar(int angle, int power) {
        try {
            if (power == 0) {
                mPwrA53A.motorStop();
            } else if (power >= POWER_THRESHOLD) {
                if (angle >= -45 && angle < 45) {
                    mPwrA53A.motorTurnLeft();
                } else if (angle >= 45 && angle < 135) {
                    mPwrA53A.motorForward();
                } else if ((angle >= 135 && angle <= 180) || (angle >= -180 && angle < -135)) {
                    mPwrA53A.motorTurnRight();
                } else if (angle >= -135 && angle < -45) {
                    mPwrA53A.motorBackward();
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @Override
    public void moveCamera(int horizontalAngle, int verticalAngle) {
        try {
            mPwrA53A.setServoAngle(PwrA53A.SERVO_ID_7, verticalAngle);
            mPwrA53A.setServoAngle(PwrA53A.SERVO_ID_8, horizontalAngle + ANGLE_OFFSET);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @Override
    public void close() {
        mPwrA53A.close();
    }
}
