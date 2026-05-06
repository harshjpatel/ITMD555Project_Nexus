// Name: Harsh Patel (A20369913)

package com.example.nexus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// MainActivity.java
public class PostDetailActivity extends AppCompatActivity {

    private TextView tempTvCommunityName, tempTvPostTime, tempTvPostTitle, tempTvPostContent, tempTvAuthorEmail;
    private MaterialCardView tempCardPostImage;
    private ImageButton tempBtnBookmark, tempBtnSendComment;
    private MaterialButton tempBtnUpvote, tempBtnDownvote, tempBtnShare;
    private EditText tempEtComment;
    
    private FirebaseFirestore firebaseDB;
    private String tempPostId, tempCommunityId;
    private Post tempCurrentPost;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set theme before super.onCreate
        super.onCreate(savedInstanceState);
        // Set theme before super.onCreate
        setContentView(R.layout.activity_post_detail);
        // Set theme before super.onCreate
        firebaseDB = FirebaseFirestore.getInstance();
        
        // Try to get the post object passed from the adapter
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // New API
            tempCurrentPost = getIntent().getSerializableExtra("POST_DATA", Post.class);
        } else {
            // (Post) getIntent().getSerializableExtra("POST_DATA");
            tempCurrentPost = (Post) getIntent().getSerializableExtra("POST_DATA");
        }

        // Initialize views
        tempInitViews();
        // Set up toolbar
        tempSetupToolbar();

        // If post data is available, display it
        if (tempCurrentPost != null) {
            // Use cached data if available
            tempPostId = tempCurrentPost.getPostId();
            // Use cached data if available
            tempCommunityId = tempCurrentPost.getCommunityId();
            // Use cached data if available
            displayPost();
            // Fetch comments
            tempLoadComments();
            // Still fetch latest details in background to sync upvotes/downvotes
            refreshPostDetails();
        } else {
            // Fallback for deep links or notifications
            tempPostId = getIntent().getStringExtra("POST_ID");
            // Fallback for deep links or notifications
            tempCommunityId = getIntent().getStringExtra("COMMUNITY_ID");
            // tempPostId != null && tempCommunityId != null
            if (tempPostId != null && tempCommunityId != null) {
                // Fetch post details
                tempLoadPostDetails();
                // Fetch comments
                tempLoadComments();
            }
        }

        // Set up click listeners tempBtnSendComment
        tempBtnSendComment.setOnClickListener(v -> postComment());
        // Set up click listeners tempBtnShare
        tempBtnShare.setOnClickListener(v -> sharePost());
        // Set up click listeners tempBtnUpvote
        tempBtnUpvote.setOnClickListener(v -> handleVote(true));
        // Set up click listeners tempBtnDownvote
        tempBtnDownvote.setOnClickListener(v -> handleVote(false));
        // Set up click listeners tempBtnBookmark
        tempBtnBookmark.setOnClickListener(v -> handleBookmark());
    }

    // Fetch latest post details in background
    private void refreshPostDetails() {
        // Listen for changes to the post document
        firebaseDB.collection("communities").document(tempCommunityId).collection("posts").document(tempPostId)
            .addSnapshotListener((snapshot, e) -> {
                // Handle errors
                if (snapshot != null && snapshot.exists()) {
                    // Update local post object
                    Post updatedPost = snapshot.toObject(Post.class);
                    if (updatedPost != null) {
                        // Update local state wasUpvoted
                        boolean wasUpvoted = tempCurrentPost.isUpvoted();
                        // Update local state wasDownvoted
                        boolean wasDownvoted = tempCurrentPost.isDownvoted();
                        // Update local state wasBookmarked
                        boolean wasBookmarked = tempCurrentPost.isBookmarked();
                        // Update local post object
                        tempCurrentPost = updatedPost;
                        // Set post ID
                        tempCurrentPost.setPostId(snapshot.getId());
                        // Set community ID
                        tempCurrentPost.setCommunityId(tempCommunityId);
                        
                        // Re-apply local session state wasUpvoted
                        tempCurrentPost.setUpvoted(wasUpvoted);
                        // Re-apply local session state wasDownvoted
                        tempCurrentPost.setDownvoted(wasDownvoted);
                        // Re-apply local session state wasBookmarked
                        tempCurrentPost.setBookmarked(wasBookmarked);
                        // Re-display post
                        displayPost();
                    }
                }
            });
    }

    // Initialize views
    private void tempInitViews() {
        tempTvCommunityName = findViewById(R.id.tvCommunityName);
        tempTvPostTime = findViewById(R.id.tvPostTime);
        tempTvPostTitle = findViewById(R.id.tvPostTitle);
        tempTvPostContent = findViewById(R.id.tvPostContent);
        tempCardPostImage = findViewById(R.id.cardPostImage);
        tempBtnBookmark = findViewById(R.id.btnBookmark);
        tempBtnUpvote = findViewById(R.id.btnUpvote);
        tempBtnDownvote = findViewById(R.id.btnDownvote);
        tempBtnShare = findViewById(R.id.btnShare);
        tempEtComment = findViewById(R.id.etComment);
        tempBtnSendComment = findViewById(R.id.btnSendComment);
        tempTvAuthorEmail = findViewById(R.id.tvAuthorEmail);
        findViewById(R.id.btnUserProfile).setOnClickListener(v -> showUserProfileDialog());

        RecyclerView rvComments = findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
    }

    // Show user profile dialog
    private void showUserProfileDialog() {
        // Check if user is logged in
        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // If not, return
        if (user == null) return;

        // Initialize dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_profile, null);
        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        // Set dialog background to transparent
        if (dialog.getWindow() != null) {
            // Set dialog background to transparent
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtName = dialogView.findViewById(R.id.profileName);
        TextView txtEmail = dialogView.findViewById(R.id.profileEmail);
        Button btnLogout = dialogView.findViewById(R.id.btnLogout);
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

        // ALWAYS FETCH LATEST FROM FIRESTORE FOR ACCURACY
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Get user data
                        String tempName = doc.getString("name");
                        String tempEmail = doc.getString("email");
                        if (tempName != null) {
                            // Set name
                            txtName.setText(tempName);
                        }
                        if (tempEmail != null) {
                            // Set email
                            txtEmail.setText(tempEmail);
                        }
                    }
                });

        // Set user data
        txtName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Email ID");
        // Set email
        txtEmail.setText(user.getEmail());

        // Set up logout button
        btnLogout.setOnClickListener(v -> {
            // Sign out user
            FirebaseAuth.getInstance().signOut();
            // Go back to log in screen
            Intent tempIntent = new Intent(this, LoginActivity.class);
            tempIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(tempIntent);
            finish();
        });

        dialog.show();
    }

    // Set up toolbar
    private void tempSetupToolbar() {
        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        // Set toolbar as action bar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Enable back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    // Fetch post details
    private void tempLoadPostDetails() {
        // Fetch post details
        firebaseDB.collection("communities").document(tempCommunityId).collection("posts").document(tempPostId).get().addOnSuccessListener(documentSnapshot -> {
            // Handle errors
            tempCurrentPost = documentSnapshot.toObject(Post.class);
            // Set post ID
            if (tempCurrentPost != null) {
                // Set post ID
                tempCurrentPost.setPostId(documentSnapshot.getId());
                // Set community ID
                tempCurrentPost.setCommunityId(tempCommunityId);
                // Display post
                displayPost();
            }
            // Fetch comments
        }).addOnFailureListener(e -> {
            // Handle errors
            Toast.makeText(this, "Error loading post", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // Display post
    private void displayPost() {
        // Set post data
        tempTvPostTitle.setText(tempCurrentPost.getTitle());
        tempTvPostContent.setText(tempCurrentPost.getContent());
        tempTvCommunityName.setText("r/" + (tempCurrentPost.getCommunityId() != null ? tempCurrentPost.getCommunityId() : "nexus"));
        
        // Use author email as requested
        tempTvAuthorEmail.setText(tempCurrentPost.getAuthorEmail() != null ? tempCurrentPost.getAuthorEmail() :
                            (tempCurrentPost.getAuthorName() != null ? tempCurrentPost.getAuthorName() : "Anonymous"));

        if (tempCurrentPost.getTimestamp() > 0) {
            // Set post time
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
            tempTvPostTime.setText(sdf.format(new Date(tempCurrentPost.getTimestamp())));
        }

        if (tempCurrentPost.getImageUrl() != null && !tempCurrentPost.getImageUrl().isEmpty()) {
            tempCardPostImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(tempCurrentPost.getImageUrl())
                    .centerCrop()
                    .into((ImageView) findViewById(R.id.ivPostImage));
        } else {
            tempCardPostImage.setVisibility(View.GONE);
        }

        // Set bookmark count text
        tempBtnUpvote.setText(String.valueOf(tempCurrentPost.getUpvotes()));
        tempBtnDownvote.setText(String.valueOf(tempCurrentPost.getDownvotes()));
        // Set bookmark count text
        updateInteractionButtons();
    }

    // Set up interaction buttons
    private void updateInteractionButtons() {
        // Set up interaction buttons
        if (tempCurrentPost.isBookmarked()) {
            // Set bookmark icon
            tempBtnBookmark.setImageResource(R.drawable.ic_bookmark);
            // Set bookmark color
            tempBtnBookmark.setColorFilter(ContextCompat.getColor(this, R.color.orange));
        } else {
            tempBtnBookmark.setImageResource(R.drawable.ic_bookmark_border);
            tempBtnBookmark.clearColorFilter();
        }

        // Set up interaction buttons
        if (tempCurrentPost.isUpvoted()) {
            // setIconTintResource
            tempBtnUpvote.setIconTintResource(R.color.upvote_green);
            tempBtnUpvote.setTextColor(ContextCompat.getColor(this, R.color.upvote_green));
        } else {
            tempBtnUpvote.setIconTintResource(R.color.subtitleText);
            tempBtnUpvote.setTextColor(ContextCompat.getColor(this, R.color.subtitleText));
        }

        if (tempCurrentPost.isDownvoted()) {
            tempBtnDownvote.setIconTintResource(R.color.downvote_red);
            tempBtnDownvote.setTextColor(ContextCompat.getColor(this, R.color.downvote_red));
        } else {
            tempBtnDownvote.setIconTintResource(R.color.subtitleText);
            tempBtnDownvote.setTextColor(ContextCompat.getColor(this, R.color.subtitleText));
        }
    }

    // Comment adapter
    private Comment replyingTo = null;

    // tempLoadComments
    private void tempLoadComments() {
        // Set up comments
        RecyclerView rvComments = findViewById(R.id.rvComments);
        firebaseDB.collection("communities").document(tempCommunityId).collection("posts")
                .document(tempPostId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        // Convert to list
                        List<Comment> tempComments = new ArrayList<>();
                        // Convert to list
                        for (DocumentSnapshot tempDoc : value.getDocuments()) {
                            // Convert to object
                            Comment tempComment = tempDoc.toObject(Comment.class);
                            // Set comment ID
                            if (tempComment != null) {
                                // Set comment ID
                                tempComment.setCommentId(tempDoc.getId());
                                // Set community ID
                                tempComments.add(tempComment);
                            }
                        }
                        
                        // Thread comments
                        List<Comment> threaded = threadComments(tempComments);

                        // Set up adapter
                        String tempUID = FirebaseAuth.getInstance().getUid();
                        if (tempUID != null) {
                            // Fetch comment votes and setup adapter
                            fetchCommentVotesAndSetupAdapter(threaded, rvComments, tempUID);
                        } else {
                            // Setup adapter
                            setupCommentAdapter(threaded, rvComments);
                        }
                    }
                });
    }

    // Fetch comment votes and setup adapter
    private void fetchCommentVotesAndSetupAdapter(List<Comment> comments, RecyclerView rvComments, String uid) {
        // Fetch comment votes
        firebaseDB.collection("users").document(uid).collection("commentVotes").get()
                .addOnSuccessListener(querySnapshot -> {
                    // Convert to map
                    Map<String, String> tempUserVotes = new HashMap<>();
                    // Convert to map
                    for (DocumentSnapshot docIdx : querySnapshot.getDocuments()) {
                        // Set user vote
                        tempUserVotes.put(docIdx.getId(), docIdx.getString("type"));
                    }

                    // Set up adapter
                    for (Comment tempCommentIdx : comments) {
                        // Set up adapter
                        String voteType = tempUserVotes.get(tempCommentIdx.getCommentId());
                        // Set up adapter
                        if (voteType != null) {
                            // Set up adapter
                            tempCommentIdx.setUpvoted("up".equals(voteType));
                            // Set down adapter
                            tempCommentIdx.setDownvoted("down".equals(voteType));
                        }
                    }
                    // Set up adapter
                    setupCommentAdapter(comments, rvComments);
                })
                // Handle errors
                .addOnFailureListener(e -> setupCommentAdapter(comments, rvComments));
    }

    // Set up adapter
    private void setupCommentAdapter(List<Comment> comments, RecyclerView tempTvComments) {
        // Set up adapter
        tempTvComments.setAdapter(new CommentAdapter(comments, new CommentAdapter.OnCommentActionListener() {
            // Handle comment click
            @Override
            public void onReply(Comment parentComment) {
                // Set up adapter
                replyingTo = parentComment;
                // Set up adapter
                tempEtComment.setHint("Replying to " + parentComment.getUserName() + "...");
                // Set up adapter
                tempEtComment.requestFocus();
            }

            // Handle comment vote
            @Override
            public void onVote(Comment comment, boolean isUpvote) {
                // Handle comment vote
                handleCommentVote(comment, isUpvote);
            }
        }));
    }

    // Thread comments
    private List<Comment> threadComments(List<Comment> flat) {
        // Thread result
        List<Comment> tempResult = new ArrayList<>();
        // Thread childrenMap
        Map<String, List<Comment>> tempChildrenMap = new HashMap<>();
        // Thread roots
        List<Comment> tempRoots = new ArrayList<>();

        // Thread flat
        for (Comment commentIdx : flat) {
            // Thread roots
            if (commentIdx.getParentCommentId() == null) {
                // Thread tempRoots
                tempRoots.add(commentIdx);
            } else {
                // Thread tempChildrenMap
                tempChildrenMap.computeIfAbsent(commentIdx.getParentCommentId(), k -> new ArrayList<>()).add(commentIdx);
            }
        }

        // Thread tempRoots
        for (Comment tempCommentIdx : tempRoots) {
            // Thread tempResult
            addCommentAndChildren(tempCommentIdx, tempChildrenMap, tempResult);
        }
        // Thread tempResult
        return tempResult;
    }

    // Add comment and children
    private void addCommentAndChildren(Comment parent, Map<String, List<Comment>> map, List<Comment> result) {
        // Thread result add parents
        result.add(parent);
        // tempChildren
        List<Comment> tempChildren = map.get(parent.getCommentId());
        // tempChildren checker
        if (tempChildren != null) {
            // Thread tempChild loop.
            for (Comment tempChild : tempChildren) {
                // Thread addCommentAndChildren
                addCommentAndChildren(tempChild, map, result);
            }
        }
    }

    // Handle comment vote
    private void handleCommentVote(Comment tempCommentIdx, boolean isUpvote) {
        // tempUID
        String tempUID = FirebaseAuth.getInstance().getUid();
        // tempUID checker
        if (tempUID == null) return;

        // Handle comment vote
        if (isUpvote) {
            if (tempCommentIdx.isUpvoted()) {
                // Set up adapter
                tempCommentIdx.setUpvotes(Math.max(0, tempCommentIdx.getUpvotes() - 1));
                // Set up adapter false
                tempCommentIdx.setUpvoted(false);
            } else {
                // Set up adapter
                tempCommentIdx.setUpvotes(tempCommentIdx.getUpvotes() + 1);
                // Set up adapter upVoted
                tempCommentIdx.setUpvoted(true);
                // Set up adapter isDownvoted
                if (tempCommentIdx.isDownvoted()) {
                    // Set up adapter
                    tempCommentIdx.setDownvotes(Math.max(0, tempCommentIdx.getDownvotes() - 1));
                    // Set up adapter isDownvoted
                    tempCommentIdx.setDownvoted(false);
                }
            }
        } else {
            if (tempCommentIdx.isDownvoted()) {
                tempCommentIdx.setDownvotes(Math.max(0, tempCommentIdx.getDownvotes() - 1));
                tempCommentIdx.setDownvoted(false);
            } else {
                tempCommentIdx.setDownvotes(tempCommentIdx.getDownvotes() + 1);
                tempCommentIdx.setDownvoted(true);
                if (tempCommentIdx.isUpvoted()) {
                    tempCommentIdx.setUpvotes(Math.max(0, tempCommentIdx.getUpvotes() - 1));
                    tempCommentIdx.setUpvoted(false);
                }
            }
        }

        // Update Firestore
        firebaseDB.collection("communities").document(tempCommunityId).collection("posts")
                .document(tempPostId).collection("comments").document(tempCommentIdx.getCommentId())
                .update("upvotes", tempCommentIdx.getUpvotes(),
                        "downvotes", tempCommentIdx.getDownvotes());

        // Sync local vote state to Firestore
        Map<String, Object> hashmapVoteData = new HashMap<>();
        // Set up adapter
        hashmapVoteData.put("type", isUpvote ? (tempCommentIdx.isUpvoted() ? "up" : "none") : (tempCommentIdx.isDownvoted() ? "down" : "none"));

        // Sync local vote state to Firestore
        firebaseDB.collection("users").document(tempUID)
                .collection("commentVotes").document(tempCommentIdx.getCommentId())
                .set(hashmapVoteData);
    }

    // Post comment
    private void postComment() {
        // Get comment text
        String tempCommentText = tempEtComment.getText().toString().trim();
        // tempCommentText checker
        if (TextUtils.isEmpty(tempCommentText)) return;

        // tempUserId
        String tempUserId = FirebaseAuth.getInstance().getUid();
        // tempUserId checker
        String tempUserName = FirebaseAuth.getInstance().getCurrentUser() != null ?
                         (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null ? 
                          FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : 
                          FirebaseAuth.getInstance().getCurrentUser().getEmail()) : "Anonymous";

        // Create tempComment
        Map<String, Object> tempComment = new HashMap<>();
        tempComment.put("text", tempCommentText);
        tempComment.put("userName", tempUserName);
        tempComment.put("timestamp", System.currentTimeMillis());
        tempComment.put("userId", tempUserId);
        tempComment.put("upvotes", 0);
        tempComment.put("downvotes", 0);

        // tempUID checker
        if (replyingTo != null) {
            tempComment.put("parentCommentId", replyingTo.getCommentId());
        }

        // Post comment
        firebaseDB.collection("communities").document(tempCommunityId).collection("posts")
                .document(tempPostId).collection("comments")
                .add(tempComment)
                .addOnSuccessListener(documentReference -> {
                    tempEtComment.setText("");
                    tempEtComment.setHint("Add a comment...");
                    replyingTo = null;
                    Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show();
                });
    }

    // Share post
    private void sharePost() {
        // tempCurrentPost is null checker
        if (tempCurrentPost == null) return;
        // Set up baseUrl
        String baseUrl = "http://localhost:8080/post/" + tempCurrentPost.getPostId();
        // Set up queryParams
        String queryParams = "?title=" + Uri.encode(tempCurrentPost.getTitle()) +
                "&content=" + Uri.encode(tempCurrentPost.getContent()) +
                "&author=" + Uri.encode(tempCurrentPost.getAuthorEmail() != null ? tempCurrentPost.getAuthorEmail() : "nexus") +
                "&upvotes=" + tempCurrentPost.getUpvotes() +
                "&comments=" + (tempCurrentPost.getCommentCount() > 0 ? tempCurrentPost.getCommentCount() : 0);
        // Set up postLink
        String postLink = baseUrl + queryParams;

        // Set up shareText
        String shareText = tempCurrentPost.getTitle() + "\n\n" + tempCurrentPost.getContent() + "\n\nRead more at: " + postLink;
        // Set up options
        String[] options = {"WhatsApp", "Messages", "Copy Link", "Open in Browser", "Other Apps"};

        // Show share dialog
        new MaterialAlertDialogBuilder(this)
                .setTitle("Share Post")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // WhatsApp
                            shareToWhatsApp(shareText);
                            break;
                        case 1: // Messages
                            shareToMessages(shareText);
                            break;
                        case 2: // Copy Link
                            copyToClipboard(postLink);
                            break;
                        case 3: // Open in Browser
                            openInBrowser(postLink);
                            break;
                        case 4: // Other
                            shareToOther(shareText);
                            break;
                    }
                })
                .show();
    }

    // shareToWhatsApp() method.
    private void shareToWhatsApp(String text) {
        // intent
        Intent tempIntent = new Intent(Intent.ACTION_SEND);
        // setType
        tempIntent.setType("text/plain");
        // setPackage
        tempIntent.setPackage("com.whatsapp");
        // putExtra
        tempIntent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            // startActivity
            startActivity(tempIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            // shareToOther
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            shareToOther(text);
        }
    }

    // shareToMessages() method.
    private void shareToMessages(String text) {
        Intent tempIntent = new Intent(Intent.ACTION_VIEW);
        tempIntent.setData(Uri.parse("sms:"));
        tempIntent.putExtra("sms_body", text);
        try {
            startActivity(tempIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Messaging app not found", Toast.LENGTH_SHORT).show();
            shareToOther(text);
        }
    }

    // copyToClipboard() method.
    private void copyToClipboard(String text) {
        // tempClipboard
        android.content.ClipboardManager tempClipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        // tempClip
        android.content.ClipData tempClip = android.content.ClipData.newPlainText("Post Content", text);
        tempClipboard.setPrimaryClip(tempClip);
        // Toast
        Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void openInBrowser(String url) {
        // tempIntent
        Intent tempIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        // startActivity
        startActivity(tempIntent);
    }

    // shareToOther() method.
    private void shareToOther(String text) {
        // Intent
        Intent tempIntent = new Intent(Intent.ACTION_SEND);
        // tempIntent
        tempIntent.setType("text/plain");
        // putExtra
        tempIntent.putExtra(Intent.EXTRA_TEXT, text);
        // startActivity
        startActivity(Intent.createChooser(tempIntent, "Share via"));
    }

    // Handle bookmark
    private void handleBookmark() {
        // tempCurrentPost checker
        if (tempCurrentPost == null) return;
        // tempUID checker
        String tempUID = FirebaseAuth.getInstance().getUid();
        // tempUID checker
        if (tempUID == null) return;

        // tempCurrentPost isBookmarked checker
        if (tempCurrentPost.isBookmarked()) {
            tempCurrentPost.setBookmarkCount(Math.max(0, tempCurrentPost.getBookmarkCount() - 1));
            tempCurrentPost.setBookmarked(false);
            firebaseDB.collection("communities").document(tempCommunityId)
                    .collection("posts").document(tempPostId)
                    .collection("bookmarks").document(tempUID).delete();
        } else {
            tempCurrentPost.setBookmarkCount(tempCurrentPost.getBookmarkCount() + 1);
            tempCurrentPost.setBookmarked(true);
            Map<String, Object> bookmark = new HashMap<>();
            bookmark.put("timestamp", System.currentTimeMillis());
            firebaseDB.collection("communities").document(tempCommunityId)
                    .collection("posts").document(tempPostId)
                    .collection("bookmarks").document(tempUID).set(bookmark);
        }

        // Update Firestore
        updateInteractionButtons();
        // Update Firestore
        firebaseDB.collection("communities").document(tempCommunityId)
                .collection("posts").document(tempPostId)
                .update("bookmarkCount", tempCurrentPost.getBookmarkCount());
    }

    // handleVote() method.
    private void handleVote(boolean isUpvote) {
        // tempCurrentPost checker
        if (tempCurrentPost == null) return;

        // tempUID checker
        String tempUID = FirebaseAuth.getInstance().getUid();
        if (tempUID == null) return;

        // tempCurrentPost isUpvoted checker
        if (isUpvote) {
            if (tempCurrentPost.isUpvoted()) {
                tempCurrentPost.setUpvotes(Math.max(0, tempCurrentPost.getUpvotes() - 1));
                tempCurrentPost.setUpvoted(false);
            } else {
                // Set up adapter
                tempCurrentPost.setUpvotes(tempCurrentPost.getUpvotes() + 1);
                tempCurrentPost.setUpvoted(true);
                if (tempCurrentPost.isDownvoted()) {
                    tempCurrentPost.setDownvotes(Math.max(0, tempCurrentPost.getDownvotes() - 1));
                    tempCurrentPost.setDownvoted(false);
                }
            }
        } else {
            // tempCurrentPost isDownvoted checker
            if (tempCurrentPost.isDownvoted()) {
                tempCurrentPost.setDownvotes(Math.max(0, tempCurrentPost.getDownvotes() - 1));
                tempCurrentPost.setDownvoted(false);
            } else {
                // Set up adapter
                tempCurrentPost.setDownvotes(tempCurrentPost.getDownvotes() + 1);
                tempCurrentPost.setDownvoted(true);
                if (tempCurrentPost.isUpvoted()) {
                    tempCurrentPost.setUpvotes(Math.max(0, tempCurrentPost.getUpvotes() - 1));
                    tempCurrentPost.setUpvoted(false);
                }
            }
        }

        // displayPost
        displayPost();
        // updateVotesInFirestore
        updateVotesInFirestore();
        
        // Sync local vote state to Firestore
        Map<String, Object> tempVoteData = new HashMap<>();
        // Set up adapter
        tempVoteData.put("type", isUpvote ? (tempCurrentPost.isUpvoted() ? "up" : "none") : (tempCurrentPost.isDownvoted() ? "down" : "none"));

        // Sync local vote state to Firestore
        firebaseDB.collection("users").document(tempUID)
                .collection("votes").document(tempCurrentPost.getPostId())
                .set(tempVoteData);
    }

    // updateVotesInFirestore() method.
    private void updateVotesInFirestore() {
        // communities
        firebaseDB.collection("communities")
                .document(tempCommunityId)
                .collection("posts")
                .document(tempPostId)
                .update(
                        "upvotes", tempCurrentPost.getUpvotes(),
                        "downvotes", tempCurrentPost.getDownvotes()
                );
    }
}