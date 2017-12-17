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

public class ThingsMessage implements Serializable {
    private final CarMovement mCarMovement;
    private final CameraCradlePosition mCameraCradlePosition;
    private final Boolean mStreaming;
    private final Boolean mLockCameraPosition;
    private final Boolean mAvoidCollisions;
    private final Boolean mFollowLine;
    private final Boolean mClientDisconnecting;

    private ThingsMessage(Builder builder) {
        mCarMovement = builder.mCarMovement;
        mCameraCradlePosition = builder.mCameraCradlePosition;
        mStreaming = builder.mStreaming;
        mLockCameraPosition = builder.mLockCameraPosition;
        mAvoidCollisions = builder.mAvoidCollisions;
        mFollowLine = builder.mFollowLine;
        mClientDisconnecting = builder.mClientDisconnecting;
    }

    public CarMovement getCarMovement() {
        return mCarMovement;
    }

    public CameraCradlePosition getCameraCradlePosition() {
        return mCameraCradlePosition;
    }

    public Boolean isStreaming() {
        return mStreaming;
    }

    public Boolean isLockCameraPosition() {
        return mLockCameraPosition;
    }

    public Boolean isAvoidCollisions() {
        return mAvoidCollisions;
    }

    public Boolean isFollowLine() {
        return mFollowLine;
    }

    public static class Builder {
        private CarMovement mCarMovement;
        private CameraCradlePosition mCameraCradlePosition;
        private Boolean mStreaming;
        private Boolean mLockCameraPosition;
        private Boolean mAvoidCollisions;
        private Boolean mFollowLine;
        private Boolean mClientDisconnecting;

        public Builder setCarMovement(CarMovement carMovement) {
            mCarMovement = carMovement;
            return this;
        }

        public Builder setCameraCradlePosition(CameraCradlePosition cameraCradlePosition) {
            mCameraCradlePosition = cameraCradlePosition;
            return this;
        }

        public Builder setStreaming(Boolean streaming) {
            mStreaming = streaming;
            return this;
        }

        public Builder setLockCameraPosition(Boolean lockCameraPosition) {
            mLockCameraPosition = lockCameraPosition;
            return this;
        }

        public Builder setAvoidCollisions(Boolean avoidCollisions) {
            mAvoidCollisions = avoidCollisions;
            return this;
        }

        public Builder setFollowLine(Boolean followLine) {
            mFollowLine = followLine;
            return this;
        }

        public Builder setClientDisconnecting() {
            mClientDisconnecting = true;
            return this;
        }

        public ThingsMessage build() {
            return new ThingsMessage(this);
        }
    }

    @Override
    public String toString() {
        return "ThingsMessage{" +
                "mCarMovement=" + mCarMovement +
                ", mCameraCradlePosition=" + mCameraCradlePosition +
                ", mStreaming=" + mStreaming +
                ", mLockCameraPosition=" + mLockCameraPosition +
                ", mAvoidCollisions=" + mAvoidCollisions +
                ", mFollowLine=" + mFollowLine +
                ", mClientDisconnecting=" + mClientDisconnecting +
                '}';
    }
}
