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

    public InventoryDataSource() {
        // Get the instance of the Firestore database
        db = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> findItemById(String itemId) {
        // This is the only part of the app that knows how to build a Firestore query.
        Query query = db.collection(COLLECTION_NAME).whereEqualTo(ID_FIELD, itemId);
        return query.get();
    }
}