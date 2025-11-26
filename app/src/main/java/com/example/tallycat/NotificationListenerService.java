package com.example.tallycat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class NotificationListenerService extends Service {
    private static final String TAG = "NotificationListenerService";
    private ListenerRegistration notificationListener;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        startListeningForNotifications();
    }

    private void startListeningForNotifications() {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;

        if (currentUserEmail == null) return;

        notificationListener = db.collection("notifications")
                .whereEqualTo("userEmail", currentUserEmail)
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed:", error);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (var change : snapshots.getDocumentChanges()) {
                            if (change.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                String itemName = change.getDocument().getString("itemName");
                                String message = change.getDocument().getString("message");

                                // Show the notification
                                NotificationHelper.showManualNotification(
                                        NotificationListenerService.this,
                                        message,
                                        itemName
                                );

                                // Mark as read
                                change.getDocument().getReference().update("read", true);
                            }
                        }
                    }
                });
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
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}