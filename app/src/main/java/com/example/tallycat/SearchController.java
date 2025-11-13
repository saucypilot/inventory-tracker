package com.example.tallycat;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * The controller responsible for handling search logic.
 * It communicates with the data source and sends results back to the view (SearchActivity).
 */
public class SearchController {

    private final InventoryDataSource dataSource;

    public SearchController() {
        this.dataSource = new InventoryDataSource();
    }

    /**
     * An interface to allow the Controller to send results back to the View (Activity).
     */
    public interface SearchResultListener {
        void onSearchSuccess(List<Item> results);
        void onSearchFailure(String errorMessage);
        void onNoResultsFound();
    }

    /**
     * Executes the search by invoking the data source and processing the result.
     */
    public void performSearch(String queryText, SearchResultListener listener) {
        dataSource.findItemById(queryText).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult().isEmpty()) {
                    // No documents were found matching the query
                    listener.onNoResultsFound();
                } else {
                    // Successfully found one or more documents
                    List<Item> searchResults = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Item item = document.toObject(Item.class);
                        searchResults.add(item);
                    }
                    listener.onSearchSuccess(searchResults);
                }
            } else {
                // The task failed
                listener.onSearchFailure("Error performing search. Check logs for details.");
            }
        });
    }
}