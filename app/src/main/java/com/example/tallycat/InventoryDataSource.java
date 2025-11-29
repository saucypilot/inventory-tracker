package com.example.tallycat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * This class is responsible for executing queries and returning the results.
 */
public class InventoryDataSource {

    private final FirebaseFirestore db;

    private static final String COLLECTION_NAME = "inventory";
    private static final String ID_FIELD = "itemId";
    private static final String NAME_FIELD = "name";
    private static final String NAME_LOWERCASE_FIELD = "name_lowercase";

    public InventoryDataSource() {
        // Get the instance of the Firestore database
        db = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> findItemById(String itemId) {
        // This is the only part of the app that knows how to build a Firestore query.
        Query query = db.collection(COLLECTION_NAME).whereEqualTo(ID_FIELD, itemId);
        return query.get();
    }

    public Task<QuerySnapshot> findItemsByName(String nameQuery) {
        // The "\uf8ff" character is a high-end Unicode character. By using it in a range query,
        // we effectively get all documents where the 'name' field starts with the nameQuery.
        String lowerCaseQuery = nameQuery.toLowerCase();
        String endText = nameQuery + "\uf8ff";

        Query query = db.collection(COLLECTION_NAME)
                .orderBy(NAME_LOWERCASE_FIELD) // This is required by Firestore for range queries.
                .whereGreaterThanOrEqualTo(NAME_LOWERCASE_FIELD, nameQuery)
                .whereLessThanOrEqualTo(NAME_LOWERCASE_FIELD, endText)
                .limit(25); // It's good practice to limit results.

        return query.get();
    }
}