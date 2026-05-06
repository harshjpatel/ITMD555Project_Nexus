// Name: Harsh Patel (A20369913)

package com.example.nexus;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.auth.FirebaseAuth;
import android.widget.TextView;
import com.google.firebase.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

// Dashboard Activity
public class DashboardActivity extends AppCompatActivity {

    RecyclerView tempRecyclerView;
    FeedAdapter tempAdapter;
    List<Post> tempPostList;

    FirebaseFirestore tempFirebaseDB;

    private String tempCurrentUserName;
    private String tempCurrentUserEmail;

    private int tempPendingRequests = 0;
    private boolean tempShowingBookmarks = false;
    private List<String> tempUserCommunityIds = new ArrayList<>();
    private boolean tempIsDateFilterActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the activity
        super.onCreate(savedInstanceState);
        // Set the layout for the activity_dashboard.xml
        setContentView(R.layout.activity_dashboard);

        // Set up the RecyclerView
        tempRecyclerView = findViewById(R.id.feedRecycler);
        // Set the layout manager for the RecyclerView
        tempRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter
        tempPostList = new ArrayList<>();
        // Set the adapter for the RecyclerView
        tempAdapter = new FeedAdapter(this, tempPostList);
        // Set the adapter for the RecyclerView
        tempRecyclerView.setAdapter(tempAdapter);

        // Set up the search bar
        EditText tempSearchPosts = findViewById(R.id.searchPosts);
        // Set up the search bar listener
        tempSearchPosts.addTextChangedListener(new android.text.TextWatcher() {
            // Set up beforeTextChanged
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            // Set up onTextChanged
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the adapter
                tempAdapter.filter(s.toString());
            }
            // Set up afterTextChanged
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Initialize the Firebase Firestore
        tempFirebaseDB = FirebaseFirestore.getInstance();

        // Get the user's name and email
        tempCurrentUserName = getIntent().getStringExtra("userName");
        tempCurrentUserEmail = getIntent().getStringExtra("userEmail");

        // Set the user's name and email in the adapter
        if (tempCurrentUserName != null && tempCurrentUserEmail != null) {
            // Set the user's name and email in the adapter
            tempAdapter.setCurrentUser(tempCurrentUserName, tempCurrentUserEmail);
        }

        // Load the user's feed
        loadUserFeed();
        // Schedule the widget update
        scheduleWidgetUpdates();

        // Set up the add post button
        FloatingActionButton addPostFab = findViewById(R.id.addPostFab);
        // Set up the add post button listener
        addPostFab.setOnClickListener(v -> showCreateOptions());

        // Set up the show bookmarks button
        findViewById(R.id.btnShowBookmarks).setOnClickListener(v -> toggleBookmarksView());

        // Set up the filter button
        findViewById(R.id.btnFilterCommunities).setOnClickListener(v -> {
            // CustomizeFeedActivity.class
            Intent intent = new Intent(this, CustomizeFeedActivity.class);
            // Pass the user's name and email to the next activity
            intent.putExtra("userName", tempCurrentUserName);
            intent.putExtra("userEmail", tempCurrentUserEmail);
            // startActivity
            startActivity(intent);
        });

        // Set up the filter date button
        findViewById(R.id.btnFilterDate).setOnClickListener(v -> {
            // Check if date filter is active
            if (tempIsDateFilterActive) {
                // Clear the date filter
                clearDateFilter();
            } else {
                // Show the date picker
                showDatePicker();
            }
        });

        // Set up the user profile button
        findViewById(R.id.btnUserProfile).setOnClickListener(v -> showUserProfileDialog());

        // Request Notification Permission for Android 13+
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // Set up the date picker
    private void showDatePicker() {
        // Set up the date picker
        com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> dateRangePicker =
                // Set up the date picker
                com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker()
                        // Set up the title
                        .setTitleText("Filter posts by date range")
                        // Set up the theme
                        .build();

        // Set up the date picker listener
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            // Filter the adapter
            if (selection.first != null && selection.second != null) {
                // Filter the adapter
                filterPostsByDateRange(selection.first, selection.second);
            }
        });

        // Show the date picker
        dateRangePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");
    }

    // Clear the date filter
    private void clearDateFilter() {
        // Clear the date filter
        resetDateFilterUI();
        // Set up the adapter
        tempAdapter.updateList(tempPostList);
        // Show all posts
        Toast.makeText(this, "Showing all posts", Toast.LENGTH_SHORT).show();
    }

    // Reset the date filter UI
    private void resetDateFilterUI() {
        // Reset the date filter UI
        tempIsDateFilterActive = false;
        // Set up the filter button
        android.widget.ImageButton btn = findViewById(R.id.btnFilterDate);
        // Set up the filter button
        btn.setColorFilter(null);
    }

    // Filter the adapter
    private void filterPostsByDateRange(long startTimestampMs, long endTimestampMs) {
        // Normalize start to start of day
        java.util.Calendar rightNow = java.util.Calendar.getInstance();
        // Set up the calendar
        rightNow.setTimeInMillis(startTimestampMs);
        // Set up the Hours
        rightNow.set(java.util.Calendar.HOUR_OF_DAY, 0);
        // Set up the Minutes
        rightNow.set(java.util.Calendar.MINUTE, 0);
        // Set up the Seconds
        rightNow.set(java.util.Calendar.SECOND, 0);
        // Set up the Milliseconds
        rightNow.set(java.util.Calendar.MILLISECOND, 0);
        // Get the time in milliseconds
        long rangeStart = rightNow.getTimeInMillis();

        // Normalize end to end of day
        rightNow.setTimeInMillis(endTimestampMs);
        // Set up the Hours
        rightNow.set(java.util.Calendar.HOUR_OF_DAY, 23);
        // Set up the Minutes
        rightNow.set(java.util.Calendar.MINUTE, 59);
        // Set up the Seconds
        rightNow.set(java.util.Calendar.SECOND, 59);
        // Set up the Milliseconds
        rightNow.set(java.util.Calendar.MILLISECOND, 999);
        // Get the time in milliseconds
        long rangeEnd = rightNow.getTimeInMillis();

        // Set up the date filter UI
        tempIsDateFilterActive = true;
        // Set up the filter button
        android.widget.ImageButton btn = findViewById(R.id.btnFilterDate);
        // Set up the filter button
        btn.setColorFilter(android.graphics.Color.parseColor("#FF9800"));

        // Filter the adapter
        List<Post> filteredList = new ArrayList<>();
        // Set up the adapter
        for (Post tempPost : tempPostList) {
            // Filter the adapter
            if (tempPost.getTimestamp() >= rangeStart && tempPost.getTimestamp() <= rangeEnd) {
                // Add the post to the filtered list
                filteredList.add(tempPost);
            }
        }

        // Set up the adapter
        tempAdapter.updateList(filteredList);

        // Check if there are any posts
        if (filteredList.isEmpty()) {
            // Set up the adapter
            Toast.makeText(this, "No posts found for this range", Toast.LENGTH_SHORT).show();
        }
    }

    // Set up the user profile dialog
    private void showUserProfileDialog() {
        // Get the current user
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Set up the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_profile, null);
        // Set up the dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        // Set up the dialog
        if (dialog.getWindow() != null) {
            // Set up the dialog window
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set up the tempTxtName
        TextView tempTxtName = dialogView.findViewById(R.id.profileName);
        // Set up the tempTxtEmail
        TextView tempTxtEmail = dialogView.findViewById(R.id.profileEmail);
        // Set up the tempBtnLogout
        Button tempBtnLogout = dialogView.findViewById(R.id.btnLogout);
        // Set up the tempBtnRefreshWidget
        Button tempBtnRefreshWidget = dialogView.findViewById(R.id.btnRefreshWidget);
        Button btnShowFCMToken = dialogView.findViewById(R.id.btnShowFCMToken);

        // Show FCM Token logic
        btnShowFCMToken.setOnClickListener(v -> {
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Fetching FCM token failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String token = task.getResult();
                    new MaterialAlertDialogBuilder(this)
                        .setTitle("FCM Registration Token")
                        .setMessage(token)
                        .setPositiveButton("Copy", (d, w) -> {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("FCM Token", token);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Token copied!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Close", null)
                        .show();
                });
        });

        // Set up the refresh widget button
        tempBtnRefreshWidget.setOnClickListener(v -> {
            // Trigger the widget update
            triggerWidgetUpdate();
            Toast.makeText(this, "Widget update requested", Toast.LENGTH_SHORT).show();
            // Dismiss the dialog
            dialog.dismiss();
        });

        // Get the user's name and email
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    // Check if the document exists
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        // Check if the name exists
                        if (name != null) {
                            // Set up the user's name
                            tempCurrentUserName = name;
                            tempTxtName.setText(name);
                        }
                        // Check if the email exists
                        if (email != null) {
                            // Set up the user's email
                            tempCurrentUserEmail = email;
                            tempTxtEmail.setText(email);
                        }
                    }
                });

        // Set up the user's name and email
        tempTxtName.setText(tempCurrentUserName != null ? tempCurrentUserName : "Email ID");
        // Set up the user's email
        tempTxtEmail.setText(tempCurrentUserEmail != null ? tempCurrentUserEmail : user.getEmail());

        // Set up the logout button
        tempBtnLogout.setOnClickListener(v -> {
            // Logout the user
            FirebaseAuth.getInstance().signOut();
            // Set up the intent
            Intent intent = new Intent(this, LoginActivity.class);
            // Set up the intent
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Set up the intent
            startActivity(intent);
            // Finish the activity
            finish();
        });

        // Show the dialog
        dialog.show();
    }

    // Toggle the bookmarks view
    private void toggleBookmarksView() {
        // Toggle the bookmarks view
        tempShowingBookmarks = !tempShowingBookmarks;
        // Set up the filter button
        android.widget.ImageButton btn = findViewById(R.id.btnShowBookmarks);
        // Set up the adapter
        tempAdapter.setTempShowingBookmarksMode(tempShowingBookmarks);

        if (tempIsDateFilterActive) {
            // Clear the date filter
            resetDateFilterUI();
        }

        if (tempShowingBookmarks) {
            // Set up the filter button
            btn.setColorFilter(android.graphics.Color.parseColor("#FF9800"));
            // Load the bookmarked posts
            loadBookmarkedPosts();
        } else {
            // Set up the filter button
            btn.setColorFilter(null);
            // Load the user's feed
            loadUserFeed();
        }
    }

    // Load the user's bookmarked posts
    private void loadBookmarkedPosts() {
        // Get the current user
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Check if the user is logged in
        if (currentUser == null) return;
        // Get the user's ID
        String tempUID = currentUser.getUid();
        // Clear the tempPostList
        tempPostList.clear();
        // Set up the adapter
        tempAdapter.notifyDataSetChanged();

        // Check if the user has any bookmarked posts
        if (tempUserCommunityIds == null || tempUserCommunityIds.isEmpty()) {
            Toast.makeText(this, "No communities found to search bookmarks", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up the tempUserCommunityIds.size();
        tempPendingRequests = tempUserCommunityIds.size();
        // Traverse the firebaseDB
        for (String tempCommunityId : tempUserCommunityIds) {
            // firebaseDB
            tempFirebaseDB.collection("communities").document(tempCommunityId).get().addOnSuccessListener(commDoc -> {
                String communityName = commDoc.getString("name");
                // firebaseDB
                tempFirebaseDB.collection("communities").document(tempCommunityId).collection("posts").get().addOnSuccessListener(querySnapshot -> {
                    // Traverse the postDoc
                    for (DocumentSnapshot postDoc : querySnapshot.getDocuments()) {
                        // firebaseDB
                        postDoc.getReference().collection("bookmarks").document(tempUID).get().addOnSuccessListener(bookmarkDoc -> {
                            // Check if the post is bookmarked
                            if (bookmarkDoc.exists()) {
                                // Set up the post
                                Post tempPost = postDoc.toObject(Post.class);
                                // Check if the post is not null
                                if (tempPost != null) {
                                    // Set up the post ID
                                    tempPost.setPostId(postDoc.getId());
                                    // Set up the community ID
                                    tempPost.setCommunityId(tempCommunityId);
                                    // Set up the community name
                                    tempPost.setAuthorName(communityName);
                                    // Set up the bookmarked
                                    tempPost.setBookmarked(true);
                                    // Add the post to the tempPostList
                                    tempPostList.add(tempPost);
                                }
                            }
                            // Check if all bookmarks are finished
                            checkAllBookmarksFinished();
                        });
                    }
                    // Check if all bookmarks are finished
                    if (querySnapshot.isEmpty()) {
                        // Check if all bookmarks are finished
                        checkAllBookmarksFinished();
                    }
                });
            });
        }
    }

    // Check if all bookmarks are finished
    private void checkAllBookmarksFinished() {
        // Sort the tempPostList
        tempPostList.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
        // Set up the adapter
        tempAdapter.updateList(tempPostList);
    }

    private void showCreatePostDialog() {
        // Set up the dialog
        MaterialAlertDialogBuilder tempBuilder = new MaterialAlertDialogBuilder(this);
        // Set up the inflater
        LayoutInflater inflater = getLayoutInflater();
        // Set up the view
        View dialogView = inflater.inflate(R.layout.dialog_create_post, null);
        // Set up the builder
        tempBuilder.setView(dialogView);
        // Set up the dialog
        AlertDialog dialog = tempBuilder.create();
        // Set up the dialog
        if (dialog.getWindow() != null) {
            // Set up the dialog window
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set up the dialog's window
            dialog.getWindow().setDimAmount(0.7f);
        }

        // Set up the editTitle
        EditText editTitle = dialogView.findViewById(R.id.editPostTitle);
        // Set up the editContent
        EditText editContent = dialogView.findViewById(R.id.editPostContent);
        // Set up the communityAutoComplete
        AutoCompleteTextView communityAutoComplete = dialogView.findViewById(R.id.communityAutoComplete);
        // Set up the btnPost
        Button btnPost = dialogView.findViewById(R.id.btnPost);
        // Set up the btnCancel
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Get the current user
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Check if the user is logged in
        if (currentUser == null) {
            return;
        }
        // Get the user's ID
        String tempUID = currentUser.getUid();
        // firebaseDB
        tempFirebaseDB.collection("users").document(tempUID).get().addOnSuccessListener(documentSnapshot -> {
            @SuppressWarnings("unchecked")
            List<String> communityIds = (List<String>) documentSnapshot.get("selectedCommunities");
            // Check if the user has any communities
            if (communityIds != null && !communityIds.isEmpty()) {
                // Set up the communityNames
                List<String> tempCommunityNames = new ArrayList<>();
                // Set up the nameToIdMap
                Map<String, String> nameToIdMap = new HashMap<>();

                // Traverse the communityIds
                for (String tempIdx : communityIds) {
                    // firebaseDB
                    tempFirebaseDB.collection("communities").document(tempIdx).get().addOnSuccessListener(commDoc -> {
                        // Set up the tempName
                        String tempName = commDoc.getString("name");
                        // Check if the tempName is not null
                        if (tempName != null) {
                            // Add the name to the tempCommunityNames
                            tempCommunityNames.add(tempName);
                            // Add the tempIdx to the nameToIdMap
                            nameToIdMap.put(tempName, tempIdx);
                        }

                        // Check if all communities are finished
                        if (tempCommunityNames.size() == communityIds.size()) {
                            // Set up the adapter
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, tempCommunityNames);
                            // Set up the adapter
                            communityAutoComplete.setAdapter(adapter);
                        }
                    });
                }

                // Set up the btnPost
                btnPost.setOnClickListener(v -> {
                    // Get the title
                    String title = editTitle.getText().toString().trim();
                    // Get the content
                    String content = editContent.getText().toString().trim();
                    // Get the selected community.
                    String selectedCommName = communityAutoComplete.getText().toString();

                    // Check if the title and content are empty
                    if (title.isEmpty() || content.isEmpty() || selectedCommName.isEmpty() || selectedCommName.equals("Select Community")) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String commId = nameToIdMap.get(selectedCommName);
                    // Set up the btnPost
                    savePostToFirestore(title, content, commId, dialog);
                });
            }
        });

        // Set up the btnCancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        // Show the dialog
        dialog.show();
    }

    // Show the create options dialog
    private void showCreateOptions() {
        // Set up the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_option, null);
        // Set up the dialog
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        // Set up the dialog
        if (dialog.getWindow() != null) {
            // Set up the dialog window
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Set up the dialog's window
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            // Set up the dialog's window
            params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;

            // Set up the dialog's window
            params.x = 60;
            params.y = 260;

            // Set up the dialog's window
            dialog.getWindow().setAttributes(params);
            // Set up the dialog's window
            dialog.getWindow().setDimAmount(0.2f);
        }

        // Set up the findViewById
        dialogView.findViewById(R.id.optionCreatePost).setOnClickListener(v -> {
            // Dismiss the dialog
            dialog.dismiss();
            // Show the create post dialog
            showCreatePostDialog();
        });

        // Set up the findViewById
        dialogView.findViewById(R.id.optionCreateCommunity).setOnClickListener(v -> {
            // Dismiss the dialog
            dialog.dismiss();
            // Show the create community dialog
            showCreateCommunityDialog();
        });

        dialog.show();
    }

    // Show the create community dialog
    private void showCreateCommunityDialog() {
        // Set up the dialog
        MaterialAlertDialogBuilder tempBuilder = new MaterialAlertDialogBuilder(this);
        // Set up the inflater
        LayoutInflater inflater = getLayoutInflater();
        // Set up the view
        View dialogView = inflater.inflate(R.layout.dialog_create_community, null);
        // Set up the builder
        tempBuilder.setView(dialogView);

        AlertDialog dialog = tempBuilder.create();
        // Set up the dialog
        if (dialog.getWindow() != null) {
            // Set up the dialog window
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Set up the dialog's window
            dialog.getWindow().setDimAmount(0.7f);
        }

        // Set up the editName
        EditText editName = dialogView.findViewById(R.id.editCommunityName);
        // Set up the editSummary
        EditText editSummary = dialogView.findViewById(R.id.editCommunitySummary);
        // Set up the btnCreate
        Button btnCreate = dialogView.findViewById(R.id.btnCreateComm);
        // Set up the btnCancel
        Button btnCancel = dialogView.findViewById(R.id.btnCancelComm);

        // Set up the btnCreate
        btnCreate.setOnClickListener(v -> {
            // Get the name
            String name = editName.getText().toString().trim();
            // Get the summary
            String summary = editSummary.getText().toString().trim();

            // Check if the name and summary are empty
            if (name.isEmpty() || summary.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the name already exists
            tempFirebaseDB.collection("communities")
                    .whereEqualTo("name", name)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Suggest an suggestAlternativeName
                            suggestAlternativeName(name, summary, dialog);
                        } else {
                            // Save the saveCommunityToFirestore
                            saveCommunityToFirestore(name, summary, dialog);
                        }
                    });
        });

        // Set up the btnCancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        // Show the dialog
        dialog.show();
    }

    // Suggest an alternative name
    private void suggestAlternativeName(String originalName, String summary, AlertDialog dialog) {
        // Set up the suggestedName
        findAvailableName(originalName, 1, summary, dialog);
    }

    // Find an available name
    private void findAvailableName(String baseName, int suffix, String summary, AlertDialog dialog) {
        // Set up the suggestedName
        String suggestedName = baseName + " " + suffix;
        // firebaseDB
        tempFirebaseDB.collection("communities")
                .whereEqualTo("name", suggestedName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("Name already exists")
                                .setMessage("A community with that name already exists. How about '" + suggestedName + "' instead?")
                                .setPositiveButton("Use Suggested", (d, w) -> {
                                    saveCommunityToFirestore(suggestedName, summary, dialog);
                                })
                                .setNegativeButton("Change Manually", null)
                                .show();
                    } else {
                        // Suggest an alternative name
                        findAvailableName(baseName, suffix + 1, summary, dialog);
                    }
                });
    }

    // Save the community to Firestore
    private void saveCommunityToFirestore(String name, String summary, AlertDialog dialog) {
        Map<String, Object> commMap = new HashMap<>();
        commMap.put("name", name);
        commMap.put("summary", summary);
        commMap.put("timestamp", System.currentTimeMillis());
        commMap.put("icon", "ic_launcher_foreground");
        // firebaseDB
        tempFirebaseDB.collection("communities")
                .add(commMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Community created!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                // Handle error
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create community", Toast.LENGTH_SHORT).show();
                });
    }

    // Save the post to Firestore
    private void savePostToFirestore(String title, String content, String communityId, AlertDialog dialog) {
        // Get the current user
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Check if the user is logged in
        if (currentUser == null) return;
        // Get the user's ID
        String uid = currentUser.getUid();
        // firebaseDB
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("content", content);
        postMap.put("communityId", communityId);
        postMap.put("authorId", uid);
        postMap.put("authorName", tempCurrentUserName);
        postMap.put("authorEmail", currentUser.getEmail());
        postMap.put("timestamp", System.currentTimeMillis());
        postMap.put("upvotes", 0);
        postMap.put("downvotes", 0);
        postMap.put("commentCount", 0);
        postMap.put("bookmarkCount", 0);

        // firebaseDB
        tempFirebaseDB.collection("communities")
                .document(communityId)
                .collection("posts")
                .add(postMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    triggerWidgetUpdate(); // Trigger widget refresh on the device
                    loadUserFeed();
                })
                // Handle error
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create post", Toast.LENGTH_SHORT).show();
                });
    }

    // Load the user's feed
    private void loadUserFeed() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Check if the user is logged in
        if (auth.getCurrentUser() == null) return;
        // Get the user's ID
        String uid = auth.getCurrentUser().getUid();

        // firebaseDB
        tempFirebaseDB.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Set up the tempCurrentUserName
                    tempCurrentUserName = documentSnapshot.getString("name");
                    // Set up the tempCurrentUserEmail
                    tempCurrentUserEmail = documentSnapshot.getString("email");
                    // Set up the adapter
                    tempAdapter.setCurrentUser(tempCurrentUserName, tempCurrentUserEmail);

                    // Check if the user has any communities
                    @SuppressWarnings("unchecked")
                    List<String> selected = (List<String>) documentSnapshot.get("selectedCommunities");

                    // Check if the user has any communities
                    if (selected == null || selected.isEmpty()) {
                        return;
                    }

                    // Set up the tempUserCommunityIds
                    tempUserCommunityIds = selected;
                    // Load the user's feed
                    fetchPosts(selected);
                });
    }

    // Fetch the user's feed
    private void fetchPosts(List<String> communities) {
        // Clear the tempPostList
        tempPostList.clear();
        // Set up the pendingRequests
        tempPendingRequests = communities.size();

        // Check if there are any posts
        if (tempPendingRequests == 0) {
            // Set up the adapter
            tempAdapter.notifyDataSetChanged();
            return;
        }

        // firebaseDB
        String uid = FirebaseAuth.getInstance().getUid();

        // Traverse the communities
        for (String communityId : communities) {
            // firebaseDB
            tempFirebaseDB.collection("communities").document(communityId).get()
                    .addOnSuccessListener(communityDoc -> {
                        // Get the community name
                        String communityName = communityDoc.getString("name");

                        tempFirebaseDB.collection("communities")
                                .document(communityId)
                                .collection("posts")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(5)
                                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                    if (e != null || queryDocumentSnapshots == null) {
                                        // Handle error
                                        onRequestFinished();
                                        return;
                                    }

                                    // Traverse the dc
                                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                        // Check if the doc is not null
                                        DocumentSnapshot doc = dc.getDocument();
                                        // Check if the doc is not null
                                        Post post = doc.toObject(Post.class);
                                        // Check if the post is not null
                                        if (post == null) continue;

                                        // Set up the post
                                        post.setPostId(doc.getId());
                                        // Set up the community ID
                                        post.setCommunityId(communityId);
                                        // Set up the community name
                                        post.setAuthorName(communityName);

                                        switch (dc.getType()) {
                                            // ADDED
                                            case ADDED:
                                                updateOrAddPost(post, uid);
                                                break;
                                            // MODIFIED
                                            case MODIFIED:
                                                updateOrAddPost(post, uid);
                                                break;
                                            // REMOVED
                                            case REMOVED:
                                                removePost(post.getPostId());
                                                break;
                                        }
                                    }
                                    // Check if all posts are finished
                                    onRequestFinished();
                                });
                    });
        }
    }

    // Update or add the post
    private void updateOrAddPost(Post post, String uid) {
        // Fetch extra info (bookmarks/comments/votes) then update UI
        post.getReference(tempFirebaseDB).collection("bookmarks").document(uid).get()
            .addOnSuccessListener(bookmarkDoc -> {
                // Check if the post is bookmarked
                post.setBookmarked(bookmarkDoc.exists());
                
                // Fetch user's vote for this post
                tempFirebaseDB.collection("users").document(uid).collection("votes").document(post.getPostId()).get()
                    .addOnSuccessListener(voteDoc -> {
                        // Check if the voteDoc exists
                        if (voteDoc.exists()) {
                            // Set up the post
                            String type = voteDoc.getString("type");
                            // Set up the post
                            post.setUpvoted("up".equals(type));
                            // Set down the post
                            post.setDownvoted("down".equals(type));
                        }

                        post.getReference(tempFirebaseDB).collection("comments").count().get(AggregateSource.SERVER)
                            .addOnSuccessListener(aggregateQuerySnapshot -> {
                                post.setCommentCount((int) aggregateQuerySnapshot.getCount());
                                
                                synchronized (tempPostList) {
                                    // Set up the tempPostList
                                    int index = -1;
                                    // Traverse the tempPostList
                                    for (int i = 0; i < tempPostList.size(); i++) {
                                        // Check if the post is already in the tempPostList
                                        if (tempPostList.get(i).getPostId().equals(post.getPostId())) {
                                            // Set up the index
                                            index = i;
                                            // Break out of the loop
                                            break;
                                        }
                                    }

                                    // Check if the post is already in the tempPostList
                                    if (tempShowingBookmarks && !post.isBookmarked()) {
                                        // Remove the post from the tempPostList
                                        if (index != -1) {
                                            // tempPostList
                                            tempPostList.remove(index);
                                        }
                                        // Update the UI
                                    } else {
                                        // Update the post
                                        if (index != -1) {
                                            // Set up the post
                                            tempPostList.set(index, post);
                                        } else {
                                            // Add the post to the tempPostList
                                            if (!tempShowingBookmarks || post.isBookmarked()) {
                                                // Add the post to the tempPostList
                                                tempPostList.add(post);
                                            }
                                        }
                                    }
                                    tempPostList.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                                }
                                runOnUiThread(() -> tempAdapter.updateList(new ArrayList<>(tempPostList)));
                            });
                    });
            });
    }

    // removePost
    private void removePost(String postId) {
        // Remove the post from the tempPostList
        synchronized (tempPostList) {
            // Remove the post from the tempPostList
            tempPostList.removeIf(p -> p.getPostId().equals(postId));
        }
        // Update the UI
        runOnUiThread(() -> tempAdapter.updateList(new ArrayList<>(tempPostList)));
    }

    // On request finished
    private void onRequestFinished() {
        // Decrement the pendingRequests
        tempPendingRequests--;
        // Check if all posts are finished
        if (tempPendingRequests <= 0) {
            // Sort the tempPostList
            tempPostList.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
            // Update the UI
            tempAdapter.updateList(tempPostList);
        }
    }

    // Trigger the widget update
    private void triggerWidgetUpdate() {
        // Create a new one-time work request
        androidx.work.OneTimeWorkRequest updateRequest =
                // Set up the work request
                new androidx.work.OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                        .build();

        // Schedule the update request
        androidx.work.WorkManager.getInstance(this).enqueueUniqueWork(
                // Set up the work name
                "widget_update_manual",
                // Replace the existing work
                androidx.work.ExistingWorkPolicy.REPLACE,
                // Set up the updateRequest
                updateRequest
        );
    }

    // Schedule the widget update
    private void scheduleWidgetUpdates() {
        // Create a new one-time work request
        Intent intent = new Intent(this, PostWidget.class);
        // Set the action
        intent.setAction("com.example.nexus.UPDATE_WIDGET_MANUAL");
        // Send the broadcast
        sendBroadcast(intent);
        // Log the widget update
        Log.d("DashboardActivity", "Widget update kickstarter");
    }
}