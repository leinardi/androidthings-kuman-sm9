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

public class CameraCradlePosition implements Serializable {
    public static final int MAX_ANGLE = 90;
    public static final int MIN_ANGLE = MAX_ANGLE * -1;
    private final int mHorizontalServoAngle;
    private final int mVerticalServoAngle;

    public CameraCradlePosition(int horizontalServoAngle, int verticalServoAngle) {
        checkAngle(horizontalServoAngle);
        checkAngle(verticalServoAngle);
        mHorizontalServoAngle = horizontalServoAngle;
        mVerticalServoAngle = verticalServoAngle;
    }

    public int getHorizontalServoAngle() {
        return mHorizontalServoAngle;
    }

    public int getVerticalServoAngle() {
        return mVerticalServoAngle;
    }

    private void checkAngle(int angle) {
        if (angle < MIN_ANGLE || angle > MAX_ANGLE) {
            throw new IllegalArgumentException("Invalid angle value");
        }
    }

    @Override
    public String toString() {
        return "CameraCradlePosition{" +
                "mHorizontalServoAngle=" + mHorizontalServoAngle +
                ", mVerticalServoAngle=" + mVerticalServoAngle +
                '}';
    }
}
