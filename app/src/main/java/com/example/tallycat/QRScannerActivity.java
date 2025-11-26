package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import android.Manifest;
import android.content.pm.PackageManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class QRScannerActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "QRScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (checkCameraPermission()) {
            startQRScanner();
        }
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR Code to Check Out/Return Item");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivityPortrait.class);
        qrLauncher.launch(options);
    }

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    handleScannedQRCode(result.getContents());
                } else {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    private void handleScannedQRCode(String qrContent) {
        Log.d(TAG, "Scanned QR content: " + qrContent);

        if (qrContent.startsWith("TALLYCAT:")) {
            String itemId = qrContent.substring("TALLYCAT:".length());
            processItemScan(itemId);
        } else {
            Toast.makeText(this, "Invalid QR Code format", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processItemScan(String itemId) {
        db.collection("inventory").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        if (item != null) {
                            item.setItemId(itemId);
                            toggleItemStatus(item);
                        } else {
                            Toast.makeText(this, "Error: Could not read item data", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void toggleItemStatus(Item item) {
        String currentStatus = item.getStatus();
        String newStatus;
        String transactionType;

        String currentUserEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() : "Unknown User";

        if ("Available".equalsIgnoreCase(currentStatus)) {
            newStatus = "Checked Out";
            transactionType = "checkout";
        } else if ("Checked Out".equalsIgnoreCase(currentStatus)) {
            newStatus = "Available";
            transactionType = "return";
        } else {
            newStatus = "Available";
            transactionType = "return";
        }

        final String transactionId = UUID.randomUUID().toString();
        final String finalTransactionType = transactionType;
        final Item finalItem = item;

        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("status", newStatus);

        if ("checkout".equals(transactionType)) {
            updateData.put("holder", currentUserEmail);
            updateData.put("dueDate", calculateDueDate());
        } else {
            updateData.put("holder", null);
            updateData.put("dueDate", null);
        }

        db.collection("inventory").document(item.getItemId())
                .update(updateData)
                .addOnSuccessListener(unused -> {
                    recordTransaction(finalItem, finalTransactionType, transactionId);

                    String message = createSuccessMessage(finalItem, finalTransactionType, transactionId, currentUserEmail);
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("SCAN_RESULT", message);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private String calculateDueDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        long oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L;
        Date dueDate = new Date(System.currentTimeMillis() + oneWeekInMillis);
        return sdf.format(dueDate);
    }

    private String createSuccessMessage(Item item, String transactionType, String transactionId, String userEmail) {
        StringBuilder message = new StringBuilder();

        message.append("✓ ").append(transactionType.toUpperCase()).append(" SUCCESSFUL!\n");
        message.append("Item: ").append(item.getName()).append("\n");
        message.append("Transaction ID: ").append(transactionId.substring(0, 8)).append("...\n");

        if ("checkout".equals(transactionType)) {
            message.append("Holder: ").append(userEmail).append("\n");
            message.append("Due Date: ").append(calculateDueDate()).append("\n");
            message.append("Status: Checked Out");
        } else {
            message.append("Status: Available");
        }

        return message.toString();
    }

    private void recordTransaction(Item item, String transactionType, String transactionId) {
        String userEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() : "Unknown User";

        HashMap<String, Object> transaction = new HashMap<>();
        transaction.put("transactionId", transactionId);
        transaction.put("transactionType", transactionType);
        transaction.put("itemId", item.getItemId());
        transaction.put("name", item.getName());
        transaction.put("email", userEmail);

        Log.d(TAG, "Recording transaction with basic fields:");
        Log.d(TAG, "transactionId: " + transactionId);
        Log.d(TAG, "transactionType: " + transactionType);
        Log.d(TAG, "itemId: " + item.getItemId());
        Log.d(TAG, "name: " + item.getName());
        Log.d(TAG, "email: " + userEmail);

        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Transaction successfully recorded with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error recording transaction: " + e.getMessage());
                    recordTransactionAlternative(item, transactionType, transactionId, userEmail);
                });
    }

    // Alternative method using Transaction object if HashMap approach fails
    private void recordTransactionAlternative(Item item, String transactionType, String transactionId, String userEmail) {
        try {
            Transaction transaction = new Transaction();
            transaction.setTransactionId(transactionId);
            transaction.setTransactionType(transactionType);
            transaction.setItemId(item.getItemId());
            transaction.setName(item.getName());
            transaction.setEmail(userEmail);

            db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Transaction recorded successfully using Transaction object");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to record transaction with both methods: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error creating Transaction object: " + e.getMessage());
        }
    }
}