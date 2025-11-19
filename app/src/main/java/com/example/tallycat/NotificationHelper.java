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
    private static final int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Reminders for checked out inventory items");
        // Vibration and light settings
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
        channel.enableLights(true);
        channel.setLightColor(0xFF6200EE); // Purple color

        NotificationManager notificationManager =
                context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    // New method with checkout time parameter
    public static void showCheckoutReminder(Context context, String itemName, String checkoutTime) {
        // Check if notifications are enabled
        if (!areNotificationsEnabled(context)) {
            return;
        }

        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, UserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create action button for search
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
                .setContentTitle("Item Checkout Reminder")
                // Improved content text
                .setContentText("8 hours since checkout: " + itemName)
                // Expandable big text style
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("It's been 8 hours since you checked out \"" + itemName + "\" on " + checkoutTime + ". Please remember to return the item."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                // Action button
                .addAction(android.R.drawable.ic_menu_search, "Search Items", searchPendingIntent)
                .setAutoCancel(true)
                // Only alert once and vibration
                .setOnlyAlertOnce(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                // Use default sound and lights
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Keep the original method for backward compatibility
    public static void showCheckoutReminder(Context context, String itemName) {
        showCheckoutReminder(context, itemName, "previously");
    }

    // Method to check user notification preferences
    private static boolean areNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("notifications_enabled", true);
    }
}