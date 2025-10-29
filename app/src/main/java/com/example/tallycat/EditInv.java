// EditItemActivity.java
package com.example.tallycat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class EditInv extends AppCompatActivity {

    private EditText etItemId, etName, etDescription, etCategory, etStatus;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit); // your edit screen layout

        etItemId = findViewById(R.id.etItemId);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etCategory = findViewById(R.id.etCategory);
        etStatus = findViewById(R.id.etStatus);
        Button btnUpdate = findViewById(R.id.btnUpdate);

        db = FirebaseFirestore.getInstance();

        btnUpdate.setOnClickListener(v -> {
            String id = etItemId.getText().toString().trim();
            if (TextUtils.isEmpty(id)) {
                toast("ItemID is required");
                return;
            }

            Map<String, Object> update = new HashMap<>();
            putIfNotEmpty(update, "name", etName.getText().toString().trim());
            putIfNotEmpty(update, "description", etDescription.getText().toString().trim());
            putIfNotEmpty(update, "category", etCategory.getText().toString().trim());
            putIfNotEmpty(update, "status", etStatus.getText().toString().trim());
            // qrCode left alone for now

            if (update.isEmpty()) {
                toast("Nothing to update");
                return;
            }

            // merge = only updates provided fields
            db.collection("inventory").document(id).set(update, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        toast("Updated");
                        finish();
                    })
                    .addOnFailureListener(e -> toast("Update failed: " + e.getMessage()));
        });
    }

    private void putIfNotEmpty(Map<String, Object> map, String key, String value) {
        if (!TextUtils.isEmpty(value)) map.put(key, value);
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
