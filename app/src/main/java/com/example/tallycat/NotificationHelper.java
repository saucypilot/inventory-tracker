package com.example.tallycat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "inventory_reminder_channel";
    private static final String CHANNEL_NAME = "Inventory Reminders";
    private static int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for checked out inventory items");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.enableLights(true);
            channel.setLightColor(0xFF6200EE);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // New method for return time reminders
    public static void showReturnReminder(Context context, String itemName, String returnTime, String checkoutTime) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        Intent intent = new Intent(context, UserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent searchIntent = new Intent(context, SearchActivity.class);
        PendingIntent searchPendingIntent = PendingIntent.getActivity(
                context,
                1,
                searchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_inventory_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_info_details))
                .setContentTitle("Item Return Reminder")
                .setContentText("Time to return: " + itemName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Please return \"" + itemName + "\" by " + returnTime + ". You checked out this item at " + checkoutTime + "."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_search, "Search Items", searchPendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(NOTIFICATION_ID++, builder.build());
    }

    // Method for manual notifications
    public static void showManualNotification(Context context, String message, String itemName) {
        if (!areNotificationsEnabled(context)) {
            return;
        }

        Intent intent = new Intent(context, UserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_inventory_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_info_details))
                .setContentTitle("Return Item Reminder")
                .setContentText(itemName + " - Return Request")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID++, builder.build());
    }

    // Keep old method for backward compatibility (if needed elsewhere)
    public static void showCheckoutReminder(Context context, String itemName, String checkoutTime, int reminderHours) {
        showReturnReminder(context, itemName, "the end of day", checkoutTime);
    }

    // Method to check user notification preferences
    private static boolean areNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("notifications_enabled", true);
    }
}