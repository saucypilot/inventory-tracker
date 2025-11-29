// DeleteItemActivity.java
package com.example.tallycat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class DeleteInv extends AppCompatActivity {

    private EditText etItemId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        etItemId = findViewById(R.id.etItemId);
        Button btnDelete = findViewById(R.id.btnDelete);

        // BACK BUTTON: Add this line to initialize the back button
        ImageButton btnBack = findViewById(R.id.btnBack4);

        db = FirebaseFirestore.getInstance();

        // BACK BUTTON: Set click listener to go back when pressed
        btnBack.setOnClickListener(v -> {
            // Close this activity and return to Admin screen
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            String id = etItemId.getText().toString().trim();
            if (TextUtils.isEmpty(id)) {
                toast("ItemID is required");
                return;
            }

            db.collection("inventory").document(id).delete()
                    .addOnSuccessListener(unused -> {
                        toast("Deleted");
                        finish();
                    })
                    .addOnFailureListener(e -> toast("Delete failed: " + e.getMessage()));
        });
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}