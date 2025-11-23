package com.example.tallycat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Inventory item document from the "inventory" collection.
 * Now includes holder and dueDate to support manual checkout/return
 * and live inventory status.
 */
public class Item implements Parcelable {
    private String name;
    private String description;
    private String category;
    private String status;
    private String itemId;
    private String holder;
    private String dueDate;

    // Firestore needs a public no-arg constructor.
    public Item() { }

    // Parcelable constructor
    protected Item(Parcel in) {
        name = in.readString();
        description = in.readString();
        category = in.readString();
        status = in.readString();
        itemId = in.readString();
        holder = in.readString();
        dueDate = in.readString();
    }

    // This is the CREATOR used to deserialize the object.
    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(status);
        dest.writeString(itemId);
        dest.writeString(holder);
        dest.writeString(dueDate);
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
