# Android Things Kuman SM9

[![GitHub release](https://img.shields.io/github/release/leinardi/androidthings-kuman-sm9.svg?style=plastic)](https://github.com/leinardi/androidthings-kuman-sm9/releases)
[![Travis](https://img.shields.io/travis/leinardi/androidthings-kuman-sm9/master.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-kuman-sm9)
[![GitHub license](https://img.shields.io/github/license/leinardi/androidthings-kuman-sm9.svg?style=plastic)](https://github.com/leinardi/androidthings-kuman-sm9/blob/master/LICENSE)

This project offer an Android Things driver for the PWR.A53.A board, an 
Android Things app and an Android Mobile app to control the Kuman SM9 car kit.
 
The communication between the Android Things device and the mobile device is 
done using Google Nearby API.

Features of the project:

 - Android Things GPIO and I2C drivers
 - MVVM with data binding and Architecture Component ViewModel
 - Dagger Android Injection
 - RxJava 2
 - Google Nearby API Communication
 
## Roadmap

- [x] Create projects modules and add common frameworks/libraries/tools (Dagger, Rx, MVVM, checkstyle, etc)
- [x] Write Android Things driver for PWR.A53.A (motors, servos and led control via I2C and GPIO)
- [x] Establish a connection between Things app and Mobile app using Google Nearby API
- [x] Adapt Android Things driver for SSD1306 to work with the SH1106
- [x] Add TravisCI integration with Github
- [x] Add project checkstyle and code style configuration
- [x] Add license profile IDEA configuration
- [x] Open a bug report for I2C read not working for PWR.A53.A on Things 0.6.0-devpreviw
- [x] Move the `sh1106` on a separate project and publish on maven
- [ ] Add violation-comments-to-github-gradle-plugin and integrate it with TravisCI
- [x] Define and implement a communication protocol between Things app and Mobile app
- [x] Allow to control the motors via Mobile app
- [x] Allow to control the cradle servos via Mobile app
- [ ] Release first alpha version
- [ ] Create a dev branch and continue the development on that branch
- [ ] Write Android Things driver for HD44780 LCD controller
- [x] Add support for Raspberry Pi Camera
- [ ] Stream camera to Mobile app
- [ ] Add support for HC-SR04 (Ultrasonic Module Distance Measuring Transducer Sensor ) to `pwra53a` 
- [ ] Add support for E18-D80NK (Adjustable Infrared Obstacle Avoidance Detection Sensor) to `pwra53a`
- [ ] Write Android Things driver for Adafruit LSM9DS1 (Accelerometer, Gyro, Magnetometer and Temperature sensor) 
- [x] Show CPU usage and temperature and interfaces IP on SH1106
- [ ] Show current status (stop, moving forward, following path, etc) on HD44780
- [ ] Use HC-SR04 to avoid collisions
- [ ] Use E18-D80NK to implement follow path
- [ ] Use Gyro to give the option to lock the camera on a direction even if the car turns

## Pre-requisites

 - Raspberry Pi 3
 - PWR.A53.A (included in the Kuman SM9 kit)
 - SH1106 OLED Display
 - Android Things 0.6-devpreview or later version
 - Android mobile device to control the car 


## Build and install

Import the project on Android Studio, select the `things-app` to install the 
Android Things application or `mobile-app` for the mobile application, click 
on the "Run" button.

## PWR.A53.A
This board offers the following features:
 - control 2 motors via GPIO
 - access to 5 IR sensors via GPIO
 - access 1 ultrasound sensor via GPIO
 - control 8 servos via I2C

## Where to find the PWR.A53.A

As far as I know this board i sold only in some bundle for the Raspberry Pi 3 
sold by Kuman Ltd or Xiao R Geek Technology Co.Ltd.

In Europe and US it is part of the kit Kuman SM9 and can be bought on 
[Amazon](https://www.amazon.com/Kuman-Professional-Raspberry-Electronic-Controlled/dp/B071L7YGXK/) 
or directly from the 
[Kuman](http://www.kumantech.com/kuman-4wd-smart-wireless-wifi-rc-video-robot-car-kit-for-raspberry-pi-3-intelligent-robotics8g-sd-card-controlled-by-pc-android-ios-app-sm9_p0386.html) website. 
In China is available on 
[Taobao](https://item.taobao.com/item.htm?spm=a1z10.1-c-s.w5003-17403619293.1.2db81832x99BUt&id=546164068526&scene=taobao_shop).

## Licenses

Copyright 2017 Roberto Leinardi.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

The FontStruction "[Graph 35+ pix](https://fontstruct.com/fontstructions/show/664062/graph_35_pix)"
 by [30100flo](https://fontstruct.com/fontstructors/623307/30100flo) is licensed
under a [Creative Commons Attribution Share Alike](http://creativecommons.org/licenses/by-sa/3.0/) license.
