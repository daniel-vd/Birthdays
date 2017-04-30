package com.mclovesmy.birthdaygift.utils;

import android.app.Application;

import com.flurry.android.FlurryAgent;
import com.mclovesmy.birthdaygift.BuildConfig;

public class ApplicationClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withListener(null)
                .build(this, BuildConfig.FlurryKey + "");
    }

}
