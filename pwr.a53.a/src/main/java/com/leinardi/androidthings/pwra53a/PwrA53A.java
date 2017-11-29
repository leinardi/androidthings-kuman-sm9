package com.leinardi.androidthings.pwra53a;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;

public class PwrA53A implements AutoCloseable {
    private static final String TAG = PwrA53A.class.getSimpleName();
    /**
     * Default I2C address for the sensor.
     */
    private static final int DEFAULT_I2C_ADDRESS = 0x17;

    private static final String DEFAULT_I2C_BUS_NAME = "I2C1";

    // Registers
    private static final int PWR_A53_A_REG_SERVO = 0xFF;
    private static final int PWR_A53_A_REG_VOLTAGE = 0x12;
    private static final int PWR_A53_A_REG_SPEED_COUNTER_1 = 0x13;
    private static final int PWR_A53_A_REG_SPEED_COUNTER_2 = 0x14;

    // GPIO LEDs
    private static final String PWR_A53_A_LED0 = "BCM10";
    private static final String PWR_A53_A_LED1 = "BCM9";
    private static final String PWR_A53_A_LED2 = "BCM25";

    //GPIO Motors
    private static final String PWR_A53_A_ENA = "BCM13";
    private static final String PWR_A53_A_ENB = "BCM20";
    private static final String PWR_A53_A_IN1 = "BCM19";
    private static final String PWR_A53_A_IN2 = "BCM16";
    private static final String PWR_A53_A_IN3 = "BCM21";
    private static final String PWR_A53_A_IN4 = "BCM26";

    //GPIO Servos
    private static final String PWR_A53_A_SER1 = "BCM11";
    private static final String PWR_A53_A_SER2 = "BCM8";
    private static final String PWR_A53_A_SER3 = "BCM7";
    private static final String PWR_A53_A_SER4 = "BCM5";
    private static final String PWR_A53_A_SER7 = "BCM6"; // Vertical servo
    private static final String PWR_A53_A_SER8 = "BCM12"; // Horizontal servo

    // GPIO Ultrasound
    private static final String PWR_A53_A_ECHO = "BCM4";
    private static final String PWR_A53_A_TRIG = "BCM17";

    // GPIO Infrared
    private static final String PWR_A53_A_IR_R = "BCM18";
    private static final String PWR_A53_A_IR_L = "BCM27";
    private static final String PWR_A53_A_IR_M = "BCM22";
    private static final String PWR_A53_A_IRF_R = "BCM23";
    private static final String PWR_A53_A_IRF_L = "BCM24";

    private I2cDevice mDevice;
    private Gpio mLed0;
    private Gpio mLed1;
    private Gpio mLed2;

    private Gpio mEnA;
    private Gpio mIn1;
    private Gpio mIn2;
    private Gpio mEnB;
    private Gpio mIn3;
    private Gpio mIn4;

    private Gpio mInfraredRight;
    private Gpio mInfraredLeft;
    private Gpio mInfraredMiddle;
    private Gpio mInfraredFollowRight;
    private Gpio mInfraredFollowLeft;

    private Gpio mTrig;
    private Gpio mEcho;

    /**
     * Create a new PWR.A53.A sensor driver connected on the given bus.
     *
     * @throws IOException
     */
    public PwrA53A() throws IOException {
        try {
            connect();
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    public static int byteToUnsignedInt(byte x) {
        return x & 0xFF;
    }

    private void connect() throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();

        mDevice = pioService.openI2cDevice(DEFAULT_I2C_BUS_NAME, DEFAULT_I2C_ADDRESS);

        mLed0 = pioService.openGpio(PWR_A53_A_LED0);
        mLed1 = pioService.openGpio(PWR_A53_A_LED1);
        mLed2 = pioService.openGpio(PWR_A53_A_LED2);

        mEnA = pioService.openGpio(PWR_A53_A_ENA);
        mIn1 = pioService.openGpio(PWR_A53_A_IN1);
        mIn2 = pioService.openGpio(PWR_A53_A_IN2);
        mEnB = pioService.openGpio(PWR_A53_A_ENB);
        mIn3 = pioService.openGpio(PWR_A53_A_IN3);
        mIn4 = pioService.openGpio(PWR_A53_A_IN4);

        mInfraredRight = pioService.openGpio(PWR_A53_A_IR_R);
        mInfraredLeft = pioService.openGpio(PWR_A53_A_IR_L);
        mInfraredMiddle = pioService.openGpio(PWR_A53_A_IR_M);
        mInfraredFollowRight = pioService.openGpio(PWR_A53_A_IRF_R);
        mInfraredFollowLeft = pioService.openGpio(PWR_A53_A_IRF_L);

        mTrig = pioService.openGpio(PWR_A53_A_TRIG);
        mEcho = pioService.openGpio(PWR_A53_A_ECHO);

        // Setup GPIO
        mLed0.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        mLed1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        mLed2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        mEnA.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mIn1.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mIn2.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mEnB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mIn3.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mIn4.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        mInfraredRight.setDirection(Gpio.DIRECTION_IN);
        mInfraredRight.setActiveType(Gpio.ACTIVE_HIGH);
        mInfraredLeft.setDirection(Gpio.DIRECTION_IN);
        mInfraredLeft.setActiveType(Gpio.ACTIVE_HIGH);
        mInfraredMiddle.setDirection(Gpio.DIRECTION_IN);
        mInfraredMiddle.setActiveType(Gpio.ACTIVE_HIGH);
        mInfraredFollowRight.setDirection(Gpio.DIRECTION_IN);
        mInfraredFollowRight.setActiveType(Gpio.ACTIVE_HIGH);
        mInfraredFollowLeft.setDirection(Gpio.DIRECTION_IN);
        mInfraredFollowLeft.setActiveType(Gpio.ACTIVE_HIGH);

        mTrig.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        mEcho.setDirection(Gpio.DIRECTION_IN);
        mEcho.setActiveType(Gpio.ACTIVE_HIGH);
    }

    public void getServoAngle(int servoId) throws IOException {
        mDevice.readRegByte(servoId);
    }

    public void setServo(int servoId, int angle) throws IOException {
        mDevice.writeRegByte(PWR_A53_A_REG_SERVO, (byte) servoId);
        mDevice.writeRegByte(angle, (byte) 255);
    }

    public void resetServo() throws IOException {
        mDevice.writeRegByte(PWR_A53_A_REG_SERVO, (byte) 0);
        mDevice.writeRegByte(1, (byte) 255);
    }

    public void saveServo() throws IOException {
        mDevice.writeRegByte(PWR_A53_A_REG_SERVO, (byte) 17);
        mDevice.writeRegByte(1, (byte) 255);
    }

    public int getVoltage() throws IOException {
        return byteToUnsignedInt(mDevice.readRegByte(PWR_A53_A_REG_VOLTAGE));
    }

    public int getSpeedCounter1() throws IOException {
        return byteToUnsignedInt(mDevice.readRegByte(PWR_A53_A_REG_SPEED_COUNTER_1));
    }

    public int getSpeedCounter2() throws IOException {
        return byteToUnsignedInt(mDevice.readRegByte(PWR_A53_A_REG_SPEED_COUNTER_2));
    }

    /**
     * Update the value of the LED0 output.
     *
     * @param value true = on, false = off
     */
    public void setLed0Value(boolean value) {
        setLedValue(mLed0, value);
    }

    /**
     * Update the value of the LED1 output.
     *
     * @param value true = on, false = off
     */
    public void setLed1Value(boolean value) {
        setLedValue(mLed1, value);
    }

    /**
     * Update the value of the LED2 output.
     *
     * @param value true = on, false = off
     */
    public void setLed2Value(boolean value) {
        setLedValue(mLed2, value);
    }

    /**
     * Update the value of the LED output.
     */
    private void setLedValue(Gpio led, boolean value) {
        try {
            Log.d(TAG, "Set led to " + (value ? "ON" : "OFF"));
            led.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    public void motorForward() {
        try {
            Log.d(TAG, "Motor Forward");
            mEnA.setValue(true);
            mEnB.setValue(true);
            mIn1.setValue(true);
            mIn2.setValue(false);
            mIn3.setValue(true);
            mIn4.setValue(false);
            mLed1.setValue(false);
            mLed2.setValue(false);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    public void motorBackward() {
        try {
            Log.d(TAG, "Motor Backward");
            mEnA.setValue(true);
            mEnB.setValue(true);
            mIn1.setValue(false);
            mIn2.setValue(true);
            mIn3.setValue(false);
            mIn4.setValue(true);
            mLed1.setValue(true);
            mLed2.setValue(false);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    public void motorTurnLeft() {
        try {
            Log.d(TAG, "Motor Turn Left");
            mEnA.setValue(true);
            mEnB.setValue(true);
            mIn1.setValue(true);
            mIn2.setValue(false);
            mIn3.setValue(false);
            mIn4.setValue(true);
            mLed1.setValue(false);
            mLed2.setValue(true);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    public void motorTurnRight() {
        try {
            Log.d(TAG, "Motor Turn Right");
            mEnA.setValue(true);
            mEnB.setValue(true);
            mIn1.setValue(false);
            mIn2.setValue(true);
            mIn3.setValue(true);
            mIn4.setValue(false);
            mLed1.setValue(false);
            mLed2.setValue(true);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    public void motorStop() {
        try {
            Log.d(TAG, "Motor Stop");
            mEnA.setValue(false);
            mEnB.setValue(true);
            mIn1.setValue(false);
            mIn2.setValue(false);
            mIn3.setValue(false);
            mIn4.setValue(false);
            mLed1.setValue(true);
            mLed2.setValue(true);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        close(mDevice);
        close(mLed0);
        close(mLed1);
        close(mLed2);

        close(mEnA);
        close(mIn1);
        close(mIn2);
        close(mEnB);
        close(mIn3);
        close(mIn4);

        close(mInfraredRight);
        close(mInfraredLeft);
        close(mInfraredMiddle);
        close(mInfraredFollowRight);
        close(mInfraredFollowLeft);

        close(mTrig);
        close(mEcho);

        mDevice = null;

        mLed0 = null;
        mLed1 = null;
        mLed2 = null;

        mEnA = null;
        mIn1 = null;
        mIn2 = null;
        mEnB = null;
        mIn3 = null;
        mIn4 = null;

        mInfraredRight = null;
        mInfraredLeft = null;
        mInfraredMiddle = null;
        mInfraredFollowRight = null;
        mInfraredFollowLeft = null;

        mTrig = null;
        mEcho = null;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.w(TAG, "Unable to close IoBase", e);
            }
        }
    }
}
