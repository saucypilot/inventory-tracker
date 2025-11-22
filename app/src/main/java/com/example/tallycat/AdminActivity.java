package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button btnGoAdd = findViewById(R.id.btnAdd);
        Button btnGoEdit = findViewById(R.id.btnEdit);
        Button btnGoDelete = findViewById(R.id.btnDelete);
        Button btnSignOut = findViewById(R.id.btnSignOut);
        Button btnGoSearch = findViewById(R.id.btnSearch);
        Button viewDataButton = findViewById(R.id.btnViewData);

        // Add the notification settings button - make sure this ID exists in your XML
        Button btnNotificationSettings = findViewById(R.id.btnNotificationSettings);

        btnGoAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddInv.class)));

        btnGoEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditInv.class)));

        btnGoDelete.setOnClickListener(v ->
                startActivity(new Intent(this, DeleteInv.class)));

        btnGoSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        // Add click listener for notification settings
        if (btnNotificationSettings != null) {
            btnNotificationSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationSettingsActivity.class)));
        }

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        viewDataButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, DashboardActivity.class);
            startActivity(intent);
        });
    }
}