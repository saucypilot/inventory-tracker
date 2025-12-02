package com.example.tallycat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, ReminderService.class);
        context.startService(service);
    }
}