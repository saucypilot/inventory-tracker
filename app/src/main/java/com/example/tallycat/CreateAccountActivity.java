package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CreateAccountActivity extends AppCompatActivity {

    private Button btnUserAccount;
    private Button btnAdminAccount;
    private EditText adminCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Views
        btnUserAccount = findViewById(R.id.btnUserAccount);
        btnAdminAccount = findViewById(R.id.btnAdminAccount);
        adminCode = findViewById(R.id.adminCode);
        TextView loginLink = findViewById(R.id.loginLink);

        // --- Logic for Account Type Selection ---

        // Set the initial state: User Account is selected by default
        selectUserAccount();

        btnUserAccount.setOnClickListener(v -> selectUserAccount());
        btnAdminAccount.setOnClickListener(v -> selectAdminAccount());


        // --- Logic to go back to Login Screen ---

        loginLink.setOnClickListener(v -> {
            // Create an Intent to open MainActivity
            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
            // Clear the activity stack for a cleaner navigation flow
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void selectUserAccount() {
        btnUserAccount.setSelected(true);
        btnAdminAccount.setSelected(false);

        // Update text colors
        btnUserAccount.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnAdminAccount.setTextColor(ContextCompat.getColor(this, R.color.text_light));

        // Hide admin code field
        adminCode.setVisibility(View.GONE);
    }

    private void selectAdminAccount() {
        btnUserAccount.setSelected(false);
        btnAdminAccount.setSelected(true);

        // Update text colors
        btnAdminAccount.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnUserAccount.setTextColor(ContextCompat.getColor(this, R.color.text_light));

        // Show admin code field
        adminCode.setVisibility(View.VISIBLE);
    }
}
