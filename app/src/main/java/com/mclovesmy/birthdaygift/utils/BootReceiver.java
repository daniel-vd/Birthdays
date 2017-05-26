package com.mclovesmy.birthdaygift.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            Intent serviceIntent = new Intent(context, ServiceBootReceiver.class);
            context.startService(serviceIntent);

        }
    }

}
