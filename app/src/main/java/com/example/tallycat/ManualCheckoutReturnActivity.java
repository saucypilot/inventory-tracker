package com.example.tallycat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
// Added these imports
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import java.util.UUID;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.text.ParseException;
import java.util.Date;


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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
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

        btnManualCheckout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm checkout")
                    .setMessage("Record a manual checkout for this item?")
                    .setPositiveButton("Yes", (dialog, which) -> performCheckout())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnManualReturn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm return")
                    .setMessage("Record a manual return for this item?")
                    .setPositiveButton("Yes", (dialog, which) -> performReturn())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

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
        // Keep all the validation logic
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
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        fmt.setLenient(false);
        try {
            Date parsedDate = fmt.parse(dueDate);
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0); todayCal.set(Calendar.MINUTE, 0); todayCal.set(Calendar.SECOND, 0); todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();
            if (parsedDate.before(today)) {
                Toast.makeText(this, "Due date cannot be in the past.", Toast.LENGTH_SHORT).show();
                return;
            }
            Calendar maxCal = Calendar.getInstance(); maxCal.setTime(today); maxCal.add(Calendar.DAY_OF_YEAR, 30);
            if (parsedDate.after(maxCal.getTime())) {
                Toast.makeText(this, "Due date cannot be more than 30 days from today.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Due date must be in yyyy-MM-dd format.", Toast.LENGTH_SHORT).show();
            return;
        }


// --- This is the new database writebatch logic ---
        WriteBatch batch = db.batch();
        DocumentReference itemDocRef = db.collection("inventory").document(item.getItemId());
        String newTransactionId = UUID.randomUUID().toString();
        DocumentReference transactionDocRef = db.collection("transactions").document(newTransactionId);

        Map<String, Object> itemUpdate = new HashMap<>();
        itemUpdate.put("status", "Checked-out");
        itemUpdate.put("holder", holderEmail);
        itemUpdate.put("dueDate", dueDate);
        itemUpdate.put("name", item.getName());
        itemUpdate.put("name_lowercase", item.getName_lowercase());
        batch.update(itemDocRef, itemUpdate);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(newTransactionId);
        transaction.setTransactionType("checkout");
        transaction.setItemId(item.getItemId());
        transaction.setName(item.getName());
        transaction.setEmail(holderEmail);
        batch.set(transactionDocRef, transaction);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    item.setStatus("Checked-out");
                    item.setHolder(holderEmail);
                    item.setDueDate(dueDate);
                    bindItemToUI();
                    Toast.makeText(this, "Checkout recorded successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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

// --- This is the new database logic ---
        WriteBatch batch = db.batch();
        DocumentReference itemDocRef = db.collection("inventory").document(item.getItemId());
        String newTransactionId = UUID.randomUUID().toString();
        DocumentReference transactionDocRef = db.collection("transactions").document(newTransactionId);

        Map<String, Object> itemUpdate = new HashMap<>();
        itemUpdate.put("status", "Available");
        itemUpdate.put("holder", null); // Use null to remove the field
        itemUpdate.put("dueDate", null); // Use null to remove the field
        itemUpdate.put("name", item.getName());
        itemUpdate.put("name_lowercase", item.getName_lowercase());
        batch.update(itemDocRef, itemUpdate);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(newTransactionId);
        transaction.setTransactionType("return");
        transaction.setItemId(item.getItemId());
        transaction.setName(item.getName());
        transaction.setEmail(auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Unknown Admin");
        batch.set(transactionDocRef, transaction);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    item.setStatus("Available");
                    item.setHolder("");
                    item.setDueDate("");
                    bindItemToUI();
                    Toast.makeText(this, "Return recorded successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Operation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // This will simulate the system's back button press
        return true;
    }
}
