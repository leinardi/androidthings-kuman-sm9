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

package com.leinardi.androidthings.driver.pwra53a;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.Pwm;
import com.leinardi.androidthings.pio.SoftPwm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PwrA53A implements AutoCloseable {
    private static final String TAG = PwrA53A.class.getSimpleName();
    private static final int POWER_MIN = 0;
    private static final int POWER_MAX = 100;
    private static final int PWM_FREQUENCY = 100;
    private final String mI2cName;
    /**
     * Default I2C address for the sensor.
     */
    private static final int DEFAULT_I2C_ADDRESS = 0x17;

    // Registers
    private static final int PWR_A53_A_REG_SERVO = 0xFF;
    private static final int PWR_A53_A_REG_VOLTAGE = 0x12;
    private static final int PWR_A53_A_REG_SPEED_COUNTER_1 = 0x13;
    private static final int PWR_A53_A_REG_SPEED_COUNTER_2 = 0x14;

    // Servos IDs
    public static final int SERVO_ID_1 = 1;
    public static final int SERVO_ID_2 = 2;
    public static final int SERVO_ID_3 = 3;
    public static final int SERVO_ID_4 = 4;
    public static final int SERVO_ID_5 = 5;
    public static final int SERVO_ID_6 = 6;
    public static final int SERVO_ID_7 = 7; // Vertical servo
    public static final int SERVO_ID_8 = 8; // Horizontal servo

    // GPIO LEDs
    private static final String PWR_A53_A_LED0 = "BCM10";
    private static final String PWR_A53_A_LED1 = "BCM9";
    private static final String PWR_A53_A_LED2 = "BCM25";

    // PWM Motors
    private static final String PWR_A53_A_ENA_PWM = "PWM1";
    private static final String PWR_A53_A_ENB_PWM = "PWM0";

    // GPIO Motors
    private static final String PWR_A53_A_ENA_GPIO = "BCM13";
    private static final String PWR_A53_A_ENB_GPIO = "BCM20";
    private static final String PWR_A53_A_IN1 = "BCM19";
    private static final String PWR_A53_A_IN2 = "BCM16";
    private static final String PWR_A53_A_IN3 = "BCM21";
    private static final String PWR_A53_A_IN4 = "BCM26";

    // GPIO Servos
    private static final String PWR_A53_A_SER1 = "BCM11";
    private static final String PWR_A53_A_SER2 = "BCM8";
    private static final String PWR_A53_A_SER3 = "BCM7";
    private static final String PWR_A53_A_SER4 = "BCM5";
    private static final String PWR_A53_A_SER7 = "BCM6";
    private static final String PWR_A53_A_SER8 = "BCM12";

    // GPIO Ultrasound
    private static final String PWR_A53_A_ECHO = "BCM4";
    private static final String PWR_A53_A_TRIG = "BCM17";

    // GPIO Infrared
    private static final String PWR_A53_A_IR_R = "BCM18";
    private static final String PWR_A53_A_IR_L = "BCM27";
    private static final String PWR_A53_A_IR_M = "BCM22";
    private static final String PWR_A53_A_IRF_R = "BCM23";
    private static final String PWR_A53_A_IRF_L = "BCM24";

    private I2cDevice mI2cDevice;
    private Pwm mPwmEnA;
    private Pwm mPwmEnB;
    private SoftPwm mPwmSwEnA;
    private SoftPwm mPwmSwEnB;
    private final Map<String, Gpio> mGpioMap;
    private boolean mUsePwm;

    /**
     * Create a new PWR.A53.A sensor driver connected on the given bus.
     *
     * @throws IOException
     */
    public PwrA53A(String i2cName) throws IOException {
        mI2cName = i2cName;
        mGpioMap = new HashMap<>();
        try {
            connect();
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (RuntimeException ignored) {
            }
            throw e;
        }
    }

    private void connect() throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();

        // Open devices
        mI2cDevice = pioService.openI2cDevice(mI2cName, DEFAULT_I2C_ADDRESS);

        mGpioMap.put(PWR_A53_A_LED0, pioService.openGpio(PWR_A53_A_LED0));
        mGpioMap.put(PWR_A53_A_LED1, pioService.openGpio(PWR_A53_A_LED1));
        mGpioMap.put(PWR_A53_A_LED2, pioService.openGpio(PWR_A53_A_LED2));

        mGpioMap.put(PWR_A53_A_IR_R, pioService.openGpio(PWR_A53_A_IR_R));
        mGpioMap.put(PWR_A53_A_IR_L, pioService.openGpio(PWR_A53_A_IR_L));
        mGpioMap.put(PWR_A53_A_IR_M, pioService.openGpio(PWR_A53_A_IR_M));
        mGpioMap.put(PWR_A53_A_IRF_R, pioService.openGpio(PWR_A53_A_IRF_R));
        mGpioMap.put(PWR_A53_A_IRF_L, pioService.openGpio(PWR_A53_A_IRF_L));

        mGpioMap.put(PWR_A53_A_TRIG, pioService.openGpio(PWR_A53_A_TRIG));
        mGpioMap.put(PWR_A53_A_ECHO, pioService.openGpio(PWR_A53_A_ECHO));

        mUsePwm = tryToOpenPwm(pioService);

        if (!mUsePwm) {
            mPwmSwEnA = new SoftPwm();
            mPwmSwEnA.openSoftPwm(PWR_A53_A_ENA_GPIO);
            mPwmSwEnB = new SoftPwm();
            mPwmSwEnB.openSoftPwm(PWR_A53_A_ENB_GPIO);
        }
        mGpioMap.put(PWR_A53_A_IN1, pioService.openGpio(PWR_A53_A_IN1));
        mGpioMap.put(PWR_A53_A_IN2, pioService.openGpio(PWR_A53_A_IN2));
        mGpioMap.put(PWR_A53_A_IN3, pioService.openGpio(PWR_A53_A_IN3));
        mGpioMap.put(PWR_A53_A_IN4, pioService.openGpio(PWR_A53_A_IN4));

        // Setup GPIO
        getGpio(PWR_A53_A_LED0).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        getGpio(PWR_A53_A_LED1).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        getGpio(PWR_A53_A_LED2).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        if (!mUsePwm) {
            mPwmSwEnA.setPwmFrequencyHz(PWM_FREQUENCY);
            mPwmSwEnB.setPwmFrequencyHz(PWM_FREQUENCY);
            setPwmThrottle(mPwmSwEnA, 0);
            setPwmThrottle(mPwmSwEnB, 0);
        } else {
            mPwmEnA.setPwmFrequencyHz(PWM_FREQUENCY);
            mPwmEnB.setPwmFrequencyHz(PWM_FREQUENCY);
            setPwmThrottle(mPwmEnA, 0);
            setPwmThrottle(mPwmEnB, 0);
        }
        getGpio(PWR_A53_A_IN1).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_IN2).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_IN3).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_IN4).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        getGpio(PWR_A53_A_IR_R).setDirection(Gpio.DIRECTION_IN);
        getGpio(PWR_A53_A_IR_L).setDirection(Gpio.DIRECTION_IN);
        getGpio(PWR_A53_A_IR_M).setDirection(Gpio.DIRECTION_IN);
        getGpio(PWR_A53_A_IRF_R).setDirection(Gpio.DIRECTION_IN);
        getGpio(PWR_A53_A_IRF_L).setDirection(Gpio.DIRECTION_IN);
        getGpio(PWR_A53_A_IR_R).setActiveType(Gpio.ACTIVE_HIGH);
        getGpio(PWR_A53_A_IR_L).setActiveType(Gpio.ACTIVE_HIGH);
        getGpio(PWR_A53_A_IR_M).setActiveType(Gpio.ACTIVE_HIGH);
        getGpio(PWR_A53_A_IRF_R).setActiveType(Gpio.ACTIVE_HIGH);
        getGpio(PWR_A53_A_IRF_L).setActiveType(Gpio.ACTIVE_HIGH);

        getGpio(PWR_A53_A_TRIG).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_ECHO).setDirection(Gpio.DIRECTION_IN);
        getGpio(PWR_A53_A_ECHO).setActiveType(Gpio.ACTIVE_HIGH);
    }

    /**
     * Tries to open the PWM devices.
     * Currently Android Things doesn't allow to set custom pins for the PWM and the PWR.A53.A
     * is expecting to get it on pin 20 and pin 13. Unfortunately Android uses pin 18 and 13.
     * If you want to use PWM with this board please star this issue:
     * https://issuetracker.google.com/issues/70115494
     */
    private boolean tryToOpenPwm(PeripheralManagerService pioService) {
        try {
            mPwmEnB = pioService.openPwm(PWR_A53_A_ENB_PWM);
            mPwmEnA = pioService.openPwm(PWR_A53_A_ENA_PWM);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Unable to open PWMs, falling back to SoftPwm", e);
            try {
                if (mPwmEnA != null) {
                    mPwmEnA.close();
                }
                if (mPwmEnB != null) {
                    mPwmEnB.close();
                }
            } catch (IOException ignored) {
                // NO-OP
            }
        }
        return false;
    }

    private void setPwmThrottle(Pwm pwm, int throttle) throws IOException {
        if (throttle < POWER_MIN || throttle > POWER_MAX) {
            throw new IllegalArgumentException("Throttle must be between 0 and 100. Throttle: " + throttle);
        }
        if (pwm == null) {
            throw new IllegalStateException("PWM Device not open");
        }
        pwm.setPwmDutyCycle(throttle);
        pwm.setEnabled(throttle != 0);
    }

    public int getServoAngle(int servoId) throws IOException, IllegalStateException {
        return readRegByte(servoId) & 0xFF;
    }

    public void setServoAngle(int servoId, int angle) throws IOException, IllegalStateException {
        Log.d(TAG, "Set servoId " + servoId + " to angle " + angle);
        writeRegByte(PWR_A53_A_REG_SERVO, (byte) servoId);
        writeRegByte(angle, (byte) 255);
    }

    public void resetServo() throws IOException, IllegalStateException {
        writeRegByte(PWR_A53_A_REG_SERVO, (byte) 0);
        writeRegByte(1, (byte) 255);
    }

    public void saveServo() throws IOException, IllegalStateException {
        writeRegByte(PWR_A53_A_REG_SERVO, (byte) 17);
        writeRegByte(1, (byte) 255);
    }

    public int getVoltage() throws IOException, IllegalStateException {
        Log.d(TAG, "Get Voltage ");
        return readRegByte(PWR_A53_A_REG_VOLTAGE) & 0xFF;
    }

    public int getSpeedCounter1() throws IOException, IllegalStateException {
        return readRegByte(PWR_A53_A_REG_SPEED_COUNTER_1) & 0xFF;
    }

    public int getSpeedCounter2() throws IOException, IllegalStateException {
        return readRegByte(PWR_A53_A_REG_SPEED_COUNTER_2) & 0xFF;
    }

    /**
     * Update the value of the LED0 output.
     *
     * @param value true = on, false = off
     */
    public void setLed0Value(boolean value) throws IOException, IllegalStateException {
        setLedValue(PWR_A53_A_LED0, value);
    }

    /**
     * Update the value of the LED1 output.
     *
     * @param value true = on, false = off
     */
    public void setLed1Value(boolean value) throws IOException, IllegalStateException {
        setLedValue(PWR_A53_A_LED1, value);
    }

    /**
     * Update the value of the LED2 output.
     *
     * @param value true = on, false = off
     */
    public void setLed2Value(boolean value) throws IOException, IllegalStateException {
        setLedValue(PWR_A53_A_LED2, value);
    }

    /**
     * Update the value of the LED output.
     */
    private void setLedValue(String ledGpioName, boolean value) throws IOException, IllegalStateException {
        Log.d(TAG, "Set led " + ledGpioName + " to " + (value ? "ON" : "OFF"));
        getGpio(ledGpioName).setValue(value);
    }

    public void motorsForward(int power) throws IOException, IllegalStateException {
        setThrottleMotors(power, power);
    }

    public void motorsBackward(int power) throws IOException, IllegalStateException {
        setThrottleMotors(-power, -power);
    }

    public void motorsTurnLeft(int power) throws IOException, IllegalStateException {
        setThrottleMotors(power, -power);
    }

    public void motorsTurnRight(int power) throws IOException, IllegalStateException {
        setThrottleMotors(-power, power);
    }

    public void motorsStop() throws IOException, IllegalStateException {
        setThrottleMotors(POWER_MIN, POWER_MIN);
    }

    public void setThrottleMotor1(int throttle) throws IOException {
        boolean moveForward = throttle >= 0;
        boolean shouldStop = throttle == 0;
        if (!moveForward) {
            throttle = -throttle;
        }
        if (mUsePwm) {
            setPwmThrottle(mPwmEnA, throttle);
        } else {
            setPwmThrottle(mPwmSwEnA, throttle);
        }
        getGpio(PWR_A53_A_IN1).setValue(moveForward && !shouldStop);
        getGpio(PWR_A53_A_IN2).setValue(!moveForward);
    }

    public void setThrottleMotor2(int throttle) throws IOException {
        boolean moveForward = throttle >= 0;
        boolean shouldStop = throttle == 0;
        if (!moveForward) {
            throttle = -throttle;
        }
        if (mUsePwm) {
            setPwmThrottle(mPwmEnB, throttle);
        } else {
            setPwmThrottle(mPwmSwEnB, throttle);
        }
        getGpio(PWR_A53_A_IN3).setValue(moveForward && !shouldStop);
        getGpio(PWR_A53_A_IN4).setValue(!moveForward);
    }

    public void setThrottleMotors(int throttleMotor1, int throttleMotor2) throws IOException {
        Log.d(TAG, "throttleMotor1 = " + throttleMotor1);
        Log.d(TAG, "throttleMotor2 = " + throttleMotor2);
        setThrottleMotor1(throttleMotor1);
        setThrottleMotor2(throttleMotor2);
    }

    public void motorsMoveToDirection(int angle, int power) throws IOException {
        int throttleMotor1 = 0;
        int throttleMotor2 = 0;
        if (power != 0) {
            if (angle >= 150 && angle < 180) {
                throttleMotor1 = Math.round(scaleValue(angle, 150, 180, POWER_MIN, -POWER_MAX));
                throttleMotor2 = Math.round(scaleValue(angle, 150, 180, POWER_MAX, POWER_MAX));
            } else if (angle >= 90 && angle < 150) {
                throttleMotor1 = Math.round(scaleValue(angle, 90, 150, POWER_MAX, POWER_MIN));
                throttleMotor2 = Math.round(scaleValue(angle, 90, 150, POWER_MAX, POWER_MAX));
            } else if (angle >= 30 && angle < 90) {
                throttleMotor1 = Math.round(scaleValue(angle, 30, 90, POWER_MAX, POWER_MAX));
                throttleMotor2 = Math.round(scaleValue(angle, 30, 90, POWER_MIN, POWER_MAX));
            } else if (angle >= 0 && angle < 30) {
                throttleMotor1 = Math.round(scaleValue(angle, 0, 30, POWER_MAX, POWER_MAX));
                throttleMotor2 = Math.round(scaleValue(angle, 0, 30, -POWER_MAX, POWER_MIN));
            } else if (angle < 0 && angle >= -30) {
                throttleMotor1 = Math.round(scaleValue(angle, 0, -30, POWER_MAX, -POWER_MAX));
                throttleMotor2 = Math.round(scaleValue(angle, 0, -30, -POWER_MAX, POWER_MIN));
            } else if (angle < -30 && angle >= -90) {
                throttleMotor1 = Math.round(scaleValue(angle, -30, -90, -POWER_MAX, -POWER_MAX));
                throttleMotor2 = Math.round(scaleValue(angle, -30, -90, POWER_MIN, -POWER_MAX));
            } else if (angle < -90 && angle >= -150) {
                throttleMotor1 = Math.round(scaleValue(angle, -90, -150, -POWER_MAX, POWER_MIN));
                throttleMotor2 = Math.round(scaleValue(angle, -90, -150, -POWER_MAX, -POWER_MAX));
            } else if (angle < -150 && angle >= -180) {
                throttleMotor1 = Math.round(scaleValue(angle, -150, -180, POWER_MIN, -POWER_MAX));
                throttleMotor2 = Math.round(scaleValue(angle, -150, -180, -POWER_MAX, POWER_MAX));
            }
            throttleMotor1 = Math.round(throttleMotor1 / 100f * power);
            throttleMotor2 = Math.round(throttleMotor2 / 100f * power);
        }
        setThrottleMotors(throttleMotor1, throttleMotor2);
    }

    private float scaleValue(float x, float oldStartRange, float oldEndRange, float newStartRange, float newEndRange) {
        return (((newEndRange - newStartRange) * (x - oldStartRange)) / (oldEndRange - oldStartRange)) + newStartRange;
    }

    private I2cDevice getI2cDevice() throws IllegalStateException {
        if (mI2cDevice == null) {
            throw new IllegalStateException("I2C Device not open");
        }
        return mI2cDevice;
    }

    private Gpio getGpio(String gpioName) throws IllegalStateException {
        Gpio gpio = mGpioMap.get(gpioName);
        if (gpio == null) {
            throw new IllegalStateException("GPIO Device not open");
        }
        return gpio;
    }

    /**
     * Read a byte from a given register.
     *
     * @param reg The register to read from (0x00-0xFF).
     * @return
     */
    private byte readRegByte(int reg) throws IOException {
        if (reg < 0 || reg > 0xFF) {
            throw new IllegalArgumentException("The register must be between 0x00-0xFF. Register:" + reg);
        }
        byte[] buffer = {(byte) (reg & 0xFF)};
        getI2cDevice().write(buffer, 1);
        buffer[0] = 0;
        getI2cDevice().read(buffer, 1);
        return buffer[0];
    }

    /**
     * Write a byte to a given register.
     *
     * @param reg  The register to write to (0x00-0xFF).
     * @param data Value to write
     */
    private void writeRegByte(int reg, byte data) throws IOException {
        if (reg < 0 || reg > 0xFF) {
            throw new IllegalArgumentException("The register must be between 0x00-0xFF. Register:" + reg);
        }
        byte[] buffer = {(byte) (reg & 0xFF), data};
        getI2cDevice().write(buffer, buffer.length);
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() {
        try {
            mI2cDevice.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close PWM device", e);
        }
        if (mUsePwm) {
            try {
                mPwmEnA.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM device", e);
            }
            try {
                mPwmEnB.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close PWM device", e);
            }
        }
        for (Gpio gpio : mGpioMap.values()) {
            try {
                gpio.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close GPIO device", e);
            }
        }

        mI2cDevice = null;
        mGpioMap.clear();
    }
}
