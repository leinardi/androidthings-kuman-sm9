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

package com.leinardi.androidthings.kuman.sm9.remote.ui;

import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.widget.TextView;

import com.leinardi.androidthings.kuman.sm9.remote.api.GoogleApiClientRepository.ConnectionStatus;

public class BindingAdapters {
    private BindingAdapters() {
    }

    @BindingAdapter("connectionStatus")
    public static void setVisibility(TextView textView, @ConnectionStatus @DrawableRes int connectionStatus) {
        textView.setCompoundDrawablesWithIntrinsicBounds(connectionStatus, 0, 0, 0);
    }
}
