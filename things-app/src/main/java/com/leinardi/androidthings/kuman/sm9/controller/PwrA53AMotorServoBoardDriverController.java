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

import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import timber.log.Timber;

import java.io.IOException;

public class PwrA53AMotorServoBoardDriverController extends MotorServoBoardDriverController<PwrA53A> {
    private static final int ANGLE_OFFSET = 90;
    private static final int DEFAULT_VERTICAL_ANGLE = 0;
    private static final int DEFAULT_HORIZONTAL_ANGLE = 0;
    private static final int POWER_THRESHOLD = 33;

    public PwrA53AMotorServoBoardDriverController() {
        moveCamera(DEFAULT_HORIZONTAL_ANGLE, DEFAULT_VERTICAL_ANGLE);
    }

    @Override
    public void moveCar(int angle, int power) {
        if (isHardwareAvailable()) {
            try {
                if (power == 0) {
                    getDriver().motorStop();
                } else if (power >= POWER_THRESHOLD) {
                    if (angle >= -45 && angle < 45) {
                        getDriver().motorTurnLeft();
                    } else if (angle >= 45 && angle < 135) {
                        getDriver().motorForward();
                    } else if ((angle >= 135 && angle <= 180) || (angle >= -180 && angle < -135)) {
                        getDriver().motorTurnRight();
                    } else if (angle >= -135 && angle < -45) {
                        getDriver().motorBackward();
                    }
                }
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void moveCamera(int horizontalAngle, int verticalAngle) {
        if (isHardwareAvailable()) {
            try {
                getDriver().setServoAngle(PwrA53A.SERVO_ID_7, verticalAngle);
                getDriver().setServoAngle(PwrA53A.SERVO_ID_8, horizontalAngle + ANGLE_OFFSET);
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }
}
