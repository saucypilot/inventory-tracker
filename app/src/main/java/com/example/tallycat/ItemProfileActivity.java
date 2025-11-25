package com.example.tallycat;

import android.os.Bundle;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

// IMPORTANT: I UPDATED THIS FILE BECAUSE IT HAD A DEPRECATED API, DO NOT TOUCH
public class ItemProfileActivity extends AppCompatActivity {

    private TextView tvName, tvId, tvStatus, tvCategory, tvDescription;
    private ImageButton btnBack;
    private Button btnManualCheckoutReturn;
    private Item item;   // <-- class field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_profile);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Find all the TextViews from the layout
        tvName = findViewById(R.id.tvProfileItemName);
        tvId = findViewById(R.id.tvProfileItemId);
        tvStatus = findViewById(R.id.tvProfileStatus);
        tvCategory = findViewById(R.id.tvProfileCategory);
        tvDescription = findViewById(R.id.tvProfileDescription);

        btnManualCheckoutReturn = findViewById(R.id.btnManualCheckoutReturn);

        // Get the Item object from the intent that started this activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item = getIntent().getParcelableExtra("ITEM_EXTRA", Item.class);
        } else {
            item = getIntent().getParcelableExtra("ITEM_EXTRA");
        }

        // Check if the item object is valid and populate the views
        if (item == null) {
            Toast.makeText(this, "Error: Item data not found.", Toast.LENGTH_LONG).show();
            if (btnManualCheckoutReturn != null) {
                btnManualCheckoutReturn.setEnabled(false);
            }
            finish();
            return;
        }

        // We have a valid item
        populateProfile(item);

        btnManualCheckoutReturn.setOnClickListener(v -> {
            Intent intent = new Intent(ItemProfileActivity.this, ManualCheckoutReturnActivity.class);
            intent.putExtra(ManualCheckoutReturnActivity.EXTRA_ITEM, item);
            startActivity(intent);
        });
    }

    private void populateProfile(Item item) {
        tvName.setText(item.getName());
        tvId.setText("itemId: " + item.getItemId());
        tvStatus.setText(item.getStatus());
        tvCategory.setText(item.getCategory());
        tvDescription.setText(item.getDescription());
    }
}
