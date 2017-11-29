package com.leinardi.androidthings.kuman.sm9.remote.ui;

import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.widget.TextView;

public class BindingAdapters {
    private BindingAdapters() {
    }

    @BindingAdapter("connectionStatus")
    public static void setVisibility(TextView textView, @MainViewModelObservable.ConnectionStatus @DrawableRes int connectionStatus) {
        textView.setCompoundDrawablesWithIntrinsicBounds(connectionStatus, 0, 0, 0);
    }
}
