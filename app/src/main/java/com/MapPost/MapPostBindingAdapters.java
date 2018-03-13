package com.MapPost;

import android.databinding.BindingAdapter;
import android.view.View;

public class MapPostBindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
