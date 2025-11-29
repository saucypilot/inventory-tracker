package com.example.tallycat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRDisplayActivity extends AppCompatActivity {

    private ImageView ivQRCode;
    private TextView tvItemName, tvItemId;
    private Button btnBack, btnDownloadQR;
    private FirebaseFirestore db;
    private static final String TAG = "QRDisplayActivity";
    private Bitmap currentQRBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        ivQRCode = findViewById(R.id.ivQRCode);
        tvItemName = findViewById(R.id.tvQRItemName);
        tvItemId = findViewById(R.id.tvQRItemId);
        btnBack = findViewById(R.id.btnBackQR);
        btnDownloadQR = findViewById(R.id.btnDownloadQR); // Initialize download button
        db = FirebaseFirestore.getInstance();

        // Get item data from intent
        Item item = getIntent().getParcelableExtra("ITEM_EXTRA");

        if (item != null) {
            displayItemInfo(item);

            if (item.getQrCode() != null && !item.getQrCode().isEmpty()) {
                // Check if it's a base64 image or just text
                if (isBase64Image(item.getQrCode())) {
                    displayQRCodeImage(item.getQrCode());
                } else {
                    // If it's just text, generate QR code on the fly
                    generateAndDisplayQRCode(item);
                }
            } else {
                Toast.makeText(this, "QR code not available for this item", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Item data not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        btnDownloadQR.setOnClickListener(v -> saveQRCode(item));
    }

    private void displayItemInfo(Item item) {
        tvItemName.setText(item.getName());
        tvItemId.setText("ID: " + item.getItemId());
    }

    private boolean isBase64Image(String data) {
        // Simple check - base64 images are usually longer and contain specific characters
        return data.length() > 100 && data.contains("/") && data.contains("+");
    }

    private void displayQRCodeImage(String base64QR) {
        try {
            byte[] decodedBytes = Base64.decode(base64QR, Base64.DEFAULT);
            currentQRBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (currentQRBitmap != null) {
                ivQRCode.setImageBitmap(currentQRBitmap);
                Toast.makeText(this, "QR Code loaded", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to decode QR code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying QR code: " + e.getMessage());
            Toast.makeText(this, "Error displaying QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateAndDisplayQRCode(Item item) {
        try {
            // Generate QR code from the text data
            String qrData = item.getQrCode(); // This should be "TALLYCAT:ITEM123"
            com.google.zxing.qrcode.QRCodeWriter writer = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            currentQRBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    currentQRBitmap.setPixel(x, y, bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }

            ivQRCode.setImageBitmap(currentQRBitmap);
            Toast.makeText(this, "QR Code generated", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code: " + e.getMessage());
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQRCode(Item item) {
        if (currentQRBitmap == null) {
            Toast.makeText(this, "QR code not available to save", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create a file name with timestamp
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "TallyCat_QR_" + item.getItemId() + "_" + timeStamp + ".png";

            // Save to external storage
            File storageDir = getExternalFilesDir(null);
            File qrFile = new File(storageDir, fileName);

            FileOutputStream out = new FileOutputStream(qrFile);
            currentQRBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // Show success message with file location
            String message = "QR code saved to:\n" + qrFile.getAbsolutePath() + "\n\nYou can now print or share this file.";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            // Optional: You can also share the file here
            // shareQRCode(qrFile, item);

        } catch (IOException e) {
            Log.e(TAG, "Error saving QR code: " + e.getMessage());
            Toast.makeText(this, "Error saving QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Optional: Method to share the QR code
    private void shareQRCode(File qrFile, Item item) {
        try {
            androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    qrFile
            );

            // You can implement sharing intent here if needed
            // Intent shareIntent = new Intent(Intent.ACTION_SEND);
            // shareIntent.setType("image/png");
            // shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            // startActivity(Intent.createChooser(shareIntent, "Share QR Code"));

        } catch (Exception e) {
            Log.e(TAG, "Error sharing QR code: " + e.getMessage());
        }
    }
}