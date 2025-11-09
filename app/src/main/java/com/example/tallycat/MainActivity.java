package com.example.tallycat;

import android.annotation.SuppressLint;
import android.content.Intent; // Make sure this is imported
import android.os.Bundle;
import android.view.View;
import android.widget.TextView; // Make sure this is imported

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // You can remove @SuppressLint("MissingInflatedId") if you add the ID in the XML.
    // However, it's harmless to leave it.
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_screen);

        // This part handles system bars padding, leave it as is.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Start of New Code to Add ---

        // Find the TextView for creating an account by its ID.
        TextView createAccountLink = findViewById(R.id.createAccountLink);

        createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        // --- End of New Code to Add ---
    }
}
