package com.example.tallycat;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private TextView tvEmptyState;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private static final String TAG = "NotificationListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerNotifications);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            tvEmptyState.setText("Please log in to view notifications");
            tvEmptyState.setVisibility(TextView.VISIBLE);
            recyclerView.setVisibility(TextView.GONE);
            return;
        }

        String email = currentUser.getEmail();

        if (email == null || email.isEmpty()) {
            tvEmptyState.setText("User email not available");
            tvEmptyState.setVisibility(TextView.VISIBLE);
            recyclerView.setVisibility(TextView.GONE);
            return;
        }

        Log.d(TAG, "Loading notifications for: " + email);

        db.collection("notifications")
                .whereEqualTo("userEmail", email)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<NotificationItem> list = new ArrayList<>();

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                String message = doc.getString("message");
                                String itemName = doc.getString("itemName");
                                String type = doc.getString("type");

                                // Handle timestamp safely
                                String timestamp = "Unknown time";
                                if (doc.getTimestamp("timestamp") != null) {
                                    timestamp = doc.getTimestamp("timestamp").toDate().toString();
                                }

                                // Use default values if fields are null
                                if (message == null) message = "No message";
                                if (itemName == null) itemName = "Unknown item";

                                list.add(new NotificationItem(message, itemName, timestamp));

                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document: " + e.getMessage());
                            }
                        }
                    }

                    adapter.setNotifications(list);

                    // Show empty state if no notifications
                    if (list.isEmpty()) {
                        tvEmptyState.setText("No notifications yet\n\nYou'll see your daily reminders and manual notifications here.");
                        tvEmptyState.setVisibility(TextView.VISIBLE);
                        recyclerView.setVisibility(TextView.GONE);
                    } else {
                        tvEmptyState.setVisibility(TextView.GONE);
                        recyclerView.setVisibility(TextView.VISIBLE);
                        Log.d(TAG, "Loaded " + list.size() + " notifications");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications: " + e.getMessage());
                    tvEmptyState.setText("Error loading notifications: " + e.getMessage() + "\n\nPlease check your internet connection and try again.");
                    tvEmptyState.setVisibility(TextView.VISIBLE);
                    recyclerView.setVisibility(TextView.GONE);
                });
    }
}