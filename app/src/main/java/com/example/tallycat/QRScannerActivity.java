package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
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
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Date;
import java.util.Calendar;

public class QRScannerActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentItemId; // Store the current item ID
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100; // Added permission request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner); // the simple blank layout you created

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentItemId = getIntent().getStringExtra("CURRENT_ITEM_ID");

        if (checkCameraPermission()) {
            proceedWithQRScanning();
        }
    }


    // Method to check and request camera permission
    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with QR scanning
                proceedWithQRScanning();
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // Method to handle QR scanning after permission is granted
    private void proceedWithQRScanning() {
        startQRScanner();  // always scan for this flow
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
        Log.d("QRScanner", "Scanned QR content: " + qrContent);
        if (qrContent.startsWith("TALLYCAT:")) {
            String itemId = qrContent.substring("TALLYCAT:".length());

            // Verify scanned QR matches the expected item (if any)
            if (currentItemId != null && !currentItemId.equals(itemId)) {
                Toast.makeText(this, "Scanned QR code doesn't match this item", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            processItemScan(itemId);
        } else {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processItemScan(String itemId) {
        db.collection("inventory").document(itemId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Item item = documentSnapshot.toObject(Item.class);
                        if (item != null) {
                            // Ensure itemId is set so it isn't null later
                            item.setItemId(itemId);

                            processItemUpdate(item); //Calls new method for all or nothing upload
                        } else {
                            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
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

    //New method. all or nothing update, sets transactionId in addition to previous data.
    private void processItemUpdate(Item item) {
        String userEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() : "Unknown User";

        // 1. Prepare the WriteBatch and all necessary references
        WriteBatch batch = db.batch();
        DocumentReference itemDocRef = db.collection("inventory").document(item.getItemId());

        // Generate a UNIQUE transaction ID on the client
        String newTransactionId = UUID.randomUUID().toString();
        DocumentReference transactionDocRef = db.collection("transactions").document(newTransactionId);

        // 2. Determine the transaction type and prepare data
        String transactionType;
        Map<String, Object> itemUpdates = new HashMap<>();

        if ("Available".equalsIgnoreCase(item.getStatus())) {
            transactionType = "checkout";
            // Also update holder and due date for a checkout
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 14); // 14-day due date
            itemUpdates.put("status", "Checked Out");
            itemUpdates.put("holder", userEmail);
            itemUpdates.put("dueDate", cal.getTime());
        } else {
            transactionType = "return";
            // Clear holder and due date for a return
            itemUpdates.put("status", "Available");
            itemUpdates.put("holder", null);
            itemUpdates.put("dueDate", null);
        }

        // 3. Stage the operations in the batch

        // Operation A: Update the inventory item
        batch.update(itemDocRef, itemUpdates);

        // Operation B: Create the new transaction object
        Transaction transaction = new Transaction();
        transaction.setTransactionId(newTransactionId); // Set the unique ID
        transaction.setItemId(item.getItemId());
        transaction.setName(item.getName());
        transaction.setEmail(userEmail);
        transaction.setTransactionType(transactionType);
        // The 'timestamp' field is left null; Firestore will populate it on the server.

        // Stage the creation of the new transaction document
        batch.set(transactionDocRef, transaction);

        // 4. Commit the atomic batch operation
        batch.commit().addOnSuccessListener(unused -> {
            Toast.makeText(this, "Item " + transactionType + " successful!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    /*private void toggleItemStatus(Item item) {
        String currentStatus = item.getStatus();
        String newStatus;
        String transactionType;

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

        // Update item status
        db.collection("inventory").document(item.getItemId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    // Record transaction
                    recordTransaction(item, transactionType);
                    Toast.makeText(this, "Item " + transactionType + " successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }*/

    /*private void recordTransaction(Item item, String transactionType) {
        String userEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() : "Unknown User";

        Transaction transaction = new Transaction();
        transaction.setItemId(item.getItemId());
        transaction.setName(item.getName());
        transaction.setEmail(userEmail);
        transaction.setTransactionType(transactionType);
        // timestamp will be automatically set by @ServerTimestamp

        db.collection("transactions").add(transaction);
    }*/
}