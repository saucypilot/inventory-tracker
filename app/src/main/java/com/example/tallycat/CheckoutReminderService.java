package com.example.tallycat;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CheckoutReminderService extends Service {
    private static final String TAG = "CheckoutReminderService";
    private ListenerRegistration transactionListener;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate - Starting checkout reminder service");
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        createNotificationChannel();
        startListeningForCheckouts();
    }

    private void createNotificationChannel() {
        Log.d(TAG, "Creating notification channel");
        NotificationHelper.createNotificationChannel(this);
    }

    private void startListeningForCheckouts() {
        Log.d(TAG, "Starting to listen for checkout transactions");
        try {
            transactionListener = db.collection("transactions")
                    .whereEqualTo("transactionType", "checkout")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Listen failed:", error);
                            return;
                        }

                        if (snapshots != null && !snapshots.isEmpty()) {
                            Log.d(TAG, "Found " + snapshots.getDocumentChanges().size() + " checkout transactions");
                            for (var change : snapshots.getDocumentChanges()) {
                                if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                    Transaction transaction = change.getDocument().toObject(Transaction.class);
                                    Log.d(TAG, "New checkout detected for item: " + transaction.getName());
                                    handleNewCheckout(transaction);
                                }
                            }
                        } else {
                            Log.d(TAG, "No checkout transactions found");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error starting transaction listener: " + e.getMessage());
        }
    }

    private void handleNewCheckout(Transaction transaction) {
        if (transaction != null && transaction.getTimestamp() != null && transaction.getName() != null) {
            Log.d(TAG, "Processing checkout for: " + transaction.getName() + " at " + transaction.getTimestamp());
            scheduleReturnReminder(transaction.getTimestamp(), transaction.getName());
        } else {
            Log.w(TAG, "Invalid transaction data - missing timestamp or name");
        }
    }

    private void scheduleReturnReminder(Timestamp checkoutTime, String itemName) {
        try {
            // Get return time from settings (default to 4:00 PM)
            int returnHour = sharedPreferences.getInt("return_time_hour", 16);
            int returnMinute = sharedPreferences.getInt("return_time_minute", 0);

            Calendar returnCalendar = Calendar.getInstance();
            returnCalendar.setTime(checkoutTime.toDate());

            // Set to the return time on the same day as checkout
            returnCalendar.set(Calendar.HOUR_OF_DAY, returnHour);
            returnCalendar.set(Calendar.MINUTE, returnMinute);
            returnCalendar.set(Calendar.SECOND, 0);
            returnCalendar.set(Calendar.MILLISECOND, 0);

            long returnTime = returnCalendar.getTimeInMillis();
            long currentTime = System.currentTimeMillis();

            // Format the return time for the notification - make these final
            final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            final String checkoutTimeString = timeFormat.format(checkoutTime.toDate());

            // If return time has already passed, schedule for next day
            if (returnTime <= currentTime) {
                Log.d(TAG, "Return time has passed, scheduling for next day");
                returnCalendar.add(Calendar.DAY_OF_YEAR, 1);
                returnTime = returnCalendar.getTimeInMillis();
            }

            // Create final variables for the lambda
            final String finalReturnTimeString = timeFormat.format(returnCalendar.getTime());
            final String finalItemName = itemName;

            long delay = returnTime - currentTime;

            Log.d(TAG, "Scheduling return reminder for " + itemName +
                    " at " + finalReturnTimeString + " (in " + delay + " ms)");

            // Schedule notification for the return time
            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "Executing scheduled reminder for: " + finalItemName);
                try {
                    NotificationHelper.showReturnReminder(CheckoutReminderService.this,
                            finalItemName, finalReturnTimeString, checkoutTimeString);
                    Log.d(TAG, "Notification sent successfully for: " + finalItemName);
                } catch (Exception e) {
                    Log.e(TAG, "Error showing notification: " + e.getMessage());
                }
            }, delay);

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling return reminder: " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand - Service is running");
        // Ensure service stays running even if app is killed
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy - Cleaning up resources");
        if (transactionListener != null) {
            transactionListener.remove();
            Log.d(TAG, "Transaction listener removed");
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "Service onTaskRemoved - App was removed from recent tasks");
        // Restart service if needed
        Intent restartService = new Intent(getApplicationContext(), CheckoutReminderService.class);
        startService(restartService);
    }
}