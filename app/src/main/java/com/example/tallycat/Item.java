package com.example.tallycat;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Item implements Parcelable {
    private String name;
    private String description;
    private String category;
    private String status;
    private String itemId;
    private String qrCode; // ADDED: QR code field

    // constructor
    public Item() {}

    //Parcel implementation
    protected Item(Parcel in) {
        name = in.readString();
        description = in.readString();
        category = in.readString();
        status = in.readString();
        itemId = in.readString();
        qrCode = in.readString(); // ADDED: Read QR code from parcel
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
        dest.writeString(qrCode); // ADDED: Write QR code to parcel
    }

    //Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getQrCode() { return qrCode; } // ADDED: QR code getter
    public void setQrCode(String qrCode) { this.qrCode = qrCode; } // ADDED: QR code setter
}