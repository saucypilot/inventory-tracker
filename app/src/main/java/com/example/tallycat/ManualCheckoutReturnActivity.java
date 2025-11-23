package com.example.tallycat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manual fallback for Use Case 11:
 * - Shows the current item status
 * - Lets admin choose Checkout or Return
 * - Writes a transaction + updates the item document
 */
public class ManualCheckoutReturnActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "EXTRA_ITEM";

    private TextView tvName, tvItemId, tvStatus, tvHolder, tvDueDate;
    private EditText etHolderEmail, etDueDate;
    private Button btnManualCheckout, btnManualReturn;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Item item;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_transaction);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        tvName = findViewById(R.id.tvManualName);
        tvItemId = findViewById(R.id.tvManualItemId);
        tvStatus = findViewById(R.id.tvManualStatus);
        tvHolder = findViewById(R.id.tvManualHolder);
        tvDueDate = findViewById(R.id.tvManualDueDate);

        etHolderEmail = findViewById(R.id.etHolderEmail);
        etDueDate = findViewById(R.id.etDueDate);

        btnManualCheckout = findViewById(R.id.btnManualCheckout);
        btnManualReturn = findViewById(R.id.btnManualReturn);

        item = getIntent().getParcelableExtra(EXTRA_ITEM);
        if (item == null) {
            Toast.makeText(this, "No item passed to manual checkout/return.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindItemToUI();

        // Pre-fill holder from signed-in user if nothing is set
        if (auth.getCurrentUser() != null && TextUtils.isEmpty(item.getHolder())) {
            etHolderEmail.setText(auth.getCurrentUser().getEmail());
        } else {
            etHolderEmail.setText(item.getHolder());
        }

        // Default due date = 7 days from now if not set
        if (TextUtils.isEmpty(item.getDueDate())) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 7);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etDueDate.setText(fmt.format(cal.getTime()));
        } else {
            etDueDate.setText(item.getDueDate());
        }

        btnManualCheckout.setOnClickListener(v -> performCheckout());
        btnManualReturn.setOnClickListener(v -> performReturn());
    }

    private void bindItemToUI() {
        tvName.setText(item.getName());
        tvItemId.setText("ID: " + item.getItemId());
        tvStatus.setText("Status: " + item.getStatus());
        tvHolder.setText("Holder: " +
                (TextUtils.isEmpty(item.getHolder()) ? "-" : item.getHolder()));
        tvDueDate.setText("Due date: " +
                (TextUtils.isEmpty(item.getDueDate()) ? "-" : item.getDueDate()));
    }

    // --- Checkout flow (UC11 steps 5–8 for checkout) ---
    private void performCheckout() {
        if ("Checked-out".equalsIgnoreCase(item.getStatus())) {
            Toast.makeText(this, "Item is already checked out.", Toast.LENGTH_SHORT).show();
            return;
        }

        String holderEmail = etHolderEmail.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        if (TextUtils.isEmpty(holderEmail)) {
            Toast.makeText(this, "Holder email is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(dueDate)) {
            Toast.makeText(this, "Due date is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Write a checkout transaction
        Map<String, Object> tx = new HashMap<>();
        tx.put("transactionType", "checkout");
        tx.put("itemId", item.getItemId());
        tx.put("name", item.getName());   // item name
        tx.put("email", holderEmail);     // who checked it out

        db.collection("transactions")
                .add(tx)
                .addOnSuccessListener(docRef -> {
                    // 2) Update the inventory document
                    Map<String, Object> update = new HashMap<>();
                    update.put("status", "Checked-out");
                    update.put("holder", holderEmail);
                    update.put("dueDate", dueDate);

                    db.collection("inventory")
                            .document(item.getItemId())
                            .update(update)
                            .addOnSuccessListener(unused -> {
                                item.setStatus("Checked-out");
                                item.setHolder(holderEmail);
                                item.setDueDate(dueDate);
                                bindItemToUI();
                                Toast.makeText(this, "Checkout recorded.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update item: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to write transaction: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // --- Return flow (UC11 steps 5–8 for return) ---
    private void performReturn() {
        if (!"Checked-out".equalsIgnoreCase(item.getStatus())) {
            Toast.makeText(this, "Item is not currently checked out.", Toast.LENGTH_SHORT).show();
            return;
        }

        String holderEmail = etHolderEmail.getText().toString().trim();
        if (TextUtils.isEmpty(holderEmail) && auth.getCurrentUser() != null) {
            holderEmail = auth.getCurrentUser().getEmail();
        }

        Map<String, Object> tx = new HashMap<>();
        tx.put("transactionType", "return");
        tx.put("itemId", item.getItemId());
        tx.put("name", item.getName());
        tx.put("email", holderEmail);

        db.collection("transactions")
                .add(tx)
                .addOnSuccessListener(docRef -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("status", "Available");
                    update.put("holder", "");
                    update.put("dueDate", "");

                    db.collection("inventory")
                            .document(item.getItemId())
                            .update(update)
                            .addOnSuccessListener(unused -> {
                                item.setStatus("Available");
                                item.setHolder("");
                                item.setDueDate("");
                                bindItemToUI();
                                Toast.makeText(this, "Return recorded.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update item: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to write transaction: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
