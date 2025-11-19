package com.example.tallycat;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;

public class UserActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Start the reminder service for notification system
        startReminderService();

        Button signOut = findViewById(R.id.btnSignOut);
        signOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        //Search button
        Button userSearchButton = findViewById(R.id.btnUserSearch);
        userSearchButton.setOnClickListener(v -> {
            // Create an Intent to open SearchActivity when the button is clicked.
            Intent intent = new Intent(UserActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }
    private void startReminderService() {
        Intent serviceIntent = new Intent(this, ReminderService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
}