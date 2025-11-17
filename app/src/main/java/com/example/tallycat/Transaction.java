package com.example.tallycat;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

/**
 * Represents a single document in the "transactions" collection.
 */
public class Transaction {
    private String transactionId;
    private String transactionType;
    private String itemId;
    private String name;
    private String email;

    @ServerTimestamp
    private Timestamp timestamp;

    // Public constructor
    public Transaction() {}

    // Getters for all fields. The adapter will use these to display the data.
    public String getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Timestamp getTimestamp() { return timestamp; }
}
