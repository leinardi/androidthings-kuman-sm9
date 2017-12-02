package com.leinardi.androidthings.driver.pwra53a;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PwrA53A implements AutoCloseable {
    private static final String TAG = PwrA53A.class.getSimpleName();
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
    public static final int SERVO_ID_7 = 7;
    public static final int SERVO_ID_8 = 8;

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

    private I2cDevice mI2cDevice;
    private final Map<String, Gpio> mGpioMap;

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
            } catch (IOException | RuntimeException ignored) {
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

        mGpioMap.put(PWR_A53_A_ENA, pioService.openGpio(PWR_A53_A_ENA));
        mGpioMap.put(PWR_A53_A_IN1, pioService.openGpio(PWR_A53_A_IN1));
        mGpioMap.put(PWR_A53_A_IN2, pioService.openGpio(PWR_A53_A_IN2));
        mGpioMap.put(PWR_A53_A_ENB, pioService.openGpio(PWR_A53_A_ENB));
        mGpioMap.put(PWR_A53_A_IN3, pioService.openGpio(PWR_A53_A_IN3));
        mGpioMap.put(PWR_A53_A_IN4, pioService.openGpio(PWR_A53_A_IN4));

        mGpioMap.put(PWR_A53_A_IR_R, pioService.openGpio(PWR_A53_A_IR_R));
        mGpioMap.put(PWR_A53_A_IR_L, pioService.openGpio(PWR_A53_A_IR_L));
        mGpioMap.put(PWR_A53_A_IR_M, pioService.openGpio(PWR_A53_A_IR_M));
        mGpioMap.put(PWR_A53_A_IRF_R, pioService.openGpio(PWR_A53_A_IRF_R));
        mGpioMap.put(PWR_A53_A_IRF_L, pioService.openGpio(PWR_A53_A_IRF_L));

        mGpioMap.put(PWR_A53_A_TRIG, pioService.openGpio(PWR_A53_A_TRIG));
        mGpioMap.put(PWR_A53_A_ECHO, pioService.openGpio(PWR_A53_A_ECHO));

        // Setup GPIO
        getGpio(PWR_A53_A_LED0).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        getGpio(PWR_A53_A_LED1).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        getGpio(PWR_A53_A_LED2).setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);

        getGpio(PWR_A53_A_ENA).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_IN1).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_IN2).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        getGpio(PWR_A53_A_ENB).setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
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

    public int getServoAngle(int servoId) throws IOException, IllegalStateException {
        return byteToUnsignedInt(getI2cDevice().readRegByte(servoId));
    }

    public void setServoAngle(int servoId, int angle) throws IOException, IllegalStateException {
        Log.d(TAG, "Set servoId " + servoId + " to angle " + angle);
        getI2cDevice().writeRegByte(PWR_A53_A_REG_SERVO, (byte) servoId);
        getI2cDevice().writeRegByte(angle, (byte) 255);
    }

    public void resetServo() throws IOException, IllegalStateException {
        getI2cDevice().writeRegByte(PWR_A53_A_REG_SERVO, (byte) 0);
        getI2cDevice().writeRegByte(1, (byte) 255);
    }

    public void saveServo() throws IOException, IllegalStateException {
        getI2cDevice().writeRegByte(PWR_A53_A_REG_SERVO, (byte) 17);
        getI2cDevice().writeRegByte(1, (byte) 255);
    }

    public int getVoltage() throws IOException, IllegalStateException {
        Log.d(TAG, "Get Voltage ");
        return byteToUnsignedInt(getI2cDevice().readRegByte(PWR_A53_A_REG_VOLTAGE));
    }

    public int getSpeedCounter1() throws IOException, IllegalStateException {
        return byteToUnsignedInt(getI2cDevice().readRegByte(PWR_A53_A_REG_SPEED_COUNTER_1));
    }

    public int getSpeedCounter2() throws IOException, IllegalStateException {
        return byteToUnsignedInt(getI2cDevice().readRegByte(PWR_A53_A_REG_SPEED_COUNTER_2));
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

    public void motorForward() throws IOException, IllegalStateException {
        Log.d(TAG, "Motor Forward");
        getGpio(PWR_A53_A_ENA).setValue(true);
        getGpio(PWR_A53_A_IN1).setValue(true);
        getGpio(PWR_A53_A_IN2).setValue(false);
        getGpio(PWR_A53_A_ENB).setValue(true);
        getGpio(PWR_A53_A_IN3).setValue(true);
        getGpio(PWR_A53_A_IN4).setValue(false);
        getGpio(PWR_A53_A_LED1).setValue(false);
        getGpio(PWR_A53_A_LED2).setValue(false);
    }

    public void motorBackward() throws IOException, IllegalStateException {
        Log.d(TAG, "Motor Backward");
        getGpio(PWR_A53_A_ENA).setValue(true);
        getGpio(PWR_A53_A_IN1).setValue(true);
        getGpio(PWR_A53_A_IN2).setValue(false);
        getGpio(PWR_A53_A_ENB).setValue(true);
        getGpio(PWR_A53_A_IN3).setValue(false);
        getGpio(PWR_A53_A_IN4).setValue(true);
        getGpio(PWR_A53_A_LED1).setValue(true);
        getGpio(PWR_A53_A_LED2).setValue(false);
    }

    public void motorTurnLeft() throws IOException, IllegalStateException {
        Log.d(TAG, "Motor Turn Left");
        getGpio(PWR_A53_A_ENA).setValue(true);
        getGpio(PWR_A53_A_IN1).setValue(true);
        getGpio(PWR_A53_A_IN2).setValue(true);
        getGpio(PWR_A53_A_ENB).setValue(false);
        getGpio(PWR_A53_A_IN3).setValue(false);
        getGpio(PWR_A53_A_IN4).setValue(true);
        getGpio(PWR_A53_A_LED1).setValue(false);
        getGpio(PWR_A53_A_LED2).setValue(true);
    }

    public void motorTurnRight() throws IOException, IllegalStateException {
        Log.d(TAG, "Motor Turn Right");
        getGpio(PWR_A53_A_ENA).setValue(true);
        getGpio(PWR_A53_A_IN1).setValue(true);
        getGpio(PWR_A53_A_IN2).setValue(false);
        getGpio(PWR_A53_A_ENB).setValue(true);
        getGpio(PWR_A53_A_IN3).setValue(true);
        getGpio(PWR_A53_A_IN4).setValue(false);
        getGpio(PWR_A53_A_LED1).setValue(false);
        getGpio(PWR_A53_A_LED2).setValue(true);
    }

    public void motorStop() throws IOException, IllegalStateException {
        Log.d(TAG, "Motor Stop");
        getGpio(PWR_A53_A_ENA).setValue(false);
        getGpio(PWR_A53_A_IN1).setValue(true);
        getGpio(PWR_A53_A_IN2).setValue(false);
        getGpio(PWR_A53_A_ENB).setValue(false);
        getGpio(PWR_A53_A_IN3).setValue(false);
        getGpio(PWR_A53_A_IN4).setValue(false);
        getGpio(PWR_A53_A_LED1).setValue(true);
        getGpio(PWR_A53_A_LED2).setValue(true);
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

    public static int byteToUnsignedInt(byte x) {
        return x & 0xFF;
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        try {
            mI2cDevice.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close I2C device", e);
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
