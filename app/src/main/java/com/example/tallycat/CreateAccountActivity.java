package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class CreateAccountActivity extends AppCompatActivity {

    private Button btnUserAccount;
    private Button btnAdminAccount;
    private Button btnCreateAccount;
    private EditText email, password, confirmPassword;
    private EditText adminCode;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isAdmin = false;

    private static final String ADMIN_SECRET = "12345";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        btnUserAccount = findViewById(R.id.btnUserAccount);
        btnAdminAccount = findViewById(R.id.btnAdminAccount);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        adminCode = findViewById(R.id.adminCode);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        TextView loginLink = findViewById(R.id.loginLink);

        // Set the initial state: User Account is selected by default
        selectUserAccount();

        btnUserAccount.setOnClickListener(v -> selectUserAccount());
        btnAdminAccount.setOnClickListener(v -> selectAdminAccount());
        btnCreateAccount.setOnClickListener(v -> createUser());

        // --- Logic to go back to Login Screen ---

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
            finish();
            });
        }

        private void createUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userConfirmPassword = confirmPassword.getText().toString().trim();
        String enteredAdminCode = adminCode.getText().toString().trim();

        // Validation
        if (userEmail.isEmpty()) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }
        if (userPassword.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }
        if (userPassword.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return;
        }
        if (!userPassword.equals(userConfirmPassword)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        // Admin check
        if (isAdmin) {
            if (!enteredAdminCode.equals(ADMIN_SECRET)) {
                adminCode.setError("Invalid admin code");
                adminCode.requestFocus();
                return;
            }
        }

        // Firebase signup
            mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser fUser = task.getResult() != null ? task.getResult().getUser() : null;
                            if (fUser == null) {
                                // Extremely rare, but guard anyway
                                Toast.makeText(this, "Account created, but user is null. Try signing in.", Toast.LENGTH_LONG).show();
                                goToLoginAndFinish();
                                return;
                            }

                            String role = isAdmin ? "admin" : "user";

                            // Write profile doc in Firestore
                            Map<String, Object> profile = new HashMap<>();
                            profile.put("email", userEmail);
                            profile.put("role", role);
                            profile.put("createdAt", FieldValue.serverTimestamp());

                            db.collection("users")
                                    .document(fUser.getUid())
                                    .set(profile)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this,
                                                (role.substring(0, 1).toUpperCase() + role.substring(1)) + " account created!",
                                                Toast.LENGTH_SHORT).show();
                                        // Optionally: fUser.sendEmailVerification();

                                        goToLoginAndFinish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // IMPORTANT: still navigate so it doesn’t feel like “nothing happened”
                                        Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        goToLoginAndFinish();
                                    });

                        } else {
                            // Auth failed
                            String msg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(this, "Account creation failed: " + msg, Toast.LENGTH_LONG).show();
                            btnCreateAccount.setEnabled(true); // re-enable so user can retry
                        }
                    });
            }

    private void goToLoginAndFinish() {
        startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
        finish();
    }

    private void selectUserAccount() {

        isAdmin = false;

        btnUserAccount.setSelected(true);
        btnAdminAccount.setSelected(false);

        // Update text colors
        btnUserAccount.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnAdminAccount.setTextColor(ContextCompat.getColor(this, R.color.text_light));

        // Hide admin code field
        adminCode.setVisibility(View.GONE);
    }

    private void selectAdminAccount() {

        isAdmin = true;

        btnUserAccount.setSelected(false);
        btnAdminAccount.setSelected(true);

        // Update text colors
        btnAdminAccount.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        btnUserAccount.setTextColor(ContextCompat.getColor(this, R.color.text_light));

        // Show admin code field
        adminCode.setVisibility(View.VISIBLE);
    }
}
