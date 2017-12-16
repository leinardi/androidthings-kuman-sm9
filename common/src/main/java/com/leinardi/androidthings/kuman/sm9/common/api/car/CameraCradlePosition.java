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
    public static final float MAX_ANGLE = 90;
    public static final float MIN_ANGLE = MAX_ANGLE * -1;
    private final float mHorizontalServoAngle;
    private final float mVerticalServoAngle;

    public CameraCradlePosition(float horizontalServoAngle, float verticalServoAngle) {
        checkAngle(horizontalServoAngle);
        checkAngle(verticalServoAngle);
        mHorizontalServoAngle = horizontalServoAngle;
        mVerticalServoAngle = verticalServoAngle;
    }

    public float getHorizontalServoAngle() {
        return mHorizontalServoAngle;
    }

    public float getVerticalServoAngle() {
        return mVerticalServoAngle;
    }

    private void checkAngle(float angle) {
        if (angle < MIN_ANGLE || angle > MAX_ANGLE) {
            throw new IllegalArgumentException("Invalid angle value");
        }
    }
}
