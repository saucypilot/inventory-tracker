package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
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


    // ADDED: Method to check and request camera permission
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

    // ADDED: Handle permission request result
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

    // ADDED: Method to handle QR scanning after permission is granted
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
        if (qrContent.startsWith("TALLYCAT_ITEM:")) {
            String itemId = qrContent.substring("TALLYCAT_ITEM:".length());

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

                            toggleItemStatus(item);
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


    private void toggleItemStatus(Item item) {
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
    }

    private void recordTransaction(Item item, String transactionType) {
        String userEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() : "Unknown User";

        Transaction transaction = new Transaction();
        transaction.setItemId(item.getItemId());
        transaction.setName(item.getName());
        transaction.setEmail(userEmail);
        transaction.setTransactionType(transactionType);
        // timestamp will be automatically set by @ServerTimestamp

        db.collection("transactions").add(transaction);
    }
}