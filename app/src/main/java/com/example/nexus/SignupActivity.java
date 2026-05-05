package com.example.nexus;

import android.content.Intent;
import android.os.Bundle;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText tempFullName, tempEmail, tempPassword;
    Button tempJoinBtn;
    TextView tempLoginSwitch;

    FirebaseAuth tempAuth;
    FirebaseFirestore tempFirebaseDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Layout file: activity_signup.xml
        setContentView(R.layout.activity_signup);

        tempFullName =findViewById(R.id.fullName);
        tempEmail =findViewById(R.id.email);
        tempPassword =findViewById(R.id.password);
        tempJoinBtn =findViewById(R.id.joinBtn);
        tempLoginSwitch =findViewById(R.id.loginSwitch);

        // Firebase Auth check
        tempAuth = FirebaseAuth.getInstance();
        // Firebase Firestore check
        tempFirebaseDB = FirebaseFirestore.getInstance();

        // Signup button click listener.
        tempJoinBtn.setOnClickListener(v -> {
            // Get all the user details.
            String tempName= tempFullName.getText().toString().trim();
            // Get all the user details.
            String tempUserEmail= tempEmail.getText().toString().trim();
            // Get all the user details.
            String tempUserPassword= tempPassword.getText().toString().trim();

            if (TextUtils.isEmpty(tempName)) {
                // If name is empty, then show error message.
                tempFullName.setError("Name is required");
                return;
            }

            if (TextUtils.isEmpty(tempUserEmail) || !Patterns.EMAIL_ADDRESS.matcher(tempUserEmail).matches()) {
                // If email is empty or invalid, then show error message.
                tempEmail.setError("Valid email is required");
                return;
            }

            if (TextUtils.isEmpty(tempUserPassword) || tempUserPassword.length() < 6) {
                // If password is empty or less than 6 characters, then show error message.
                tempPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (!isNetworkAvailable()) {
                // If no internet connection, then show error message.
                Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
                return;
            }

            tempJoinBtn.setEnabled(false); // Prevent multiple clicks during network call

            tempAuth.createUserWithEmailAndPassword(tempUserEmail,tempUserPassword)
                    .addOnCompleteListener(task -> {
                        // If signup is successful, then go to CustomizeFeedActivity.class
                        tempJoinBtn.setEnabled(true);
                        // If signup is successful, then go to CustomizeFeedActivity.class
                        if(task.isSuccessful())
                        {
                            // Get the user id.
                            String uid= tempAuth.getCurrentUser().getUid();
                            // Create a hashmap.
                            HashMap<String,Object> tempUser=new HashMap<>();
                            // Put all the userName in the hashmap.
                            tempUser.put("name",tempName);
                            // Put all the user email in the hashmap.
                            tempUser.put("email",tempUserEmail);

                            // Put all the user id in the hashmap.
                            tempFirebaseDB.collection("users").document(uid).set(tempUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // If signup is successful, then go to CustomizeFeedActivity.class
                                        Toast.makeText(SignupActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                        // Get the user id.
                                        Intent intent = new Intent(SignupActivity.this, CustomizeFeedActivity.class);
                                        // Get the useName.
                                        intent.putExtra("userName", tempName);
                                        // Get the user email.
                                        intent.putExtra("userEmail", tempUserEmail);
                                        // start the CustomizeFeedActivity.class
                                        startActivity(intent);
                                        // finish the SignupActivity.class
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // If signup is not successful, then show error message.
                                        Toast.makeText(SignupActivity.this, "Failed to save user info", Toast.LENGTH_SHORT).show();
                                    });
                        }
                        else
                        {
                            // If signup is not successful, then show error message.
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            // Log the error
                            android.util.Log.e("NexusDebug", "Signup Error: " + error);
                            // Show the error message
                            Toast.makeText(this,"Signup Failed: " + error,Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tempLoginSwitch.setOnClickListener(v -> {
            // start the LoginActivity.class
            startActivity(new Intent(SignupActivity.this,LoginActivity.class));
        });

    }

    private boolean isNetworkAvailable() {
        // Check for internet connection.
        ConnectivityManager tempConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // If connectivity manager is not null, then check for network.
        if (tempConnectivityManager != null) {
            // Check for active network.
            Network tempNetwork = tempConnectivityManager.getActiveNetwork();
            // If network is not null, then check for network capabilities.
            if (tempNetwork != null) {
                // Check for network capabilities.
                NetworkCapabilities tempCapabilities = tempConnectivityManager.getNetworkCapabilities(tempNetwork);
                // Check for network type.
                return tempCapabilities != null && (tempCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        tempCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        tempCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        // If no network is available, then return false.
        return false;
    }
}