package com.example.tallycat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CheckoutReminderService extends Service {
    private static final String TAG = "CheckoutReminderService";
    private ListenerRegistration transactionListener;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
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
            scheduleReminder(transaction.getTimestamp(), transaction.getName());
        }
    }

    private void scheduleReminder(Timestamp checkoutTime, String itemName) {
        long checkoutMillis = checkoutTime.toDate().getTime();
        long reminderTime = checkoutMillis + TimeUnit.HOURS.toMillis(8);
        long currentTime = System.currentTimeMillis();

        // Format the checkout time for the notification
        SimpleDateFormat timeFormat = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        String formattedTime = timeFormat.format(checkoutTime.toDate());

        // If 8 hours have already passed, show notification immediately
        if (reminderTime <= currentTime) {
            // Pass formattedTime to the notification
            NotificationHelper.showCheckoutReminder(this, itemName, formattedTime);
        } else {
            // Schedule notification for the future
            long delay = reminderTime - currentTime;

            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                // Pass formattedTime to the notification
                NotificationHelper.showCheckoutReminder(this, itemName, formattedTime);
            }, delay);

            Log.d(TAG, "Scheduled reminder for " + itemName + " in " + delay + " ms");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Service will be restarted if killed
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