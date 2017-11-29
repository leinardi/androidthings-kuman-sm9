package com.leinardi.androidthings.kuman.sm9.common.ui;

import android.databinding.BindingAdapter;
import android.view.View;

public class BindingAdapters {
    private BindingAdapters() {
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, Boolean value) {
        view.setVisibility(value ? View.VISIBLE : View.GONE);
    }
}
