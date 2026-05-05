package com.example.nexus;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// CommentAdapter.java
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> tempAllComments;
    private OnCommentActionListener tempActionListener;

    // Interface for handling comment actions
    public interface OnCommentActionListener {
        // Called when a comment is replied to
        void onReply(Comment parentComment);
        // Called when a comment is upvoted or downvoted
        void onVote(Comment comment, boolean isUpvote);
    }

    // Constructor
    public CommentAdapter(List<Comment> commentList, OnCommentActionListener listener) {
        // Initialize the adapter with the provided data
        this.tempAllComments = commentList;
        // Initialize the listener
        this.tempActionListener = listener;
    }

    // ViewHolder
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View tempView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        // Create a new ViewHolder with the inflated view
        return new CommentViewHolder(tempView);
    }

    // Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder tempLikeVoteHolder, int position) {
        // Get the comment at the current position
        Comment tempComment = tempAllComments.get(position);
        
        // Set padding based on parent-child relationship
        int paddingStart = tempComment.getParentCommentId() != null ? 32 : 0;
        // Set padding
        tempLikeVoteHolder.itemView.setPadding(paddingStart, tempLikeVoteHolder.itemView.getPaddingTop(),
                                 tempLikeVoteHolder.itemView.getPaddingRight(), tempLikeVoteHolder.itemView.getPaddingBottom());

        // Set the text for each view in the ViewHolder
        tempLikeVoteHolder.tvCommentUser.setText(tempComment.getUserName() != null ? tempComment.getUserName() : "Anonymous");
        // Set the text for each view in the ViewHolder
        tempLikeVoteHolder.tvCommentText.setText(tempComment.getText());

        // Set the time for each view in the ViewHolder
        if (tempComment.getTimestamp() > 0) {
            // Set the time for each view in the ViewHolder
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    // Set the time for each view in the ViewHolder
                    tempComment.getTimestamp(),
                    // Set the time for each view in the ViewHolder
                    System.currentTimeMillis(),
                    // Set the time for each view in the ViewHolder
                    DateUtils.MINUTE_IN_MILLIS);
            // Set the time for each view in the ViewHolder
            tempLikeVoteHolder.tvCommentTime.setText(timeAgo);
        } else {
            tempLikeVoteHolder.tvCommentTime.setText("");
        }

        // Set the vote counts for each view in the ViewHolder
        tempLikeVoteHolder.tvCommentUpvotes.setText(String.valueOf(tempComment.getUpvotes()));
        tempLikeVoteHolder.tvCommentDownvotes.setText(String.valueOf(tempComment.getDownvotes()));

        // Update vote icons UI
        if (tempComment.isUpvoted()) {
            // Update ivCommentUpvote icons UI
            tempLikeVoteHolder.ivCommentUpvote.setColorFilter(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.upvote_green));
            // Update tvCommentUpvotes text color
            tempLikeVoteHolder.tvCommentUpvotes.setTextColor(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.upvote_green));
            // Update ivCommentDownvote icons UI
            tempLikeVoteHolder.ivCommentDownvote.clearColorFilter();
            // Update tvCommentDownvotes text color
            tempLikeVoteHolder.tvCommentDownvotes.setTextColor(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.subtitleText));
        } else if (tempComment.isDownvoted()) {
            tempLikeVoteHolder.ivCommentDownvote.setColorFilter(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.downvote_red));
            tempLikeVoteHolder.tvCommentDownvotes.setTextColor(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.downvote_red));
            tempLikeVoteHolder.ivCommentUpvote.clearColorFilter();
            tempLikeVoteHolder.tvCommentUpvotes.setTextColor(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.subtitleText));
        } else {
            tempLikeVoteHolder.ivCommentUpvote.clearColorFilter();
            tempLikeVoteHolder.ivCommentDownvote.clearColorFilter();
            tempLikeVoteHolder.tvCommentUpvotes.setTextColor(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.subtitleText));
            tempLikeVoteHolder.tvCommentDownvotes.setTextColor(ContextCompat.getColor(tempLikeVoteHolder.itemView.getContext(), R.color.subtitleText));
        }

        tempLikeVoteHolder.ivCommentUpvote.setOnClickListener(v -> {
            // Handle upvote
            tempActionListener.onVote(tempComment, true);
            // Update the UI
            notifyItemChanged(tempLikeVoteHolder.getAdapterPosition());
        });
        tempLikeVoteHolder.ivCommentDownvote.setOnClickListener(v -> {
            // Handle downvote
            tempActionListener.onVote(tempComment, false);
            // Update the UI
            notifyItemChanged(tempLikeVoteHolder.getAdapterPosition());
        });
        tempLikeVoteHolder.tvCommentReply.setOnClickListener(v -> tempActionListener.onReply(tempComment));

        // Toggle visibility of children (simplified)
        tempLikeVoteHolder.itemView.setOnClickListener(v -> {
            // Toggle the expanded state
            tempComment.setExpanded(!tempComment.isExpanded());
            // Update the UI
            notifyDataSetChanged();
        });

        // Hide if parent is collapsed (basic logic)
        if (shouldHide(tempComment)) {
            // Hide the view
            tempLikeVoteHolder.itemView.setVisibility(View.GONE);
            // Set the layout params to 0
            tempLikeVoteHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            // Show the view
            tempLikeVoteHolder.itemView.setVisibility(View.VISIBLE);
            // Set the layout params to match parent
            tempLikeVoteHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private boolean shouldHide(Comment comment) {
        // Check if the comment is not expanded
        String tempParentId = comment.getParentCommentId();
        // If not expanded, return true
        while (tempParentId != null) {
            // Find the parent comment
            for (Comment tempCommentIdx : tempAllComments) {
                // If found, check if it's expanded
                if (tempCommentIdx.getCommentId().equals(tempParentId)) {
                    // If not expanded, return true
                    if (!tempCommentIdx.isExpanded()) return true;
                    // Update tempParentId
                    tempParentId = tempCommentIdx.getParentCommentId();
                    // Break the loop
                    break;
                }
            }
            // If not found, break the loop
            if (tempParentId == null) break;
        }
        // Return false
        return false;
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        return tempAllComments.size();
    }

    // ViewHolder
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentUser, tvCommentText, tvCommentTime, tvCommentUpvotes, tvCommentDownvotes, tvCommentReply;
        ImageView ivCommentUpvote, ivCommentDownvote;

        // Constructor
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentUpvotes = itemView.findViewById(R.id.tvCommentUpvotes);
            tvCommentDownvotes = itemView.findViewById(R.id.tvCommentDownvotes);
            tvCommentReply = itemView.findViewById(R.id.tvCommentReply);
            ivCommentUpvote = itemView.findViewById(R.id.ivCommentUpvote);
            ivCommentDownvote = itemView.findViewById(R.id.ivCommentDownvote);
        }
    }
}