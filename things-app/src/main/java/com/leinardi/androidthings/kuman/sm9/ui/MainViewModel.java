/*
 * Copyright $today.yea Roberto Leinardi.
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

package com.leinardi.androidthings.kuman.sm9.ui;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import com.leinardi.androidthings.driver.sh1106.BitmapHelper;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import com.leinardi.androidthings.kuman.sm9.R;
import com.leinardi.androidthings.kuman.sm9.api.GoogleApiClientRepository;
import com.leinardi.androidthings.kuman.sm9.common.ui.BaseViewModel;
import timber.log.Timber;

import javax.inject.Inject;
import java.io.IOException;

public class MainViewModel extends BaseViewModel<MainViewModelObservable> {
    private GoogleApiClientRepository mGoogleApiClientRepository;
    private Application mApplication;
    private static final String RPI3_I2C_BUS_NAME = "I2C1";
    private PwrA53A mPwrA53A;
    private Sh1106 mSh1106;
    private int mContrast;
    private boolean mReversed;

    @Inject
    public MainViewModel(GoogleApiClientRepository googleApiClientRepository, Application application) {
        mGoogleApiClientRepository = googleApiClientRepository;
        mApplication = application;
        //        mGoogleApiClientRepository.connect();

        try {
            mPwrA53A = new PwrA53A(RPI3_I2C_BUS_NAME);
            mSh1106 = new Sh1106(RPI3_I2C_BUS_NAME);
        } catch (IOException e) {
            Timber.e(e);
        }
//        mPwrA53A.test();
        testScreen();

    }

    private void testScreen() {
        // Draw on the screen:

        try {
            mSh1106.clearPixels();

            //            for (int i = 0; i < mSh1106.getLcdWidth(); i++) {
            //                for (int j = 0; j < mSh1106.getLcdHeight(); j++) {
            //                    // checkerboard
            //                    mSh1106.setPixel(i, j, (i % 3) == (j % 2));
            //                }
            //            }

            Bitmap bitmap = BitmapFactory.decodeResource(mApplication.getResources(), R.drawable.test_inv);
            BitmapHelper.setBmpData(mSh1106, 0, 0, bitmap, false);

            mSh1106.show(); // render the pixel data

            // You can also use BitmapHelper to render a bitmap instead of setting pixels manually
        } catch (IOException e) {
            // error setting display
            Timber.e(e);
        }
    }

    @Override
    protected void onCleared() {
        mGoogleApiClientRepository.clear();
        try {
            mPwrA53A.close();
        } catch (IOException e) {
            Timber.e(e);
        }
        try {
            mSh1106.close();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onLed0Clicked() {
        try {
            mPwrA53A.setLed0Value(true);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onLed1Clicked() {
        try {
            mPwrA53A.setLed1Value(true);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onLed2Clicked() {
        try {
            mPwrA53A.setLed2Value(true);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onLedResetClicked() {
        try {
            mPwrA53A.setLed0Value(false);
            mPwrA53A.setLed1Value(false);
            mPwrA53A.setLed2Value(false);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private void setServoAngle(int servoId, int angle) {
        try {
            Timber.d("Initial servo angle = %d", mPwrA53A.getServoAngle(servoId));
            mPwrA53A.setServoAngle(servoId, angle);
            Timber.d("Final servo angle = %d", mPwrA53A.getServoAngle(servoId));
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onHorizontalServo0DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_7, 0);
    }

    public void onHorizontalServo90DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_7, 90);
    }

    public void onHorizontalServo180DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_7, 180);
    }

    public void onVerticalServo0DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_8, 0);
    }

    public void onVerticalServo90DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_8, 90);
    }

    public void onVerticalServo180DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_8, 180);
    }

    public void onReadVoltageClicked() {
        try {
            Timber.d("Voltage = %d", mPwrA53A.getVoltage());
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveStopClicked() {
        try {
            mPwrA53A.motorStop();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveForwardClicked() {
        try {
            mPwrA53A.motorForward();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveBackwardClicked() {
        try {
            mPwrA53A.motorBackward();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveLeftClicked() {
        try {
            mPwrA53A.motorTurnLeft();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveRightClicked() {
        try {
            mPwrA53A.motorTurnRight();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onToggleContrastClicked() {
        try {
            mSh1106.setContrast(mContrast);
            if (mContrast == 0) {
                mContrast = 127;
            } else if (mContrast == 127) {
                mContrast = 255;
            } else {
                mContrast = 0;
            }
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onStartScrollClicked() {
        try {
            mSh1106.setInvertDisplay(mReversed);
            mReversed = !mReversed;
        } catch (IOException e) {
            Timber.e(e);
        }
    }
}
