package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ManualNotificationActivity extends AppCompatActivity {

    private EditText etUserEmail, etItemName, etCustomMessage;
    private Button btnSendNotification;

    // BACK BUTTON: Add this line for the back button
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_notification);

        etUserEmail = findViewById(R.id.etUserEmail);
        etItemName = findViewById(R.id.etItemName);
        etCustomMessage = findViewById(R.id.etCustomMessage);
        btnSendNotification = findViewById(R.id.btnSendNotification);

        // BACK BUTTON: Initialize the back button
        btnBack = findViewById(R.id.btnBack5);

        // BACK BUTTON: Set click listener to go back when pressed
        btnBack.setOnClickListener(v -> {
            // Close this activity and return to Notification Settings
            finish();
        });

        btnSendNotification.setOnClickListener(v -> sendManualNotification());
    }

    private void sendManualNotification() {
        String userEmail = etUserEmail.getText().toString().trim();
        String itemName = etItemName.getText().toString().trim();
        String customMessage = etCustomMessage.getText().toString().trim();

        if (userEmail.isEmpty()) {
            etUserEmail.setError("Please enter user email");
            etUserEmail.requestFocus();
            return;
        }

        if (itemName.isEmpty()) {
            etItemName.setError("Please enter item name");
            etItemName.requestFocus();
            return;
        }

        if (customMessage.isEmpty()) {
            customMessage = "Please return the checked out item: " + itemName;
        }

        // Show the notification immediately (no Firestore needed)
        NotificationHelper.showManualNotification(this, customMessage, itemName);

        Toast.makeText(this, "Notification sent to " + userEmail, Toast.LENGTH_SHORT).show();
        finish();
    }
}