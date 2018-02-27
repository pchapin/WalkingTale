package com.walkingtale;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {
    private static Analytics instance;
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void init(Context context) {
        instance = new Analytics();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static FirebaseAnalytics getInstance() {
        return mFirebaseAnalytics;
    }
}
