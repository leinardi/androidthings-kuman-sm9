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

package com.leinardi.androidthings.kuman.sm9.ui;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;

import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import com.leinardi.androidthings.kuman.sm9.common.ui.BaseViewModel;
import timber.log.Timber;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainViewModel extends BaseViewModel<MainViewModelObservable> {
    //    private final Camera mCamera;
    private Application mApplication;

    private PwrA53A mPwrA53A;
    private Sh1106 mSh1106;
    private int mContrast;
    private boolean mReversed;

    @Inject
    public MainViewModel(@Nullable PwrA53A pwrA53A,
                         @Nullable Sh1106 sh1106,
                         Application application) {
        mPwrA53A = pwrA53A;
        mSh1106 = sh1106;
        mApplication = application;
        //        mPwrA53A.test();
        //        testScreen();

        //        // Creates new handlers and associated threads for camera and networking operations.
        //        mCameraThread = new HandlerThread("CameraBackground");
        //        mCameraThread.start();
        //        mCameraHandler = new Handler(mCameraThread.getLooper());
        //
        //        // Camera code is complicated, so we've shoved it all in this closet class for you.
        //        mCamera = Camera.getInstance();
        //        mCamera.initializeCamera(mApplication, mCameraHandler, mOnImageAvailableListener);
        //
        //        new CountDownTimer(3000, 3000) {
        //            @Override
        //            public void onTick(long l) {
        //
        //            }
        //
        //            @Override
        //            public void onFinish() {
        //                mCamera.takePicture();
        //            }
        //        }.start();
    }

    private void testScreen() {
        // Draw on the screen:

        //        try {
        //            mSh1106.clearPixels();
        //
        //            //            for (int i = 0; i < mSh1106.getLcdWidth(); i++) {
        //            //                for (int j = 0; j < mSh1106.getLcdHeight(); j++) {
        //            //                    // checkerboard
        //            //                    mSh1106.setPixel(i, j, (i % 3) == (j % 2));
        //            //                }
        //            //            }
        //
        //            Bitmap bitmap = BitmapFactory.decodeResource(mApplication.getResources(), R.drawable.test_inv);
        //            BitmapHelper.setBmpData(mSh1106, 0, 0, bitmap, false);
        //
        //            mSh1106.show(); // render the pixel data
        //
        //            // You can also use BitmapHelper to render a bitmap instead of setting pixels manually
        //        } catch (IOException e) {
        //            // error setting display
        //            Timber.e(e);
        //        }
    }

    /**
     * A {@link Handler} for running Camera tasks in the background.
     */
    private Handler mCameraHandler;

    /**
     * An additional thread for running Camera tasks that shouldn't block the UI.
     */
    private HandlerThread mCameraThread;

    /**
     * Listener for new camera images.
     */
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    FileOutputStream fos = null;
                    Bitmap bitmap = null;

                    try {
                        image = reader.acquireLatestImage();
                        if (image != null) {
                            final Image.Plane[] planes = image.getPlanes();
                            final ByteBuffer buffer = planes[0].getBuffer();
                            final byte[] data = new byte[buffer.capacity()];
                            buffer.get(data);
                            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                            // write bitmap to a file
                            fos = new FileOutputStream("/sdcard/myscreen.jpg");
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                            Timber.d("captured image");
                        }

                    } catch (Exception e) {
                        Timber.e(e);
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
                        }

                        if (bitmap != null) {
                            bitmap.recycle();
                        }

                        if (image != null) {
                            image.close();
                        }
                    }
                }
            };

    @Override
    protected void onCleared() {
        //        mCamera.shutDown();
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
        setServoAngle(PwrA53A.SERVO_ID_8, 0);
    }

    public void onHorizontalServo90DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_8, 90);
    }

    public void onHorizontalServo180DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_8, 180);
    }

    public void onVerticalServo0DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_7, 0);
    }

    public void onVerticalServo90DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_7, 90);
    }

    public void onVerticalServo180DegreesClicked() {
        setServoAngle(PwrA53A.SERVO_ID_7, 180);
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
            mPwrA53A.motorsStop();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveForwardClicked() {
        try {
            mPwrA53A.motorsForward(100);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveBackwardClicked() {
        try {
            mPwrA53A.motorsBackward(100);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveLeftClicked() {
        try {
            mPwrA53A.motorsTurnLeft(100);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public void onMoveRightClicked() {
        try {
            mPwrA53A.motorsTurnRight(100);
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
}
