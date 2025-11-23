package com.example.tallycat;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ItemProfileActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvId;
    private TextView tvStatus;
    private TextView tvCategory;
    private TextView tvDescription;
    private TextView tvHolder;
    private TextView tvDueDate;

    private ImageButton btnBack;

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
        tvHolder = findViewById(R.id.tvProfileHolder);
        tvDueDate = findViewById(R.id.tvProfileDueDate);

        // Get the Item object from the intent that started this activity
        Item item = getIntent().getParcelableExtra("ITEM_EXTRA");

        if (item != null) {
            populateProfile(item);
        } else {
            Toast.makeText(this, "Error: Item data not found.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void populateProfile(Item item) {
        tvName.setText(item.getName());
        tvId.setText("itemId: " + item.getItemId());
        tvStatus.setText(item.getStatus());
        tvCategory.setText(item.getCategory());
        tvDescription.setText(item.getDescription());

        String holderText = item.getHolder();
        if (holderText == null || holderText.isEmpty()) {
            holderText = "\u2014";
        }
        tvHolder.setText(holderText);

        String dueText = item.getDueDate();
        if (dueText == null || dueText.isEmpty()) {
            dueText = "\u2014";
        }
        tvDueDate.setText(dueText);
    }
}
