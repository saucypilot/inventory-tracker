package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button btnLogin;
    TextView forgotPassword, createAccountLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Add notification permission constants
    private static final int NOTIFICATION_PERMISSION_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        //firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        forgotPassword = findViewById(R.id.forgotPassword);
        createAccountLink = findViewById(R.id.createAccountLink);

        // Request notification permission
        requestNotificationPermission();

        //Handle login using firebase
        btnLogin.setOnClickListener(v -> login());

        //allow password reset
        forgotPassword.setOnClickListener(v -> {
            String email = username.getText().toString().trim();
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                username.setError("Enter a valid email to reset");
                username.requestFocus();
                return;
            }
            //calling password reset function to send email
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(LoginActivity.this, "Reset failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        createAccountLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class)));
    }

    // thod to request notification permission
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied - some features may not work", Toast.LENGTH_LONG).show();
            }
        }
    }

    //login function
    private void login() {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (user.isEmpty()) {
            username.setError("Email is required");
            username.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            username.setError("Enter a valid email");
            username.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(user, pass)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() == null) {
                            Toast.makeText(this, "Login error. Try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String uid = mAuth.getCurrentUser().getUid();
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(this::routeByRole)
                                .addOnFailureListener(e ->
                                        Toast.makeText(LoginActivity.this, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void routeByRole(DocumentSnapshot doc) {
        String role = (doc.exists() && doc.contains("role")) ? doc.getString("role") : null;
        Toast.makeText(this, "role=" + role, Toast.LENGTH_SHORT).show(); // DEBUG: see what you're routing to

        if (role == null || role.trim().isEmpty()) {
            role = "user";
        }
        goToRole(role);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If a user is already signed in, skip login screen
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(this::routeByRole)
                    .addOnFailureListener(e ->
                            Toast.makeText(LoginActivity.this,
                                    "Failed to load profile: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        }
    }
    private void goToRole(String role) {
        Intent i = "admin".equalsIgnoreCase(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, UserActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}