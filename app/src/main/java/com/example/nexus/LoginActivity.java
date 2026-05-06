// Name: Harsh Patel (A20369913)

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

public class LoginActivity extends AppCompatActivity {

    TextInputEditText tempEmail, tempPassword;
    Button tempLoginBtn;
    TextView tempSignupSwitch;

    FirebaseAuth tempAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tempEmail =findViewById(R.id.email);
        tempPassword =findViewById(R.id.password);
        tempLoginBtn =findViewById(R.id.loginBtn);
        tempSignupSwitch =findViewById(R.id.signupSwitch);

        tempAuth = FirebaseAuth.getInstance();

        tempLoginBtn.setOnClickListener(v -> {

            String tempUserEmail = tempEmail.getText().toString().trim();
            String tempUserPassword = tempPassword.getText().toString().trim();

            // Email fill up check.
            if (TextUtils.isEmpty(tempUserEmail) || !Patterns.EMAIL_ADDRESS.matcher(tempUserEmail).matches()) {
                tempEmail.setError("Valid email is required");
                return;
            }

            // Password fill up check.
            if (TextUtils.isEmpty(tempUserPassword)) {
                tempPassword.setError("Password is required");
                return;
            }

            // Check for internet connection.
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            tempLoginBtn.setEnabled(false);

            // Sign in with email and password.
            tempAuth.signInWithEmailAndPassword(tempUserEmail,tempUserPassword)
                    .addOnCompleteListener(tempTask -> {
                        tempLoginBtn.setEnabled(true);
                        // if login is successful, the go to CustomizeFeedActivity.class
                        if(tempTask.isSuccessful())
                        {
                            Toast.makeText(this,"Login Successful",Toast.LENGTH_SHORT).show();

                            String tempUserId = tempAuth.getCurrentUser().getUid();
                            // get all the users' details.
                            FirebaseFirestore.getInstance().collection("users").document(tempUserId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        Intent intent = new Intent(LoginActivity.this, CustomizeFeedActivity.class);
                                        intent.putExtra("userName", documentSnapshot.getString("name"));
                                        intent.putExtra("userEmail", documentSnapshot.getString("email"));
                                        // start the CustomizeFeedActivity.class
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // start the CustomizeFeedActivity.class
                                        startActivity(new Intent(LoginActivity.this, CustomizeFeedActivity.class));
                                        finish();
                                    });
                        }
                        else
                        {
                            // if login is not successful, then show error message.
                            Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show();
                        }

                    });

        });

        // Switch to signup activity.
        tempSignupSwitch.setOnClickListener(v -> {
            // start the SignupActivity.class
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

    }

    // Check for internet connection.
    private boolean isNetworkAvailable() {
        ConnectivityManager tempConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (tempConnectivityManager != null) {
            // Check for active network.
            Network tempNetwork = tempConnectivityManager.getActiveNetwork();
            if (tempNetwork != null) {
                // Check for network capabilities.
                NetworkCapabilities tempNetworkCapabilities = tempConnectivityManager.getNetworkCapabilities(tempNetwork);
                // Check for network type.
                return tempNetworkCapabilities != null && (tempNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        tempNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        tempNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }
}