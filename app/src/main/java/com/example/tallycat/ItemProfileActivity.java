package com.example.tallycat;

import android.os.Bundle;
import android.os.Build;
// Import the ImageButton class
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


// IMPORTANT: I UPDATED THIS FILE BECAUSE IT HAD A DEPRECATED API, DO NOT TOUCH
public class ItemProfileActivity extends AppCompatActivity {

    private TextView tvName, tvId, tvStatus, tvCategory, tvDescription;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_profile);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Find all the TextViews from the layout
        tvName = findViewById(R.id.tvProfileItemName);
        tvId = findViewById(R.id.tvProfileItemId);
        tvStatus = findViewById(R.id.tvProfileStatus);
        tvCategory = findViewById(R.id.tvProfileCategory);
        tvDescription = findViewById(R.id.tvProfileDescription);

        // Get the Item object from the intent that started this activity
        Item item;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use the new API for Android 13+
            item = getIntent().getParcelableExtra("ITEM_EXTRA", Item.class);
        } else {
            // Use the old API for older versions
            item = getIntent().getParcelableExtra("ITEM_EXTRA");
        }

        // Check if the item object is valid and populate the views
        if (item != null) {
            populateProfile(item);
        } else {
            // Handle the error case where the item is null
            Toast.makeText(this, "Error: Item data not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no data
        }
    }

    private void populateProfile(Item item) {
        tvName.setText(item.getName());
        tvId.setText("itemId: " + item.getItemId());
        tvStatus.setText(item.getStatus());
        tvCategory.setText(item.getCategory());
        tvDescription.setText(item.getDescription());
    }
}