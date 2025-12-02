package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ManualNotificationActivity extends AppCompatActivity {

    private EditText etUserEmail, etItemName, etCustomMessage;
    private Button btnSendNotification;

    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_notification);

        etUserEmail = findViewById(R.id.etUserEmail);
        etItemName = findViewById(R.id.etItemName);
        etCustomMessage = findViewById(R.id.etCustomMessage);
        btnSendNotification = findViewById(R.id.btnSendNotification);

        btnBack = findViewById(R.id.btnBack5);

        btnBack.setOnClickListener(v -> {
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

        // Save notification to Firestore so it can be delivered to the specific user
        saveManualNotificationToFirestore(userEmail, itemName, customMessage);

        Toast.makeText(this, "Notification sent to " + userEmail, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveManualNotificationToFirestore(String userEmail, String itemName, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("userEmail", userEmail);
        notification.put("itemName", itemName);
        notification.put("message", message);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("type", "manual");
        notification.put("read", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ManualNotification", "Notification saved for user: " + userEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e("ManualNotification", "Error saving notification: " + e.getMessage());
                });
    }
}