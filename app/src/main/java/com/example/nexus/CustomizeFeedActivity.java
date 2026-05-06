// Name: Harsh Patel (A20369913)

package com.example.nexus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class CustomizeFeedActivity extends AppCompatActivity {

    RecyclerView tempRecyclerView;
    List<Community> tempCommunityList;
    CommunityAdapter tempAdapter;

    FirebaseFirestore firebaseDb;
    Button tempContinueBtn;

    TextView tempSelectedCountText;

    private String tempCurrentUserName;
    private String tempCurrentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Layout file: activity_customize_feed.xml
        super.onCreate(savedInstanceState);
        // Layout file: activity_customize_feed.xml
        setContentView(R.layout.activity_customize_feed);

        tempRecyclerView = findViewById(R.id.communityRecycler);
        tempContinueBtn = findViewById(R.id.startFeedBtn);
        tempSelectedCountText = findViewById(R.id.selectedCountText);

        tempRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // User profile
        findViewById(R.id.btnUserProfile).setOnClickListener(v -> showUserProfileDialog());

        // Initialize adapter
        tempCommunityList = new ArrayList<>();
        // Set adapter
        tempAdapter = new CommunityAdapter(this, tempCommunityList, count -> {
            // Update selected count text
            Log.d("NexusDebug", "Selection count changed to: " + count);
            // Update selected count text
            tempSelectedCountText.setText(getString(R.string.selected_count, count));
        });
        // Set initial count display
        tempSelectedCountText.setText(getString(R.string.selected_count, 0));
        // Set adapter
        tempRecyclerView.setAdapter(tempAdapter);
        // Search
        android.widget.EditText searchEdit = findViewById(R.id.searchCommunities);
        // Search
        searchEdit.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            // Search
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter
                tempAdapter.filter(s.toString());
            }
            // Search
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Firebase Firestore check
        firebaseDb = FirebaseFirestore.getInstance();
        // Get userName and email from intent
        tempCurrentUserName = getIntent().getStringExtra("userName");
        tempCurrentUserEmail = getIntent().getStringExtra("userEmail");
        // Load user preferences and communities
        loadUserPreferencesAndCommunities();
        // Continue button
        tempContinueBtn.setOnClickListener(v -> saveSelection());
    }

    private void loadUserPreferencesAndCommunities() {
        // Check for user
        FirebaseUser tempCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        // If no user, load all communities
        if (tempCurrentUser == null) {
            // Load all communities
            loadCommunities(null);
            // Return
            return;
        }

        firebaseDb.collection("users").document(tempCurrentUser.getUid()).get()
                // If user exists, load user preferences and communities
                .addOnSuccessListener(documentSnapshot -> {
                    // If no preferences exist, load all communities
                    tempCurrentUserName = documentSnapshot.getString("name");
                    // If no preferences exist, load all communities
                    tempCurrentUserEmail = documentSnapshot.getString("email");

                    // If no preferences exist, load all communities
                    @SuppressWarnings("unchecked")
                    List<String> selected = (List<String>) documentSnapshot.get("selectedCommunities");
                    // Load communities
                    loadCommunities(selected);
                })
                // If user does not exist, load all communities
                .addOnFailureListener(e -> {
                    // If user does not exist, load all communities
                    loadCommunities(null);
                });
    }

    // Load communities
    private void loadCommunities(List<String> userSelectedIds) {
        Log.d("NexusDebug", "Fetching communities...");
        // Fetch communities
        firebaseDb.collection("communities")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // If no communities exist, show toast
                    List<Community> fetchedCommunities = new ArrayList<>();
                    // Log number of communities found
                    Log.d("NexusDebug", "Documents found: " + queryDocumentSnapshots.size());
                    // If no communities exist, show toast
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Show toast
                        Toast.makeText(this, "No communities found", Toast.LENGTH_SHORT).show();
                        // Return
                        return;
                    }

                    // Loop through communities
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert document to community
                        Community community = doc.toObject(Community.class);
                        // If community exists, add to list
                        if (community != null && community.getName() != null) {
                            // Set community id
                            community.setCommunityId(doc.getId());
                            // Add community to list
                            fetchedCommunities.add(community);
                        }
                    }

                    // Update adapter
                    tempAdapter.updateList(fetchedCommunities);
                    // Set selected communities
                    if (userSelectedIds != null) {
                        // Set selected communities
                        tempAdapter.setTempSelectedCommunities(userSelectedIds);
                        // Update selected count text
                    } else {
                        // Set selected communities
                        tempAdapter.setTempSelectedCommunities(new ArrayList<>());
                    }
                })
                // If error, show toast
                .addOnFailureListener(e -> {
                    // Show toast
                    Log.e("NexusDebug", "Load failed", e);
                    // Show toast
                    Toast.makeText(this,
                            "Load failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void saveSelection() {
        // Log selection
        Log.d("NexusDebug", "Attempting to save selection...");
        // Get selected communities
        Set<String> selected = tempAdapter.getTempSelectedCommunities();
        // If no communities selected, show toast
        if (selected == null || selected.isEmpty()) {
            // Show toast
            Toast.makeText(this, "Select at least 1 community", Toast.LENGTH_SHORT).show();
            // Return
            return;
        }
        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // If no user, show toast
        if (currentUser == null) {
            // Show toast
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            // Return
            return;
        }

        // Get user id
        String uid = currentUser.getUid();
        // Disable button
        tempContinueBtn.setEnabled(false);
        // Create hashmap
        HashMap<String, Object> data = new HashMap<>();
        // Put selected communities in hashmap
        data.put("selectedCommunities", new ArrayList<>(selected));

        // Save to Firebase
        firebaseDb.collection("users")
                // Update user
                .document(uid)
                // Merge with existing data
                .set(data, SetOptions.merge())
                // If success, show toast
                .addOnSuccessListener(aVoid -> {
                    // Log success
                    Log.d("NexusDebug", "Preferences saved successfully!");
                    // Show toast
                    Toast.makeText(this,
                            // USE STRING RESOURCES
                            "Preferences saved",
                            // USE STRING RESOURCES
                            Toast.LENGTH_SHORT).show();

                    // NAVIGATE ONLY ON SUCCESS
                    Intent intent = new Intent(
                            // USE STRING RESOURCES
                            CustomizeFeedActivity.this,
                            // USE STRING RESOURCES
                            DashboardActivity.class
                    );
                    // Pass userName and email
                    intent.putExtra("userName", tempCurrentUserName);
                    intent.putExtra("userEmail", tempCurrentUserEmail);
                    // Start activity
                    startActivity(intent);
                    // Finish activity
                    finish();
                })
                // If error, show toast
                .addOnFailureListener(e -> {
                    // Log error
                    Log.e("NexusDebug", "Error writing to Firebase", e);
                    // Show toast
                    Toast.makeText(this,
                            // USE STRING RESOURCES
                            "Error: " + e.getMessage(),
                            // USE STRING RESOURCES
                            Toast.LENGTH_SHORT).show();
                    // Enable button
                    tempContinueBtn.setEnabled(true);
                });
    }

    // User profile
    private void showUserProfileDialog() {
        // Get current user
        FirebaseUser tempUser = FirebaseAuth.getInstance().getCurrentUser();
        // If no user, return
        if (tempUser == null) return;

        // Create dialog
        View tempDialogView = getLayoutInflater().inflate(R.layout.dialog_user_profile, null);
        // Create dialog
        AlertDialog tempDialog = new MaterialAlertDialogBuilder(this)
                // Set dialog view
                .setView(tempDialogView)
                // Create dialog
                .create();
        // Set background
        if (tempDialog.getWindow() != null) {
            // Set background
            tempDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get views
        TextView txtName = tempDialogView.findViewById(R.id.profileName);
        // Get views
        TextView txtEmail = tempDialogView.findViewById(R.id.profileEmail);
        // Get views
        Button btnLogout = tempDialogView.findViewById(R.id.btnLogout);

        // Get user info
        FirebaseFirestore.getInstance().collection("users").document(tempUser.getUid()).get()
                // If success, set user info
                .addOnSuccessListener(doc -> {
                    // If user exists, set user info
                    if (doc.exists()) {
                        // Get user info
                        String tempName = doc.getString("name");
                        // Get user info
                        String tempEmail = doc.getString("email");
                        // If userName exists, set userName
                        if (tempName != null) {
                            // Set userName
                            tempCurrentUserName = tempName;
                            // Set userName
                            txtName.setText(tempName);
                        }
                        if (tempEmail != null) {
                            // Set userEmail
                            tempCurrentUserEmail = tempEmail;
                            // Set userEmail
                            txtEmail.setText(tempEmail);
                        }
                    }
                });

        // Set user info
        txtName.setText(tempCurrentUserName != null ? tempCurrentUserName : "Email ID");
        // Set user info
        txtEmail.setText(tempCurrentUserEmail != null ? tempCurrentUserEmail : tempUser.getEmail());

        // Logout
        btnLogout.setOnClickListener(v -> {
            // Logout
            FirebaseAuth.getInstance().signOut();

            // NAVIGATE ONLY ON SUCCESS
            Intent intent = new Intent(this, LoginActivity.class);
            // Set flags
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Start activity to FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent);
            // Finish activity
            finish();
        });

        // Show dialog
        tempDialog.show();
    }
}