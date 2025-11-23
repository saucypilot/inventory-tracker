package com.example.tallycat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvInventory;
    private ItemAdapter itemAdapter;
    private final List<Item> allItems = new ArrayList<>();

    private EditText etInventorySearch;
    private Spinner spStatusFilter;
    private TextView tvInventoryMetrics;

    private ListenerRegistration inventoryListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        db = FirebaseFirestore.getInstance();

        rvInventory = findViewById(R.id.rvInventory);
        etInventorySearch = findViewById(R.id.etInventorySearch);
        spStatusFilter = findViewById(R.id.spStatusFilter);
        tvInventoryMetrics = findViewById(R.id.tvInventoryMetrics);

        // Default adapter behavior: tapping an item opens ItemProfileActivity
        itemAdapter = new ItemAdapter(new ArrayList<>(), this);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        rvInventory.setAdapter(itemAdapter);

        // Live subscription to inventory
        inventoryListener = db.collection("inventory")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load inventory: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot == null) return;

                    allItems.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Item item = doc.toObject(Item.class);
                        item.setItemId(doc.getId());
                        allItems.add(item);
                    }
                    applyFilters();
                });

        etInventorySearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        spStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        String query = etInventorySearch.getText().toString().trim().toLowerCase();
        String selectedStatus = spStatusFilter.getSelectedItem() != null
                ? spStatusFilter.getSelectedItem().toString()
                : "All";

        List<Item> filtered = new ArrayList<>();
        int available = 0;
        int checkedOut = 0;
        int overdue = 0;

        for (Item item : allItems) {
            String status = item.getStatus() != null ? item.getStatus() : "";
            boolean matchesStatus = "All".equalsIgnoreCase(selectedStatus)
                    || selectedStatus.equalsIgnoreCase(status);

            boolean matchesQuery = query.isEmpty()
                    || (item.getName() != null && item.getName().toLowerCase().contains(query))
                    || (item.getItemId() != null && item.getItemId().toLowerCase().contains(query));

            if (matchesStatus && matchesQuery) {
                filtered.add(item);
            }

            // Aggregate metrics across the full list (not just filtered)
            if ("Available".equalsIgnoreCase(status)) {
                available++;
            } else if ("Checked-out".equalsIgnoreCase(status) || "Checked out".equalsIgnoreCase(status)) {
                checkedOut++;
            } else if ("Overdue".equalsIgnoreCase(status)) {
                overdue++;
            }
        }

        itemAdapter.updateData(filtered);

        int total = allItems.size();
        String metrics = "Total: " + total
                + " \u2022 Available: " + available
                + " \u2022 Checked-out: " + checkedOut
                + " \u2022 Overdue: " + overdue;
        tvInventoryMetrics.setText(metrics);
    }

    @Override
    protected void onDestroy() {
        if (inventoryListener != null) {
            inventoryListener.remove();
        }
        super.onDestroy();
    }
}
