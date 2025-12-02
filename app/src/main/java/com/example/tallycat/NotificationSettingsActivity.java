package com.example.tallycat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationSettingsActivity extends AppCompatActivity {

    private TimePicker timePickerReturnTime;
    private Button btnSaveSettings, btnSendManualNotification;
    private SharedPreferences sharedPreferences;

    // BACK BUTTON: Add this line for the back button
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        sharedPreferences = getSharedPreferences("NotificationPrefs", MODE_PRIVATE);

        timePickerReturnTime = findViewById(R.id.timePickerReturnTime);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnSendManualNotification = findViewById(R.id.btnSendManualNotification);

        // BACK BUTTON: Initialize the back button
        btnBack = findViewById(R.id.btnBack6);

        // Load current settings
        loadCurrentSettings();

        // BACK BUTTON: Set click listener to go back when pressed
        btnBack.setOnClickListener(v -> {
            // Close this activity and return to Admin screen
            finish();
        });

        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnSendManualNotification.setOnClickListener(v ->
                startActivity(new Intent(this, ManualNotificationActivity.class)));
    }

    private void loadCurrentSettings() {
        // Default to 4:00 PM (16:00)
        int defaultHour = sharedPreferences.getInt("return_time_hour", 16);
        int defaultMinute = sharedPreferences.getInt("return_time_minute", 0);

        timePickerReturnTime.setHour(defaultHour);
        timePickerReturnTime.setMinute(defaultMinute);
    }

    private void saveSettings() {
        int hour = timePickerReturnTime.getHour();
        int minute = timePickerReturnTime.getMinute();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("return_time_hour", hour);
        editor.putInt("return_time_minute", minute);
        editor.apply();

        DailyReminderScheduler.scheduleDailyReminder(this, hour, minute);

        Toast.makeText(this, "Return time updated", Toast.LENGTH_SHORT).show();
    }


    private String formatTime(int hour, int minute) {
        String period = "AM";
        int displayHour = hour;

        if (hour >= 12) {
            period = "PM";
            if (hour > 12) {
                displayHour = hour - 12;
            }
        }
        if (hour == 0) {
            displayHour = 12;
        }

        return String.format("%d:%02d %s", displayHour, minute, period);
    }
}