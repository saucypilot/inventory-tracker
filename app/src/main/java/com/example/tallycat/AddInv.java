package com.example.tallycat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddInv extends AppCompatActivity {

    private EditText etItemId, etName, etDescription, etCategory, etStatus;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        etItemId = findViewById(R.id.ItemId);
        etName = findViewById(R.id.Name);
        etDescription = findViewById(R.id.Description);
        etCategory = findViewById(R.id.Category);
        etStatus = findViewById(R.id.Status);
        Button btnSave = findViewById(R.id.btnAdd);

        // BACK BUTTON: Add this line to initialize the back button
        ImageButton btnBack = findViewById(R.id.btnBack2);

        db = FirebaseFirestore.getInstance();

        // BACK BUTTON: Set click listener to go back when pressed
        btnBack.setOnClickListener(v -> {
            // Close this activity and return to Admin screen
            finish();
        });

        btnSave.setOnClickListener(v -> {
            String id = etItemId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();
            String cat = etCategory.getText().toString().trim();
            String status = etStatus.getText().toString().trim();

            if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name) ||
                    TextUtils.isEmpty(desc) || TextUtils.isEmpty(cat) || TextUtils.isEmpty(status)) {
                toast("Please fill ItemID, Name, Description, Category, Status");
                return;
            }

            // FIXED: Generate actual QR code image
            String qrData = "TALLYCAT:" + id;
            String qrCodeBase64 = generateQRCode(qrData);

            if (qrCodeBase64.isEmpty()) {
                toast("Failed to generate QR code");
                return;
            }

            Map<String, Object> doc = new HashMap<>();
            doc.put("itemId", id);
            doc.put("name", name);
            doc.put("description", desc);
            doc.put("category", cat);
            doc.put("status", status);
            doc.put("qrCode", qrCodeBase64); // Store actual QR code image as base64

            db.collection("inventory").document(id).set(doc)
                    .addOnSuccessListener(unused -> {
                        toast("Added with QR Code");
                        finish(); // back to Admin
                    })
                    .addOnFailureListener(e -> toast("Add failed: " + e.getMessage()));
        });
    }

    private String generateQRCode(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            // Generate QR code as bitmap
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 400, 400);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            // Convert BitMatrix to Bitmap
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Convert bitmap to base64 string for storage
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);

        } catch (WriterException e) {
            e.printStackTrace();
            toast("QR generation error: " + e.getMessage());
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            toast("Error: " + e.getMessage());
            return "";
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}