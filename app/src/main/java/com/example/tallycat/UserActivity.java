package com.example.tallycat;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
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

        // Start the reminder service
        startReminderService();

        // Initialize views
        switchNotifications = findViewById(R.id.switchNotifications);
        Button signOut = findViewById(R.id.btnSignOut);
        TextView tvLastCheckout = findViewById(R.id.tvLastCheckout);

        // Load notification preference
        loadNotificationPreference();

        // Set up notification toggle listener
        switchNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveNotificationPreference(isChecked);
            }
        });

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
    }

    private void loadNotificationPreference() {
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void saveNotificationPreference(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notifications_enabled", enabled);
        editor.apply();

        // Restart service when notification preference changes
        if (enabled) {
            startReminderService();
        }
    }

    private void startReminderService() {
        try {
            Intent serviceIntent = new Intent(this, CheckoutReminderService.class);

            // Stop any existing service first to ensure clean start
            stopService(serviceIntent);

            // Start the service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d("UserActivity", "Reminder service started successfully");
        } catch (Exception e) {
            Log.e("UserActivity", "Failed to start reminder service: " + e.getMessage());
        }
    }
}