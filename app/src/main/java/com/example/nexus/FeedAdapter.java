// Name: Harsh Patel (A20369913)

package com.example.nexus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    Context tempContext;
    List<Post> tempList;
    List<Post> tempListFull;
    FirebaseFirestore firebaseDB;
    private String tempCurrentUserName, tempCurrentUserEmail;
    private boolean tempShowingBookmarksMode;

    // Constructor FeedAdapter
    public FeedAdapter(Context context, List<Post> list) {
        this.tempContext = context;
        this.tempList = list;
        this.tempListFull = new ArrayList<>(list);
        this.firebaseDB = FirebaseFirestore.getInstance();
    }

    // ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View tempView = LayoutInflater.from(tempContext).inflate(R.layout.item_post, parent, false);
        // Create a new ViewHolder with the inflated view
        return new ViewHolder(tempView);
    }

    // Bind data to the ViewHolder
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the post at the current position
        Post post = tempList.get(position);
        // Set the text for each view in the ViewHolder
        holder.title.setText(post.getTitle());
        // Set the text for each view in the ViewHolder
        holder.content.setText(post.getContent());
        // Set the text for each view in the ViewHolder
        holder.communityTag.setText(post.getAuthorName() != null ? post.getAuthorName() : "nexus");
        // Set the text for each view in the ViewHolder
        holder.upvoteBtn.setText(String.valueOf(post.getUpvotes()));
        // Set the text for each view in the ViewHolder
        holder.downvoteBtn.setText(String.valueOf(post.getDownvotes()));
        // Set the text for each view in the ViewHolder
        holder.commentCount.setText(String.valueOf(post.getCommentCount()));
        // Set the text for each view in the ViewHolder
        holder.bookmarkBtn.setText(String.valueOf(post.getBookmarkCount()));

        // Set the timestamp
        if (post.getTimestamp() > 0) {
            // Set the timestamp
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US);
            // Set the timestamp
            holder.timestamp.setText(sdf.format(new java.util.Date(post.getTimestamp())));
        }

        // UI State for UP Votes
        if (post.isUpvoted()) {
            // Set the icon and text color for the upvote button
            holder.upvoteBtn.setIconTintResource(R.color.upvote_green);
            // Set the icon and text color for the upvote button
            holder.upvoteBtn.setTextColor(ContextCompat.getColor(tempContext, R.color.upvote_green));
        } else {
            // Set the icon and text color for the upvote button
            holder.upvoteBtn.setIconTintResource(R.color.subtitleText);
            // Set the icon and text color for the upvote button
            holder.upvoteBtn.setTextColor(ContextCompat.getColor(tempContext, R.color.subtitleText));
        }

        // UI State for down Votes
        if (post.isDownvoted()) {
            // Set the icon and text icon for the downvote button
            holder.downvoteBtn.setIconTintResource(R.color.downvote_red);
            // Set the icon and text color for the downvote button
            holder.downvoteBtn.setTextColor(ContextCompat.getColor(tempContext, R.color.downvote_red));
        } else {
            // Set the icon and text icon for the downvote button
            holder.downvoteBtn.setIconTintResource(R.color.subtitleText);
            // Set the icon and text color for the downvote button
            holder.downvoteBtn.setTextColor(ContextCompat.getColor(tempContext, R.color.subtitleText));
        }

        // UI State for Bookmarks
        if (post.isBookmarked()) {
            // Set the icon and text icon for the bookmark button
            holder.bookmarkBtn.setIconResource(R.drawable.ic_bookmark);
            // Set the icon and text icon tint for the bookmark button
            holder.bookmarkBtn.setIconTintResource(R.color.orange);
            // Set the icon and text color for the bookmark button
            holder.bookmarkBtn.setTextColor(ContextCompat.getColor(tempContext, R.color.orange));
        } else {
            // Set the icon and text icon for the bookmark button
            holder.bookmarkBtn.setIconResource(R.drawable.ic_bookmark_border);
            // Set the icon and text icon tint for the bookmark button
            holder.bookmarkBtn.setIconTintResource(R.color.subtitleText);
            // Set the icon and text color for the bookmark button
            holder.bookmarkBtn.setTextColor(ContextCompat.getColor(tempContext, R.color.subtitleText));
        }

        // UPVOTE
        holder.upvoteBtn.setOnClickListener(v -> handleVote(post, true));

        // DOWNVOTE
        holder.downvoteBtn.setOnClickListener(v -> handleVote(post, false));

        // BOOKMARK
        holder.bookmarkBtn.setOnClickListener(v -> handleBookmark(post));

        // SHARE
        holder.shareBtn.setOnClickListener(v -> {
            // showShareOptions
            showShareOptions(post);
        });

        // COMMENT / DETAIL
        View.OnClickListener openDetail = v -> {
            // Create an intent to open the PostDetailActivity
            Intent intent = new Intent(tempContext, PostDetailActivity.class);
            // Pass the post data to the PostDetailActivity
            intent.putExtra("POST_DATA", post);
            // Start the PostDetailActivity
            tempContext.startActivity(intent);
        };
        // Set the click listener for the comment count
        holder.commentCount.setOnClickListener(openDetail);
        // Set the click listener for the community tag
        holder.itemView.setOnClickListener(openDetail);
    }

    // Handle Upvote/Downvote
    private void handleVote(Post post, boolean isUpvote) {
        // Get the user ID
        String uid = FirebaseAuth.getInstance().getUid();
        // If no user, return
        if (uid == null) return;

        // Update the upVote count
        if (isUpvote) {
            // Update the upVote count
            if (post.isUpvoted()) {
                post.setUpvotes(Math.max(0, post.getUpvotes() - 1));
                post.setUpvoted(false);
            } else {
                // Update the upVote count
                post.setUpvotes(post.getUpvotes() + 1);
                post.setUpvoted(true);
                if (post.isDownvoted()) {
                    post.setDownvotes(Math.max(0, post.getDownvotes() - 1));
                    post.setDownvoted(false);
                }
            }
        } else {
            // Update the downVote count
            if (post.isDownvoted()) {
                post.setDownvotes(Math.max(0, post.getDownvotes() - 1));
                post.setDownvoted(false);
            } else {
                // Update the downVote count
                post.setDownvotes(post.getDownvotes() + 1);
                post.setDownvoted(true);
                if (post.isUpvoted()) {
                    post.setUpvotes(Math.max(0, post.getUpvotes() - 1));
                    post.setUpvoted(false);
                }
            }
        }

        // Update the UI
        notifyDataSetChanged();

        // Update Firestore
        firebaseDB.collection("communities").document(post.getCommunityId())
                // Update the upVote count
                .collection("posts").document(post.getPostId())
                // Update the upVote count
                .update(
                        "upvotes", post.getUpvotes(),
                        "downvotes", post.getDownvotes()
                );

        // Update Firestore
        Map<String, Object> voteData = new HashMap<>();
        // Update Firestore
        voteData.put("type", isUpvote ? (post.isUpvoted() ? "up" : "none") : (post.isDownvoted() ? "down" : "none"));

        // Update Firestore
        firebaseDB.collection("users").document(uid)
                // Update Firestore
                .collection("votes").document(post.getPostId())
                .set(voteData);
    }

    // Handle Bookmark
    private void handleBookmark(Post tempPost) {
        // Get the user ID
        String tempUID = FirebaseAuth.getInstance().getUid();
        // If no user, return
        if (tempUID == null) return;

        // Update the bookmark count
        if (tempPost.isBookmarked()) {
            // Update the bookmark count
            tempPost.setBookmarkCount(Math.max(0, tempPost.getBookmarkCount() - 1));
            // Update the bookmark count
            tempPost.setBookmarked(false);

            // If we are in Bookmarks Mode, remove the item from the list immediately
            if (tempShowingBookmarksMode) {
                // Remove the item from the list
                int position = tempList.indexOf(tempPost);
                // Remove the item from the list
                if (position != -1) {
                    // Remove the item from the list
                    tempList.remove(position);
                    // Notify the adapter that the item has been removed
                    notifyItemRemoved(position);
                    
                    // Also remove from listFull to ensure it doesn't reappear
                    tempListFull.remove(tempPost);
                }
            } else {
                // Update the UI
                notifyDataSetChanged();
            }

            // Update Firestore
            firebaseDB.collection("communities").document(tempPost.getCommunityId())
                    // Update the post count
                    .collection("posts").document(tempPost.getPostId())
                    // Update the bookmark count
                    .collection("bookmarks").document(tempUID).delete();
        } else {
            // Update the bookmark count
            tempPost.setBookmarkCount(tempPost.getBookmarkCount() + 1);
            // Update the bookmark count
            tempPost.setBookmarked(true);
            // Update Firestore
            Map<String, Object> bookmark = new HashMap<>();
            // Update Firestore
            bookmark.put("timestamp", System.currentTimeMillis());
            // Update Firestore
            firebaseDB.collection("communities").document(tempPost.getCommunityId())
                    // Update the post count
                    .collection("posts").document(tempPost.getPostId())
                    // Update the bookmark count
                    .collection("bookmarks").document(tempUID).set(bookmark);
            // Update the UI
            notifyDataSetChanged();
        }

        // Update Firestore
        firebaseDB.collection("communities").document(tempPost.getCommunityId())
                // Update the post count
                .collection("posts").document(tempPost.getPostId())
                // Update the bookmark count
                .update("bookmarkCount", tempPost.getBookmarkCount());
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        return tempList.size();
    }

    // show share options
    private void showShareOptions(Post post) {
        // Create a share link
        String baseUrl = "http://localhost:8080/post/" + post.getPostId();
        // queryParams link
        String queryParams = "?title=" + Uri.encode(post.getTitle()) +
                "&content=" + Uri.encode(post.getContent()) +
                "&author=" + Uri.encode(post.getAuthorName() != null ? post.getAuthorName() : "nexus") +
                "&upvotes=" + post.getUpvotes() +
                "&comments=" + post.getCommentCount();
        // postLink
        String postLink = baseUrl + queryParams;

        // shareText
        String shareText = post.getTitle() + "\n\n" + post.getContent() + "\n\nRead more at: " + postLink;
        // available options to share.
        String[] options = {"WhatsApp", "Messages", "Copy Link", "Open in Browser", "Other Apps"};

        // new MaterialAlertDialogBuilder
        new MaterialAlertDialogBuilder(tempContext)
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

    // shareToWhatsApp
    private void shareToWhatsApp(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            tempContext.startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle the exception
            Toast.makeText(tempContext, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            shareToOther(text);
        }
    }

    // shareToMessages
    private void shareToMessages(String text) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:"));
        intent.putExtra("sms_body", text);
        try {
            tempContext.startActivity(intent);
        } catch (Exception e) {
            // Handle the exception
            Toast.makeText(tempContext, "Messaging app not found", Toast.LENGTH_SHORT).show();
            shareToOther(text);
        }
    }

    // copyToClipboard
    private void copyToClipboard(String text) {
        // Create a new clipboard manager
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) tempContext.getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        // Create a new clip
        android.content.ClipData clip = android.content.ClipData.newPlainText("Post Content", text);
        // Set the clip
        clipboard.setPrimaryClip(clip);
        Toast.makeText(tempContext, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // openInBrowser on chrome
    private void openInBrowser(String url) {
        // Create a new intent to open a URL
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        // Start the intent
        tempContext.startActivity(intent);
    }

    // shareToOther
    private void shareToOther(String text) {
        // Create a new intent to share text
        Intent intent = new Intent(Intent.ACTION_SEND);
        // Set the type of the intent
        intent.setType("text/plain");
        // Set the text to share
        intent.putExtra(Intent.EXTRA_TEXT, text);
        // Start the intent
        tempContext.startActivity(Intent.createChooser(intent, "Share via"));
    }

    // Update the list of posts
    public void updateList(List<Post> newList) {
        // Update the list
        this.tempList = newList;
        // Update the listFull
        this.tempListFull = new ArrayList<>(newList);
        // Notify the adapter
        notifyDataSetChanged();
    }

    // Filter the list of posts
    public void filter(String text) {
        // Clear the list
        tempList.clear();
        // If no text, show all posts
        if (text.isEmpty()) {
            // Add all posts to the list
            tempList.addAll(tempListFull);
        } else {
            // Filter the list
            String filterPattern = text.toLowerCase().trim();
            // Loop through the list
            for (Post post : tempListFull) {
                //  if filterPattern
                if (post.getTitle().toLowerCase().contains(filterPattern) ||
                        post.getContent().toLowerCase().contains(filterPattern)) {
                    // add into tempList
                    tempList.add(post);
                }
            }
        }
        // Notify the adapter
        notifyDataSetChanged();
    }

    // Set the current user
    public void setCurrentUser(String name, String email) {
        // Set the current userName
        this.tempCurrentUserName = name;
        // Set the current user email
        this.tempCurrentUserEmail = email;
    }

    // Get the current user
    public void setTempShowingBookmarksMode(boolean mode) {
        this.tempShowingBookmarksMode = mode;
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, communityTag, timestamp;
        MaterialButton upvoteBtn, downvoteBtn, commentCount, shareBtn, bookmarkBtn;

        // Constructor ViewHolder
        public ViewHolder(@NonNull View itemView) {
            // Constructor ViewHolder
            super(itemView);
            // Initialize title
            title = itemView.findViewById(R.id.postTitle);
            // Initialize content
            content = itemView.findViewById(R.id.postContent);
            // Initialize communityTag
            communityTag = itemView.findViewById(R.id.postTag);
            // Initialize timestamp
            timestamp = itemView.findViewById(R.id.timeText);
            // Initialize upvoteBtn
            upvoteBtn = itemView.findViewById(R.id.upvoteBtn);
            // Initialize downvoteBtn
            downvoteBtn = itemView.findViewById(R.id.downvoteBtn);
            // Initialize commentCount
            commentCount = itemView.findViewById(R.id.commentBtn);
            // Initialize shareBtn
            shareBtn = itemView.findViewById(R.id.shareBtn);
            // Initialize bookmarkBtn
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
        }
    }
}
