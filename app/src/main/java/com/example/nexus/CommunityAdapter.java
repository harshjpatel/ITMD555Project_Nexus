package com.example.nexus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// CommunityAdapter
public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

    // Context, List<Community>, OnCommunitySelectedListener, Set
    Context tempContext;
    List<Community> tempCommunityList;
    List<Community> tempCommunityListFull;
    OnCommunitySelectedListener tempListener;
    Set<String> tempSelectedCommunities = new HashSet<>();

    // Interface for handling community selection
    public interface OnCommunitySelectedListener {
        // Called when the selection count changes
        void onSelectionChanged(int count);
    }

    // Constructor
    public CommunityAdapter(Context context, List<Community> communityList, OnCommunitySelectedListener listener) {
        // tempContext
        this.tempContext = context;
        // tempCommunityList
        this.tempCommunityList = communityList;
        // tempCommunityListFull
        this.tempCommunityListFull = new ArrayList<>(communityList);
        // tempListener
        this.tempListener = listener;
    }

    // ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View tempView = LayoutInflater.from(tempContext)
                // Inflate the layout for each item in the RecyclerView
                .inflate(R.layout.community_item, parent, false);
        // Create a new ViewHolder with the inflated view
        return new ViewHolder(tempView);
    }

    // Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the community at the current position
        Community tempCommunity = tempCommunityList.get(position);
        // Set the text for each view in the ViewHolder
        holder.name.setText(tempCommunity.getName());

        // Set the icon for each view in the ViewHolder
        int iconRes = 0;
        // Set the icon for each view in the ViewHolder
        if (tempCommunity.getIcon() != null && !tempCommunity.getIcon().isEmpty()) {
            // Set the icon for each view in the ViewHolder
            iconRes = tempContext.getResources().getIdentifier(
                    // Set the icon for each view in the ViewHolder
                    tempCommunity.getIcon(),
                    // Set the icon for each view in the ViewHolder
                    "drawable",
                    // Set the icon for each view in the ViewHolder
                    tempContext.getPackageName()
            );
        }

        // Set the icon for each view in the ViewHolder
        if (iconRes != 0) {
            // Set the icon for each view in the ViewHolder
            holder.icon.setImageResource(iconRes);
        } else {
            // Set the icon for each view in the ViewHolder
            holder.icon.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // highlight selected
        if (tempSelectedCommunities.contains(tempCommunity.getCommunityId())) {
            // highlight color.
            holder.cardView.setStrokeColor(android.graphics.Color.parseColor("#4CAF50"));
            // highlight width
            holder.cardView.setStrokeWidth(4);
            // highlight visibility
            holder.checkMark.setVisibility(View.VISIBLE);
        } else {
            // highlight color.
            holder.cardView.setStrokeColor(android.graphics.Color.parseColor("#E6E8F0"));
            // highlight width
            holder.cardView.setStrokeWidth(2);
            // highlight visibility
            holder.checkMark.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            // toggle selected
            String tempId = tempCommunity.getCommunityId();
            // tempSelectedCommunities
            if (tempSelectedCommunities.contains(tempId)) {
                // tempID
                tempSelectedCommunities.remove(tempId);
                // toggle selected
            } else {
                tempSelectedCommunities.add(tempId);
            }

            // update UI
            notifyItemChanged(position);

            // update count
            if (tempListener != null) {
                // update count
                tempListener.onSelectionChanged(tempSelectedCommunities.size());
            }
        });
    }

    // Get the number of items in the list
    @Override
    public int getItemCount() {
        return tempCommunityList.size();
    }

    // Get the selected communities
    public Set<String> getTempSelectedCommunities() {
        return tempSelectedCommunities;
    }

    // Set the selected communities
    public void setTempSelectedCommunities(List<String> selectedList) {
        // Clear the list
        if (selectedList != null) {
            // Clear the list
            this.tempSelectedCommunities = new HashSet<>(selectedList);
            // Notify the adapter
            notifyDataSetChanged();
            // Update the count
            updateCount();
        }
    }

    // Update the count
    private void updateCount() {
        // Update the count
        if (tempListener != null) {
            // Update the count
            int count = tempSelectedCommunities != null ? tempSelectedCommunities.size() : 0;
            // Update the count
            tempListener.onSelectionChanged(count);
        }
    }

    // Filter the list of communities
    public void filter(String text) {
        // Filter the list
        List<Community> tempFilteredList = new ArrayList<>();
        // If no text, show all communities
        if (text == null || text.isEmpty()) {
            // Add all communities to the list
            tempFilteredList.addAll(tempCommunityListFull);
        } else {
            // Filter the list
            String tempQuery = text.toLowerCase().trim();
            // Loop through the list
            for (Community item : tempCommunityListFull) {
                // if filterPattern
                if (item.getName() != null && item.getName().toLowerCase().contains(tempQuery)) {
                    // add into tempFilteredList
                    tempFilteredList.add(item);
                }
            }
        }
        // Update the list
        tempCommunityList.clear();
        // Update the list
        tempCommunityList.addAll(tempFilteredList);
        // Notify the adapter
        notifyDataSetChanged();
        // Keep count in sync after filter
        updateCount();
    }

    public void updateList(List<Community> newList) {
        // Update the list
        this.tempCommunityListFull = new ArrayList<>(newList);
        // Update the list with clear
        this.tempCommunityList.clear();
        // add all
        this.tempCommunityList.addAll(newList);
        // Notify the adapter
        notifyDataSetChanged();
        // Keep count in sync after filter
        updateCount();
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView icon;
        com.google.android.material.card.MaterialCardView cardView;
        ImageView checkMark;

        // Constructor ViewHolder
        public ViewHolder(View itemView) {
            // Constructor ViewHolder
            super(itemView);
            // Initialize name
            name = itemView.findViewById(R.id.communityName);
            // Initialize icon
            icon = itemView.findViewById(R.id.communityIcon);
            // Initialize cardView
            cardView = itemView.findViewById(R.id.cardView);
            // Initialize checkMark
            checkMark = itemView.findViewById(R.id.checkMark);
        }
    }
}