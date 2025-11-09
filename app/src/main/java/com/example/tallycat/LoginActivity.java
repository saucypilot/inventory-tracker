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

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button btnLogin;
    TextView forgotPassword, createAccountLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

//    private void routeByRole(DocumentSnapshot doc) {
//        if (!doc.exists()) {
//            // Create a default user profile, then route
//            String uid = mAuth.getCurrentUser().getUid();
//            String email = mAuth.getCurrentUser().getEmail();
//
//            Map<String, Object> profile = new HashMap<>();
//            profile.put("email", email != null ? email : "");
//            profile.put("role", "user");
//            profile.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
//
//            db.collection("users").document(uid).set(profile)
//                    .addOnSuccessListener(unused -> goToRole("user"))
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(this, "Profile missing and couldn’t be created: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                        // Fallback: still let them in as user
//                        goToRole("user");
//                    });
//            return;
//        }
//
//        String role = doc.getString("role");
//        goToRole(role != null ? role : "user");
//    }

    private void routeByRole(DocumentSnapshot doc) {
        String role = (doc.exists() && doc.contains("role")) ? doc.getString("role") : null;
        Toast.makeText(this, "role=" + role, Toast.LENGTH_SHORT).show(); // DEBUG: see what you're routing to

        if (role == null || role.trim().isEmpty()) {
            role = "user";
        }
        goToRole(role);
    }


    private void goToRole(String role) {
        Intent i = "admin".equalsIgnoreCase(role)
                ? new Intent(this, AdminActivity.class)
                : new Intent(this, UserActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

}
