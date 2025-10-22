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

// Unnecessary imports removed for clarity (Button, EditText) if not used here

public class MainActivity extends AppCompatActivity {

    // You can remove @SuppressLint("MissingInflatedId") if you add the ID in the XML.
    // However, it's harmless to leave it.
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // This part handles system bars padding, leave it as is.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Start of New Code to Add ---

        // 1. Find the TextView for creating an account by its ID.
        //    Make sure you have added android:id="@+id/createAccountLink" to the TextView in activity_main.xml
        TextView createAccountLink = findViewById(R.id.createAccountLink);

        // 2. Set an OnClickListener to listen for clicks.
        createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 3. Create an Intent to start the CreateAccountActivity.
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        // --- End of New Code to Add ---
    }
}
