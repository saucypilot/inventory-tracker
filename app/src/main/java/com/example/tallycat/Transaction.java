package com.example.tallycat;

import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.Timestamp;

public class Transaction {
    private String transactionId;
    private String transactionType;
    private String itemId;
    private String name;
    private String email;
    private String holder;
    private String dueDate;

    @ServerTimestamp
    private Timestamp timestamp;

    public Transaction() {}

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getHolder() { return holder; }
    public String getDueDate() { return dueDate; }
    public Timestamp getTimestamp() { return timestamp; }

    // Setters
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setHolder(String holder) { this.holder = holder; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}