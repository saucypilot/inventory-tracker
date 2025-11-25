package com.example.tallycat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
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

    private ItemAdapter itemAdapter;
    private SearchController searchController;

    private boolean manualMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 1. Initialize the Controller
        searchController = new SearchController();

        // Decide if we are in manual UC11 mode
        manualMode = getIntent().getBooleanExtra("manualMode", false);

        // 2. Initialize UI components
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnPerformSearch = findViewById(R.id.btnPerformSearch);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        // 3. Setup RecyclerView
        itemAdapter = new ItemAdapter(new ArrayList<>(), this, manualMode);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(itemAdapter);

        // 4. Set OnClickListener to trigger the search
        btnPerformSearch.setOnClickListener(v -> {
            String queryText = etSearchQuery.getText().toString().trim();
            if (!queryText.isEmpty()) {
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

    public static void launchManualSearch(Context context) {
        Intent i = new Intent(context, SearchActivity.class);
        i.putExtra("manualMode", true);
        context.startActivity(i);
    }
}