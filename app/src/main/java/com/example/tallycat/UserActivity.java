package com.example.tallycat;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class UserActivity extends AppCompatActivity {

    private Switch switchNotifications;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        // MANUAL notifications listener (KEEP this)
        listenForManualNotifications();

        // Initialize views
        switchNotifications = findViewById(R.id.switchNotifications);
        Button signOut = findViewById(R.id.btnSignOut);
        TextView tvLastCheckout = findViewById(R.id.tvLastCheckout);

        // Load saved state for notification toggle
        loadNotificationPreference();

        // Toggle listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationPreference(isChecked);
        });

        // Sign out button
        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Search button
        Button userSearchButton = findViewById(R.id.btnUserSearch);
        userSearchButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        Button btnViewAll = findViewById(R.id.btnViewAllNotifications);
        btnViewAll.setOnClickListener(v -> {
            startActivity(new Intent(UserActivity.this, NotificationListActivity.class));
        });


    }

    private void loadNotificationPreference() {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void saveNotificationPreference(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", enabled);
        editor.apply();
        // No need to start any service here — the new system uses AlarmManager + Firestore listeners
    }

    // Manual notification listener (WORKS with Firestore manual notifications)
    private void listenForManualNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("notifications")
                .whereEqualTo("userEmail", email)
                .whereEqualTo("read", false)
                .addSnapshotListener((snap, err) -> {
                    if (snap == null || snap.isEmpty()) return;

                    snap.getDocumentChanges().forEach(change -> {
                        String message = change.getDocument().getString("message");
                        String item = change.getDocument().getString("itemName");

                        NotificationHelper.showManualNotification(
                                UserActivity.this,
                                message,
                                item
                        );

                        // Mark as read
                        change.getDocument().getReference().update("read", true);
                    });
                });
    }

}
