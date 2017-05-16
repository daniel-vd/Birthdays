package com.mclovesmy.birthdaygift.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service1 = new Intent(context, AlarmService.class);
        context.startService(service1);
        // For our recurring task, we'll just display a message
    }
}
