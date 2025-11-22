package com.example.tallycat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The View responsible for displaying the search UI and results.
 * It implements SearchResultListener to receive data from the SearchController.
 */
public class SearchActivity extends AppCompatActivity implements SearchController.SearchResultListener {

    private static final String TAG = "SearchActivity";

    private EditText etSearchQuery;
    private Button btnPerformSearch;
    private RecyclerView rvSearchResults;

    // BACK BUTTON: Add this line for the back button
    private ImageButton btnBack;

    private ItemAdapter itemAdapter;
    private SearchController searchController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Initialize the Controller
        searchController = new SearchController();

        // 2. Initialize UI components
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnPerformSearch = findViewById(R.id.btnPerformSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        // BACK BUTTON: Initialize the back button
        btnBack = findViewById(R.id.btnBack);

        // 3. Setup RecyclerView
        itemAdapter = new ItemAdapter(new ArrayList<>(), this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(itemAdapter);

        // BACK BUTTON: Set click listener to go back when pressed
        btnBack.setOnClickListener(v -> {
            // Close this activity and return to previous screen
            finish();
        });

        // 4. Set OnClickListener to trigger the search
        btnPerformSearch.setOnClickListener(v -> {
            String queryText = etSearchQuery.getText().toString().trim();
            if (!queryText.isEmpty()) {
                // The Activity tells the controller to do the work, passing itself as the listener
                searchController.performSearch(queryText, this);
            } else {
                Toast.makeText(this, "Please enter an item ID to search", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- These methods are called by the SearchController to update the UI ---

    @Override
    public void onSearchSuccess(List<Item> results) {
        Log.d(TAG, "Search successful, found " + results.size() + " items.");
        itemAdapter.updateData(results); // Update the adapter with the new list
    }

    @Override
    public void onSearchFailure(String errorMessage) {
        Log.e(TAG, "Search failed: " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        itemAdapter.clearData(); // Clear previous results on failure
    }

    @Override
    public void onNoResultsFound() {
        Log.d(TAG, "Search completed, but no items were found.");
        Toast.makeText(this, "No item found with that ID", Toast.LENGTH_SHORT).show();
        itemAdapter.clearData(); // Clear previous results
    }
}