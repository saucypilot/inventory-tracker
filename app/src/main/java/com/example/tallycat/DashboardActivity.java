package com.example.tallycat;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches transaction data from the "transactions" collection and displays it in a list.
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    private FirebaseFirestore db;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = FirebaseFirestore.getInstance();
        rvTransactions = findViewById(R.id.rvTransactions);
        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(transactionList);

        // BACK BUTTON: Initialize the back button
        ImageButton btnBack = findViewById(R.id.btnBack7);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        // BACK BUTTON: Set click listener to go back when pressed
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Fetch the data from Firestore when the activity is created
        fetchTransactions();
    }

    private void fetchTransactions() {
        db.collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest first
                .limit(50)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "No transactions found.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        transactionList.clear(); // Clear old data
                        // Loop through documents and convert them to Transaction objects
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            transactionList.add(transaction);
                        }
                        adapter.notifyDataSetChanged(); // Refresh the RecyclerView
                        Log.d(TAG, "Successfully loaded " + transactionList.size() + " transactions.");
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(this, "Failed to load transaction data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}