package com.example.tallycat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private Button btnClearAll;
    private FirebaseFirestore db;
    private List<NotificationItemWithId> notificationList;
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
        btnClearAll = findViewById(R.id.btnClearAll);

        notificationList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(convertToNotificationItems(notificationList));
        recyclerView.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Clear all button
        btnClearAll.setOnClickListener(v -> showClearAllDialog());

        // Setup swipe to delete
        setupSwipeToDelete();

        loadNotifications();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                deleteNotification(position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }



    private void showClearAllDialog() {
        if (notificationList.isEmpty()) {
            Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to delete all notifications? This cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> clearAllNotifications())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNotification(int position) {
        if (position < 0 || position >= notificationList.size()) {
            return;
        }

        NotificationItemWithId item = notificationList.get(position);
        String docId = item.documentId;

        // Delete from Firestore
        db.collection("notifications")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification deleted successfully");
                    notificationList.remove(position);
                    adapter.setNotifications(convertToNotificationItems(notificationList));

                    if (notificationList.isEmpty()) {
                        showEmptyState();
                    }

                    Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting notification: " + e.getMessage());
                    Toast.makeText(this, "Failed to delete notification", Toast.LENGTH_SHORT).show();
                    // Reload to restore the item
                    loadNotifications();
                });
    }

    private void clearAllNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            return;
        }

        String email = currentUser.getEmail();

        // Get all notifications for this user
        db.collection("notifications")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No notifications to clear", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Delete all documents
                    int totalCount = queryDocumentSnapshots.size();
                    int[] deletedCount = {0};
                    int[] failedCount = {0};

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    deletedCount[0]++;
                                    checkClearAllComplete(deletedCount[0], failedCount[0], totalCount);
                                })
                                .addOnFailureListener(e -> {
                                    failedCount[0]++;
                                    checkClearAllComplete(deletedCount[0], failedCount[0], totalCount);
                                    Log.e(TAG, "Failed to delete notification: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching notifications: " + e.getMessage());
                    Toast.makeText(this, "Failed to clear notifications", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkClearAllComplete(int deletedCount, int failedCount, int totalCount) {
        if (deletedCount + failedCount == totalCount) {
            // All operations completed
            notificationList.clear();
            adapter.setNotifications(new ArrayList<>());
            showEmptyState();

            if (failedCount == 0) {
                Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, deletedCount + " cleared, " + failedCount + " failed",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            tvEmptyState.setText("Please log in to view notifications");
            showEmptyState();
            return;
        }

        String email = currentUser.getEmail();
        if (email == null || email.isEmpty()) {
            tvEmptyState.setText("User email not available");
            showEmptyState();
            return;
        }

        Log.d(TAG, "Loading notifications for: " + email);

        db.collection("notifications")
                .whereEqualTo("userEmail", email)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                String message = doc.getString("message");
                                String itemName = doc.getString("itemName");
                                String timestamp = "Unknown time";

                                if (doc.getTimestamp("timestamp") != null) {
                                    timestamp = doc.getTimestamp("timestamp").toDate().toString();
                                }

                                if (message == null) message = "No message";
                                if (itemName == null) itemName = "Unknown item";

                                notificationList.add(new NotificationItemWithId(
                                        doc.getId(), message, itemName, timestamp));
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document: " + e.getMessage());
                            }
                        }
                    }

                    adapter.setNotifications(convertToNotificationItems(notificationList));

                    if (notificationList.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        Log.d(TAG, "Loaded " + notificationList.size() + " notifications");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading notifications: " + e.getMessage());
                    tvEmptyState.setText("Error loading notifications: " + e.getMessage() +
                            "\n\nPlease check your internet connection and try again.");
                    showEmptyState();
                });
    }

    private List<NotificationItem> convertToNotificationItems(List<NotificationItemWithId> items) {
        List<NotificationItem> result = new ArrayList<>();
        for (NotificationItemWithId item : items) {
            result.add(new NotificationItem(item.message, item.itemName, item.timestamp));
        }
        return result;
    }

    private void showEmptyState() {
        tvEmptyState.setVisibility(TextView.VISIBLE);
        recyclerView.setVisibility(TextView.GONE);
    }

    private void hideEmptyState() {
        tvEmptyState.setVisibility(TextView.GONE);
        recyclerView.setVisibility(TextView.VISIBLE);
    }

    // Helper class to store notification with document ID
    private static class NotificationItemWithId {
        String documentId;
        String message;
        String itemName;
        String timestamp;

        NotificationItemWithId(String documentId, String message, String itemName, String timestamp) {
            this.documentId = documentId;
            this.message = message;
            this.itemName = itemName;
            this.timestamp = timestamp;
        }
    }
}