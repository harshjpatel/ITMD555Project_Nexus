package com.example.nexus;

import com.google.firebase.firestore.Exclude;

// Comment.java
public class Comment {
    private String userName;
    private String userId;
    private String text;
    private long timestamp;
    private String userAvatarUrl;

    private int upvotes = 0;
    private int downvotes = 0;
    private String parentCommentId; // null if top-level
    private boolean isExpanded = true;
    private String commentId;
    private boolean isUpvoted = false;
    private boolean isDownvoted = false;

    // Required for Firebase
    public Comment() {
        // Required for Firebase
    }

    // Constructor for top-level comments
    public Comment(String userName, String userId, String text, long timestamp) {
        this.userName = userName;
        this.userId = userId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getter-setter
    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
    
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    
    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
    
    public int getDownvotes() { return downvotes; }
    public void setDownvotes(int downvotes) { this.downvotes = downvotes; }
    
    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }
    
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    @Exclude
    public boolean isUpvoted() { return isUpvoted; }
    @Exclude
    public void setUpvoted(boolean upvoted) { isUpvoted = upvoted; }

    @Exclude
    public boolean isDownvoted() { return isDownvoted; }
    @Exclude
    public void setDownvoted(boolean downvoted) { isDownvoted = downvoted; }
}