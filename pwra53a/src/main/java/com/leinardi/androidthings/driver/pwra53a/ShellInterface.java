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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface to the Superuser shell on Android devices with some helper functions.<p/><p/>
 * Common usage for su shell:<p/>
 * <code>if(ShellInterface.isSuAvailable()) { ShellInterface.runCommand("reboot"); }</code>
 * <p/><p/>
 * To get process output as a String:<p/>
 * <code>if(ShellInterface.isSuAvailable()) { String date = ShellInterface.getProcessOutput("date"); }</code>
 * <p/><p/>
 * To run command with standard shell (no root permissions):
 * <code>ShellInterface.setShell("sh");</code><p/>
 * <code>ShellInterface.runCommand("date");</code>
 * <p/><p/>
 * Date: Mar 24, 2010
 * Time: 4:14:07 PM
 *
 * @author serge
 */
public class ShellInterface {
    private static final String TAG = ShellInterface.class.getSimpleName();

    private static String sString;

    public static final String ROOT = "su";
    public static final String STANDARD = "sh";

    // uid=0(root) gid=0(root)
    private static final Pattern UID_PATTERN = Pattern.compile("^uid=(\\d+).*?");

    enum OutputType {
        STDOUT,
        STDERR,
        BOTH
    }

    private static final String EXIT = "exit\n";

    private static final String[] SU_COMMANDS = new String[]{
            "su",
            "/system/xbin/su",
            "/system/bin/su"
    };

    private static final String[] TEST_COMMANDS = new String[]{
            "id",
            "/system/xbin/id",
            "/system/bin/id"
    };

    private ShellInterface() {
    }

    public static synchronized boolean isSuAvailable() {
        sString = null;
        checkSu();

        return sString != null;
    }

    public static synchronized void setShell(String shell) {
        ShellInterface.sString = shell;
    }

    private static boolean checkSu() {
        for (String command : SU_COMMANDS) {
            sString = command;
            if (isRootUid()) {
                return true;
            }
        }
        sString = null;
        return false;
    }

    private static boolean isRootUid() {
        String out = null;
        for (String command : TEST_COMMANDS) {
            out = getProcessOutput(command);
            if (out != null && out.length() > 0) {
                break;
            }
        }
        if (out == null || out.length() == 0) {
            return false;
        }
        Matcher matcher = UID_PATTERN.matcher(out);
        if (matcher.matches()) {
            if ("0".equals(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    public static String getProcessOutput(String command) {
        try {
            return runCommand(command, OutputType.STDERR);
        } catch (IOException ignored) {
            return null;
        }
    }

    public static boolean runCommand(String command) {
        try {
            runCommand(command, OutputType.BOTH);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static String runCommand(String command, OutputType o) throws IOException {
        DataOutputStream os = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(sString);
            os = new DataOutputStream(process.getOutputStream());
            InputStreamHandler sh = sinkProcessOutput(process, o);
            os.writeBytes(command + '\n');
            os.flush();
            os.writeBytes(EXIT);
            os.flush();
            process.waitFor();
            if (sh != null) {
                String output = sh.getOutput();
                Log.d(TAG, command + " output: " + output);
                return output;
            } else {
                return null;
            }
        } catch (Exception e) {
            final String msg = e.getMessage();
            Log.e(TAG, "runCommand error: " + msg);
            throw new IOException(msg);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static InputStreamHandler sinkProcessOutput(Process p, OutputType o) {
        InputStreamHandler output = null;
        switch (o) {
            case STDOUT:
                output = new InputStreamHandler(p.getErrorStream(), false);
                new InputStreamHandler(p.getInputStream(), true);
                break;
            case STDERR:
                output = new InputStreamHandler(p.getInputStream(), false);
                new InputStreamHandler(p.getErrorStream(), true);
                break;
            case BOTH:
                new InputStreamHandler(p.getInputStream(), true);
                new InputStreamHandler(p.getErrorStream(), true);
                break;
        }
        return output;
    }

    private static class InputStreamHandler extends Thread {
        private final InputStream mStream;
        private final boolean mSink;
        private StringBuffer mOutput;

        String getOutput() {
            return mOutput.toString();
        }

        InputStreamHandler(InputStream stream, boolean sink) {
            this.mSink = sink;
            this.mStream = stream;
            start();
        }

        @Override
        public void run() {
            try {
                if (mSink) {
                    while (mStream.read() != -1) {
                    }
                } else {
                    mOutput = new StringBuffer();
                    BufferedReader b = new BufferedReader(new InputStreamReader(mStream));
                    String s;
                    while ((s = b.readLine()) != null) {
                        mOutput.append(s);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }
}
