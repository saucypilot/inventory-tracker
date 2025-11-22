package com.example.tallycat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddInv extends AppCompatActivity {

    private EditText etItemId, etName, etDescription, etCategory, etStatus;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add); // your add screen layout

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

            Map<String, Object> doc = new HashMap<>();
            doc.put("itemId", id);
            doc.put("name", name);
            doc.put("description", desc);
            doc.put("category", cat);
            doc.put("status", status);
            doc.put("qrCode", ""); // empty for now

            db.collection("inventory").document(id).set(doc)
                    .addOnSuccessListener(unused -> {
                        toast("Added");
                        finish(); // back to Admin
                    })
                    .addOnFailureListener(e -> toast("Add failed: " + e.getMessage()));
        });
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}