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

package com.leinardi.androidthings.kuman.sm9.util;

import android.app.Application;
import android.text.format.DateFormat;

import timber.log.Timber;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

@Singleton
public class SystemHelper {

    private static final int TOP_CPU_COLUMN = 0;  // "400%cpu"
    private static final int TOP_USER_COLUMN = 1; // "20%user"
    private static final int TOP_NICE_COLUMN = 2; // "0%nice"
    private static final int TOP_SYS_COLUMN = 3;  // "12%sys"
    private static final int TOP_IDLE_COLUMN = 4; // "364%idle"
    private static final int TOP_IOW_COLUMN = 5;  // "0%iow"
    private static final int TOP_IRQ_COLUMN = 6;  // "0%irq"
    private static final int TOP_SIRQ_COLUMN = 7; // "4%sirq"
    private static final int TOP_HOST_COLUMN = 8; // "0%host"
    private Application mApplication;

    @Inject
    public SystemHelper(Application application) {
        mApplication = application;
    }

    public String getTime() {
        return DateFormat.getTimeFormat(mApplication).format(new Date());
    }

    public Integer getCpuUsage() {
        Integer usage = null;
        try {
            // Run the command
            Process process = Runtime.getRuntime().exec("top -n 1");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Grab the results
            List<String> topStrings = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                topStrings.add(line);
            }

            for (String topString : topStrings) {
                if (topString.contains("%cpu ")) {
                    usage = getUsageFromTopString(topString);
                    break;
                }
            }
        } catch (IOException e) {
            Timber.e(e);
        }

        return usage;
    }

    private int getUsageFromTopString(String topString) {
        String[] split = topString.split("\\s+");
        float totalCPU = Float.parseFloat(split[TOP_CPU_COLUMN].substring(0, split[TOP_CPU_COLUMN].indexOf('%')));
        float idleCPU = Float.parseFloat(split[TOP_IDLE_COLUMN].substring(0, split[TOP_IDLE_COLUMN].indexOf('%')));
        return Math.round(((idleCPU / totalCPU) - 1) * 100 * -1);
    }

    public Integer getCpuTemperature() {
        Integer temp = null;
        try (RandomAccessFile reader = new RandomAccessFile("/sys/class/thermal/thermal_zone0/temp", "r")) {
            temp = Math.round(Integer.parseInt(reader.readLine()) / 1000f);
        } catch (IOException e) {
            Timber.e(e);
        }
        return temp;
    }

    public List<String> getNetworkInterfaceAdresses() {
        Enumeration<NetworkInterface> networkInterfaceEnumeration;
        List<String> networkInterfaceAddressesList = new ArrayList<>();
        try {
            networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses();
                while (inetAddressEnumeration.hasMoreElements()) {
                    InetAddress inetAddress = inetAddressEnumeration.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        networkInterfaceAddressesList.add(
                                String.format("%5s %s",
                                        networkInterface.getDisplayName(),
                                        inetAddress.getHostAddress()));
                    }
                }
            }
        } catch (SocketException e) {
            Timber.e(e);
        }
        return networkInterfaceAddressesList;
    }
}
