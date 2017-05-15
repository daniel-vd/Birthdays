package com.mclovesmy.birthdaygift.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, AlarmService.class);
        startWakefulService(context, service);
        // For our recurring task, we'll just display a message
    }
}
