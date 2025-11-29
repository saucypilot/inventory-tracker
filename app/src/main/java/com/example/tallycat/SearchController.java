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
        // Step 1: Try searching by exact Item ID first.
        dataSource.findItemById(queryText).addOnCompleteListener(idTask -> {
            if (idTask.isSuccessful() && idTask.getResult() != null && !idTask.getResult().isEmpty()) {
                // SUCCESS: Found an item by its ID. This is the most direct match.
                List<Item> searchResults = new ArrayList<>();
                // Even though we get a list, it will only contain one item.
                for (QueryDocumentSnapshot document : idTask.getResult()) {
                    Item item = document.toObject(Item.class);
                    item.setItemId(document.getId()); // Set the ID from the document
                    searchResults.add(item);
                }
                listener.onSearchSuccess(searchResults);
            } else {
                // Step 2: If searching by ID fails or returns no results, try searching by name.
                searchItemsByName(queryText, listener);
            }
        });
    }

    private void searchItemsByName(String queryText, SearchResultListener listener) {
        dataSource.findItemsByName(queryText).addOnCompleteListener(nameTask -> {
            if (nameTask.isSuccessful() && nameTask.getResult() != null) {
                if (nameTask.getResult().isEmpty()) {
                    // No documents found by ID or by name.
                    listener.onNoResultsFound();
                } else {
                    // SUCCESS: Found one or more items by name.
                    List<Item> searchResults = new ArrayList<>();
                    for (QueryDocumentSnapshot document : nameTask.getResult()) {
                        Item item = document.toObject(Item.class);
                        item.setItemId(document.getId()); // Set the ID from the document
                        searchResults.add(item);
                    }
                    listener.onSearchSuccess(searchResults);
                }
            } else {
                // The task itself failed (permissions, no network, etc.)
                String errorMessage = nameTask.getException() != null ?
                        nameTask.getException().getMessage() : "Search by name failed.";
                listener.onSearchFailure(errorMessage);
            }
        });
    }
}