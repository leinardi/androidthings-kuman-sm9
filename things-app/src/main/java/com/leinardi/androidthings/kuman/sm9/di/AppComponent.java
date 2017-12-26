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

package com.leinardi.androidthings.kuman.sm9.di;

import android.app.Application;
import android.support.annotation.Nullable;

import com.leinardi.androidthings.driver.lsm9ds1.Lsm9ds1;
import com.leinardi.androidthings.driver.pwra53a.PwrA53A;
import com.leinardi.androidthings.driver.sh1106.Sh1106;
import com.leinardi.androidthings.kuman.sm9.ThingsApp;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AndroidThingsModule.class,
        ApiModule.class,
        AppModule.class,
        CarServiceModule.class,
        MainActivityModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        @BindsInstance
        Builder pwra53a(@Nullable PwrA53A pwrA53A);

        @BindsInstance
        Builder sh1106(@Nullable Sh1106 sh1106);

        @BindsInstance
        Builder lsm9ds1(@Nullable Lsm9ds1 lsm9ds1);

        AppComponent build();
    }

    void inject(ThingsApp target);
}
