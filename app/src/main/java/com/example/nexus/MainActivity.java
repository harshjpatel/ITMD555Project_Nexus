package com.example.nexus;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth tempUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Code for : Check for broken pipe errors and exit gracefully.
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable.getMessage() != null && throwable.getMessage().contains("EPIPE")) {
                android.util.Log.e("NexusDebug", "Caught Broken Pipe error, ignoring.");
            } else {
                android.util.Log.e("NexusDebug", "Uncaught Exception", throwable);
                System.exit(1);
            }
        });

        EdgeToEdge.enable(this);
        // Layout file: activity_main.xml
        setContentView(R.layout.activity_main);

        // Note: Edge-to-edge padding handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase Auth check
        tempUserAuth = FirebaseAuth.getInstance();
        // get all the users' details.
        FirebaseUser user = tempUserAuth.getCurrentUser();

        // if user is not null, then login activities.
        if (user != null) {
            // Go to LoginActivity.class
            android.util.Log.d("NexusDebug", "User found: " + user.getUid());
            // Force login every time as requested
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // start the LoginActivity.class
            startActivity(intent);
        } else {         // if user is null, then sign up activities.
            // Go to SignupActivity.class
            android.util.Log.d("NexusDebug", "No user found, redirecting to Signup");
            // start the SignupActivity.class
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        }

        // If then, close this activity.
        finish();
    }
}