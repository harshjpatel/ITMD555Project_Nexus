package com.example.nexus;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class Post implements Serializable {

    private String postId;
    private String communityId;

    private String title;
    private String content;
    private String imageUrl;

    private int upvotes = 0;
    private int downvotes = 0;
    private int commentCount = 0;
    private int bookmarkCount = 0;

    private long timestamp = 0;
    private long lastUpdated = 0;

    private String authorId;
    private String authorName;
    private String authorEmail;

    private boolean upvoted = false;
    private boolean downvoted = false;
    private boolean isBookmarked = false;

    public Post() {}

    // Getters
    public String getPostId() { return postId; }
    public String getCommunityId() { return communityId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getUpvotes() { return upvotes; }
    public int getDownvotes() { return downvotes; }
    public int getCommentCount() { return commentCount; }
    public int getBookmarkCount() { return bookmarkCount; }
    public long getTimestamp() { return timestamp; }
    public long getLastUpdated() { return lastUpdated; }
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorEmail() { return authorEmail; }
    public boolean isUpvoted() { return upvoted; }
    public boolean isDownvoted() { return downvoted; }
    public boolean isBookmarked() { return isBookmarked; }

    // Setters with String-to-Number safety (Handles Firestore string numbers)
    public void setPostId(String postId) { this.postId = postId; }
    public void setCommunityId(String communityId) { this.communityId = communityId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUpvoted(boolean upvoted) { this.upvoted = upvoted; }
    public void setDownvoted(boolean downvoted) { this.downvoted = downvoted; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    @PropertyName("upvotes")
    public void setUpvotes(Object upvotes) {
        this.upvotes = convertToInt(upvotes);
    }

    @PropertyName("downvotes")
    public void setDownvotes(Object downvotes) {
        this.downvotes = convertToInt(downvotes);
    }

    @PropertyName("commentCount")
    public void setCommentCount(Object commentCount) {
        this.commentCount = convertToInt(commentCount);
    }

    @PropertyName("bookmarkCount")
    public void setBookmarkCount(Object bookmarkCount) {
        this.bookmarkCount = convertToInt(bookmarkCount);
    }

    @PropertyName("timestamp")
    public void setTimestamp(Object timestamp) {
        this.timestamp = convertToLong(timestamp);
    }

    @PropertyName("lastUpdated")
    public void setLastUpdated(Object lastUpdated) {
        this.lastUpdated = convertToLong(lastUpdated);
    }

    // Setters with String-to-String safety setAuthorId
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    // setAuthorName
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    // setAuthorEmail
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    // Helper conversion methods
    public com.google.firebase.firestore.DocumentReference getReference(com.google.firebase.firestore.FirebaseFirestore db) {
        // If postId is null, return null
        return db.collection("communities").document(communityId).collection("posts").document(postId);
    }

    // Helper conversion methods
    private int convertToInt(Object val) {
        // If val is null, return 0
        if (val == null) return 0;
        // If val is a Number, return its intValue
        if (val instanceof Number) return ((Number) val).intValue();
        // If val is a String, try to parse it as an integer
        if (val instanceof String) {
            // If parsing fails, return 0
            try { return Integer.parseInt((String) val); } catch (Exception e) { return 0; }
        }
        // If none of the above conditions are met, return 0
        return 0;
    }

    // Helper conversion methods
    private long convertToLong(Object val) {
        // If val is null, return 0L
        if (val == null) return 0L;
        // If val is a Number, return its longValue
        if (val instanceof Number) return ((Number) val).longValue();
        // If val is a String, try to parse it as a long
        if (val instanceof String) {
            // If parsing fails, return 0L
            try { return Long.parseLong((String) val); } catch (Exception e) { return 0L; }
        }
        // If none of the above conditions are met, return 0L
        return 0L;
    }
}