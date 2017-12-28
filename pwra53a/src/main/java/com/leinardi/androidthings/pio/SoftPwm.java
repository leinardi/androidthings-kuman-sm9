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

package com.leinardi.androidthings.pio;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Controls a GPIO pin providing PWM capabilities. Opening a GPIO pin takes ownership of it for the whole system,
 * preventing anyone else from opening/accessing the GPIO until you call close(). Forgetting to call close() will
 * prevent anyone (including the same process/app) from using the GPIO.
 */
public class SoftPwm extends Pwm {
    public static final int MAX_FREQ = 10000; //10kHz
    private static final int NANOS_PER_MILLI = 1000000;
    private static final long HZ_IN_NANOSECONDS = 1000000000;
    private static final String TAG = SoftPwm.class.getSimpleName();
    private Gpio mGpio;
    private double mFreq;
    private double mDutyCycle;
    private Thread mThread;
    private long mPeriodTotal;
    private long mPeriodHighMs;
    private int mPeriodHighNs;
    private long mPeriodLowMs;
    private int mPeriodLowNs;
    private boolean mEnabled;

    public SoftPwm() {
    }

    public void openSoftPwm(String gpioName) throws IOException {
        PeripheralManagerService manager = new PeripheralManagerService();
        mGpio = manager.openGpio(gpioName);
        mGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    }

    @Override
    public void close() {
        if (mGpio != null) {
            try {
                mGpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO device", e);
            }
        }
    }

    /**
     * Enable the GPIO/PWM pin. Frequency must be set via {@link #setPwmFrequencyHz(double)} before enabling the pin,
     * but frequency and duty cycle settings can be set in both enabled and disabled state and will be remembered if the
     * PWM is disabled and then re-enabled.
     *
     * @param enabled True to enable the PWM, false to disable.
     */
    @Override
    public void setEnabled(boolean enabled) throws IOException {
        if (mGpio == null) {
            throw new IllegalStateException("GPIO Device not open");
        }
        if (mFreq == 0) {
            throw new IllegalStateException("Frequency must be set via setPwmFrequencyHz() before enabling the pin");
        }
        if (mEnabled != enabled) {
            mEnabled = enabled;
            if (enabled) {
                startNewThread();
            } else {
                stopThread();
            }
        }
    }

    private void stopThread() throws IOException {
        mGpio.setValue(false);
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    private void startNewThread() {
        mThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (mPeriodHighMs != 0 || mPeriodHighNs != 0) {
                        mGpio.setValue(true);
                        Thread.sleep(mPeriodHighMs, mPeriodHighNs);
                    }
                    if (mPeriodLowMs != 0 || mPeriodLowNs != 0) {
                        mGpio.setValue(false);
                        Thread.sleep(mPeriodLowMs, mPeriodLowNs);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    Log.e(TAG, "GPIO error", e);
                }
            }
        });
        mThread.setPriority(Thread.NORM_PRIORITY + 1);
        mThread.start();
    }

    /**
     * Get the duty cycle.
     *
     * @return A double between 0 and 100 (inclusive).
     */
    public double getPwmDutyCycle() {
        return mDutyCycle;
    }

    /**
     * Set the duty cycle.
     *
     * @param dutyCycle A double between 0 and 100 (inclusive).
     */
    @Override
    public void setPwmDutyCycle(double dutyCycle) {
        if (dutyCycle < 0 || dutyCycle > 100) {
            throw new IllegalArgumentException("Invalid duty cycle value (must be between 0 and 100 included). "
                    + "Duty cycle:" + dutyCycle);
        }
        mDutyCycle = dutyCycle;
        mPeriodHighNs = (int) Math.round(mPeriodTotal / 100f * dutyCycle);
        mPeriodLowNs = (int) (mPeriodTotal - mPeriodHighNs);
        mPeriodHighMs = mPeriodHighNs / NANOS_PER_MILLI;
        mPeriodHighNs %= NANOS_PER_MILLI;
        mPeriodLowMs = mPeriodLowNs / NANOS_PER_MILLI;
        mPeriodLowNs %= NANOS_PER_MILLI;
    }

    /**
     * Get the frequency of the signal.
     *
     * @return Frequency in Hertz to use for the signal. Must be positive.
     */
    public double getPwmFrequencyHz() {
        return mFreq;
    }

    /**
     * Set the frequency of the signal.
     *
     * @param freqHz Frequency in Hertz to use for the signal. Must be positive (max 10kHz).
     */
    @Override
    public void setPwmFrequencyHz(double freqHz) {
        if (freqHz < 0 || freqHz > MAX_FREQ) {
            throw new IllegalArgumentException("Invalid frequency value (must be between 0 and " + MAX_FREQ
                    + "Hz included). Freq:" + freqHz);
        }
        mFreq = freqHz;
        mPeriodTotal = Math.round(HZ_IN_NANOSECONDS / freqHz);
        setPwmDutyCycle(mDutyCycle);
    }
}
