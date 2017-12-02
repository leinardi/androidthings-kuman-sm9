Android Things Kuman SM9
=====================================

[![GitHub release](https://img.shields.io/github/release/leinardi/androidthings-kuman-sm9.svg?style=plastic)](https://github.com/leinardi/androidthings-kuman-sm9/releases)
[![Travis](https://img.shields.io/travis/leinardi/androidthings-kuman-sm9/dev.svg?style=plastic)](https://travis-ci.org/leinardi/androidthings-kuman-sm9)
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

Pre-requisites
--------------

- Raspberry Pi 3
- PWR.A53.A (included in the Kuman SM9 kit)
- SH1106 OLED Display
- Android Things 0.6-devpreview or later version
- Android mobile device to control the car 


Build and install
=================

Import the project on Android Studio, select the `things-app` to install the 
Android Things application or `mobile-app` for the mobile application, click 
on the "Run" button.


Where to find the PWR.A53.A
===========================

As far as I know this board i sold only in some bundle for the Raspberry Pi 3 
sold by Kuman Ltd or Xiao R Geek Technology Co.Ltd.

In Europe and US the is part of the kit Kuman SM9 and can be bought on 
[Amazon](https://www.amazon.com/Kuman-Professional-Raspberry-Electronic-Controlled/dp/B071L7YGXK/) 
or directly from the 
[Kuman](http://www.kumantech.com/kuman-4wd-smart-wireless-wifi-rc-video-robot-car-kit-for-raspberry-pi-3-intelligent-robotics8g-sd-card-controlled-by-pc-android-ios-app-sm9_p0386.html) website. 
In China is available on 
[Taobao](https://item.taobao.com/item.htm?spm=a1z10.1-c-s.w5003-17403619293.1.2db81832x99BUt&id=546164068526&scene=taobao_shop).

License
-------

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
