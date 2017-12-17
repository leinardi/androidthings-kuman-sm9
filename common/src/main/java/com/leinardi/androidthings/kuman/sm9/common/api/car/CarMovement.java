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

package com.leinardi.androidthings.kuman.sm9.common.api.car;

import java.io.Serializable;

public class CarMovement implements Serializable {
    public static final int MAX_ANGLE = 180;
    public static final int MIN_ANGLE = MAX_ANGLE * -1;
    public static final int MIN_POWER = 0;
    public static final int MAX_POWER = 100;
    private final int mAngle;
    private final int mPower;

    public CarMovement(int angle, int power) {
        checkAngle(angle);
        mAngle = angle;
        mPower = power;
    }

    public int getAngle() {
        return mAngle;
    }

    public int getPower() {
        return mPower;
    }

    private void checkAngle(int angle) {
        if (angle < MIN_ANGLE || angle > MAX_ANGLE) {
            throw new IllegalArgumentException("Invalid angle value");
        }
    }

    private void checkPower(int power) {
        if (power < MIN_POWER || power > MAX_POWER) {
            throw new IllegalArgumentException("Invalid power value");
        }
    }

    @Override
    public String toString() {
        return "CarMovement{" +
                "mAngle=" + mAngle +
                ", mPower=" + mPower +
                '}';
    }
}
