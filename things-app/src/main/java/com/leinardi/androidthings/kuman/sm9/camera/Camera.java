/*
 * Copyright 2016, The Android Open Source Project
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

package com.leinardi.androidthings.kuman.sm9.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Size;

import timber.log.Timber;

import java.util.Collections;

import static android.content.Context.CAMERA_SERVICE;

/**
 * Helper class to deal with methods to deal with images from the camera.
 */
public class Camera {
    private static final String TAG = Camera.class.getSimpleName();

    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;
    private static final int MAX_IMAGES = 1;

    private CameraDevice mCameraDevice;

    private CameraCaptureSession mCaptureSession;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    // Lazy-loaded singleton, so only one instance of the camera is created.
    private Camera() {
    }

    private static class InstanceHolder {
        private static Camera sCamera = new Camera();
    }

    public static Camera getInstance() {
        return InstanceHolder.sCamera;
    }

    /**
     * Initialize the camera device
     */
    @SuppressLint("MissingPermission")
    public void initializeCamera(Context context,
                                 Handler backgroundHandler,
                                 ImageReader.OnImageAvailableListener imageAvailableListener) {
        // Discover the camera instance
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Timber.e(e, "Cam access exception getting IDs");
        }
        if (camIds.length < 1) {
            Timber.d("No cameras found");
            return;
        }
        String id = camIds[0];
        Timber.d("Using camera id %s", id);

        // Initialize the image processor
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                ImageFormat.JPEG, MAX_IMAGES);
        mImageReader.setOnImageAvailableListener(
                imageAvailableListener, backgroundHandler);

        // Open the camera resource
        try {
            manager.openCamera(id, mStateCallback, backgroundHandler);
        } catch (CameraAccessException cae) {
            Timber.e(cae, "Camera access exception");
        }
    }

    /**
     * Callback handling device state changes
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Timber.d("Opened camera.");
            mCameraDevice = cameraDevice;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Timber.d("Camera disconnected, closing.");
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Timber.d("Camera device error, closing.");
            cameraDevice.close();
        }

        @Override
        public void onClosed(@NonNull CameraDevice cameraDevice) {
            Timber.d("Closed camera, releasing");
            mCameraDevice = null;
        }
    };

    /**
     * Begin a still image capture
     */
    public void takePicture() {
        if (mCameraDevice == null) {
            Timber.w("Cannot capture image. Camera not initialized.");
            return;
        }

        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionCallback,
                    null);
        } catch (CameraAccessException cae) {
            Timber.d(cae, "access exception while preparing pic");
        }
    }

    /**
     * Callback handling session state changes
     */
    private CameraCaptureSession.StateCallback mSessionCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (mCameraDevice == null) {
                        return;
                    }

                    // When the session is ready, we start capture.
                    mCaptureSession = cameraCaptureSession;
                    triggerImageCapture();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Timber.w("Failed to configure camera");
                }
            };

    /**
     * Execute a new capture request within the active session
     */
    private void triggerImageCapture() {
        try {
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            Timber.d("Session initialized.");
            mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException cae) {
            Timber.d("camera capture exception");
        }
    }

    /**
     * Callback handling capture session events
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    Timber.d("Partial result");
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    session.close();
                    mCaptureSession = null;
                    Timber.d("CaptureSession closed");
                }
            };

    /**
     * Close the camera resources
     */
    public void shutDown() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    /**
     * Helpful debugging method:  Dump all supported camera formats to log.  You don't need to run
     * this for normal operation, but it's very helpful when porting this code to different
     * hardware.
     */
    public static void dumpFormatInfo(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Timber.d("Cam access exception getting IDs");
        }
        if (camIds.length < 1) {
            Timber.d("No cameras found");
        }
        String id = camIds[0];
        Timber.d("Using camera id %s", id);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            StreamConfigurationMap configs = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            for (int format : configs.getOutputFormats()) {
                Timber.d("Getting sizes for format: %s", format);
                for (Size s : configs.getOutputSizes(format)) {
                    Timber.d("\t%s", s.toString());
                }
            }
            int[] effects = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
            for (int effect : effects) {
                Timber.d("Effect available: %s", effect);
            }
        } catch (CameraAccessException e) {
            Timber.d("Cam access exception getting characteristics.");
        }
    }
}
