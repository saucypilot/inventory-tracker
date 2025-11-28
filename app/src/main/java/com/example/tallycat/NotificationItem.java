package com.example.tallycat;

public class NotificationItem {
    public String message;
    public String itemName;
    public String timestamp;

    // Default constructor
    public NotificationItem() {
        this.message = "No message";
        this.itemName = "Unknown item";
        this.timestamp = "Unknown time";
    }

    public NotificationItem(String message, String itemName, String timestamp) {
        this.message = message != null ? message : "No message";
        this.itemName = itemName != null ? itemName : "Unknown item";
        this.timestamp = timestamp != null ? timestamp : "Unknown time";
    }
}