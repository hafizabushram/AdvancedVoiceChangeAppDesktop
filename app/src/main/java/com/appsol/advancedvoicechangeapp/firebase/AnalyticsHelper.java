package com.appsol.advancedvoicechangeapp.firebase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsHelper {

    private static AnalyticsHelper analyticsHelper = null;
    private final FirebaseAnalytics instance;

    private AnalyticsHelper(FirebaseAnalytics instance) {
        this.instance = instance;
    }

    public static AnalyticsHelper getAnalyticsHelper(Context context) {
        return analyticsHelper == null ?
                analyticsHelper = new AnalyticsHelper(FirebaseAnalytics.getInstance(context.getApplicationContext())) :
                analyticsHelper;
    }

    public void logScreenEvent(String eventIdentifier, Context context) {
        instance.setCurrentScreen((Activity) context, eventIdentifier, "Opened");
    }

    public void logTapEvent(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "Tapped");
        instance.logEvent(eventName, bundle);
    }
}