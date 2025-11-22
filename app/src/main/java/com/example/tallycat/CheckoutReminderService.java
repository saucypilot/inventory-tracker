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
import java.util.concurrent.TimeUnit;
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
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);
        createNotificationChannel();
        startListeningForCheckouts();
    }

    private void createNotificationChannel() {
        NotificationHelper.createNotificationChannel(this);
    }

    private void startListeningForCheckouts() {
        transactionListener = db.collection("transactions")
                .whereEqualTo("transactionType", "checkout")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed:", error);
                        return;
                    }

                    if (snapshots != null) {
                        for (var change : snapshots.getDocumentChanges()) {
                            if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                handleNewCheckout(change.getDocument().toObject(Transaction.class));
                            }
                        }
                    }
                });
    }

    private void handleNewCheckout(Transaction transaction) {
        if (transaction.getTimestamp() != null && transaction.getName() != null) {
            scheduleReturnReminder(transaction.getTimestamp(), transaction.getName());
        }
    }

    private void scheduleReturnReminder(Timestamp checkoutTime, String itemName) {
        // Get return time from settings (default to 4:00 PM)
        int returnHour = sharedPreferences.getInt("return_time_hour", 16);
        int returnMinute = sharedPreferences.getInt("return_time_minute", 0);

        Calendar returnCalendar = Calendar.getInstance();
        returnCalendar.setTime(checkoutTime.toDate());

        // Set to the return time on the same day as checkout
        returnCalendar.set(Calendar.HOUR_OF_DAY, returnHour);
        returnCalendar.set(Calendar.MINUTE, returnMinute);
        returnCalendar.set(Calendar.SECOND, 0);

        long returnTime = returnCalendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        // Format the return time for the notification - make these final
        final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        final String checkoutTimeString = timeFormat.format(checkoutTime.toDate());

        // If return time has already passed, schedule for next day
        if (returnTime <= currentTime) {
            returnCalendar.add(Calendar.DAY_OF_YEAR, 1);
            returnTime = returnCalendar.getTimeInMillis();
        }

        // Create final variables for the lambda
        final String finalReturnTimeString = timeFormat.format(returnCalendar.getTime());
        final String finalItemName = itemName;

        long delay = returnTime - currentTime;

        // Schedule notification for the return time
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            NotificationHelper.showReturnReminder(CheckoutReminderService.this, finalItemName, finalReturnTimeString, checkoutTimeString);
        }, delay);

        Log.d(TAG, "Scheduled return reminder for " + itemName + " at " + finalReturnTimeString + " (in " + delay + " ms)");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
        if (transactionListener != null) {
            transactionListener.remove();
        }
    }
}