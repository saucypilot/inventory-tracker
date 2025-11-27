package com.example.tallycat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ReminderService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendDailyReturnReminders();
        return START_NOT_STICKY;
    }

    private void sendDailyReturnReminders() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("inventory")
                .whereEqualTo("status", "checked out")
                .get()
                .addOnSuccessListener(result -> {
                    for (QueryDocumentSnapshot doc : result) {
                        String itemName = doc.getString("name");
                        String holder = doc.getString("holder");

                        if (holder == null) continue;

                        // Send notification
                        NotificationHelper.showManualNotification(
                                this,
                                "Please return: " + itemName,
                                itemName
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("ReminderService", "Error: " + e.getMessage()));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
