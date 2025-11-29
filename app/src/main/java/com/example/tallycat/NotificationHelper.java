package com.example.tallycat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "inventory_reminder_channel";
    private static final String CHANNEL_NAME = "Inventory Reminders";
    private static int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                NotificationManager notificationManager =
                        context.getSystemService(NotificationManager.class);

                // Check if channel already exists
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
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

                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created successfully");
                } else {
                    Log.d(TAG, "Notification channel already exists");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error creating notification channel: " + e.getMessage());
            }
        }
    }

    public static Notification createForegroundNotification(Context context) {
        createNotificationChannel(context); // Ensure channel is created

        Intent notificationIntent = new Intent(context, UserActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Checkout Reminder Service")
                .setContentText("Monitoring your item checkouts.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();
    }

    // New method for return time reminders
    public static void showReturnReminder(Context context, String itemName, String returnTime, String checkoutTime) {
        Log.d(TAG, "Attempting to show return reminder for: " + itemName);

        if (!areSystemNotificationsEnabled(context)) {
            Log.d(TAG, "System notifications are disabled");
            return;
        }

        if (!areNotificationsEnabledInPrefs(context)) {
            Log.d(TAG, "App notifications are disabled in preferences");
            return;
        }

        try {
            // Ensure channel is created
            createNotificationChannel(context);

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

            // Use app icon if custom icon isn't available
            int smallIcon = context.getResources().getIdentifier("ic_inventory_notification", "drawable", context.getPackageName());
            if (smallIcon == 0) {
                smallIcon = android.R.drawable.ic_dialog_info;
                Log.d(TAG, "Using default icon, custom icon not found");
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(smallIcon)
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

            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID++, builder.build());
                Log.d(TAG, "Return reminder notification shown successfully");
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing return reminder: " + e.getMessage());
        }
    }

    // Method for manual notifications
    public static void showManualNotification(Context context, String message, String itemName) {
        Log.d(TAG, "Attempting to show manual notification for: " + itemName);

        if (!areSystemNotificationsEnabled(context)) {
            Log.d(TAG, "System notifications are disabled");
            return;
        }

        if (!areNotificationsEnabledInPrefs(context)) {
            Log.d(TAG, "App notifications are disabled in preferences");
            return;
        }

        try {
            // Ensure channel is created
            createNotificationChannel(context);

            Intent intent = new Intent(context, UserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Use app icon if custom icon isn't available
            int smallIcon = context.getResources().getIdentifier("ic_inventory_notification", "drawable", context.getPackageName());
            if (smallIcon == 0) {
                smallIcon = android.R.drawable.ic_dialog_info;
                Log.d(TAG, "Using default icon, custom icon not found");
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(smallIcon)
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

            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID++, builder.build());
                Log.d(TAG, "Manual notification shown successfully");
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing manual notification: " + e.getMessage());
        }
    }

    // Keep old method for backward compatibility (if needed elsewhere)
    public static void showCheckoutReminder(Context context, String itemName, String checkoutTime, int reminderHours) {
        showReturnReminder(context, itemName, "the end of day", checkoutTime);
    }

    // Method to check user notification preferences from SharedPreferences
    private static boolean areNotificationsEnabledInPrefs(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
            return prefs.getBoolean("notifications_enabled", true);
        } catch (Exception e) {
            Log.e(TAG, "Error reading notification preferences: " + e.getMessage());
            return true; // Default to enabled if there's an error
        }
    }

    // Method to check if system notifications are enabled
    private static boolean areSystemNotificationsEnabled(Context context) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return manager == null || manager.areNotificationsEnabled();
        } catch (Exception e) {
            Log.e(TAG, "Error checking system notification status: " + e.getMessage());
            return true; // Default to enabled if there's an error
        }
    }

    // Test method to verify notifications work
    public static void testNotification(Context context) {
        Log.d(TAG, "Testing notification system");
        showManualNotification(context, "This is a test notification to verify the system is working.", "Test Item");
    }
}
