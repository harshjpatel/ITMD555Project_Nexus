// Name: Harsh Patel (A20369913)

package com.example.nexus;

import com.google.firebase.firestore.PropertyName;

// Community.java
public class Community {

    // name
    @PropertyName("name")
    public String name;

    // icon
    @PropertyName("icon")
    public String icon;

    // summary
    @PropertyName("summary")
    public String summary;

    // timestamp
    @PropertyName("timestamp")
    public long timestamp;

    // communityId
    public String communityId;
    // selected
    public boolean selected;

    // Constructor
    public Community() {}

    public String getCommunityId() {
        // Return the communityId
        return communityId;
    }
    public void setCommunityId(String communityId) {
        // Set the communityId
        this.communityId = communityId;
    }

    // Constructor
    public Community(String name, String icon) {
        // name
        this.name = name;
        // icon
        this.icon = icon;
        // selected
        this.selected = false;
    }

    // Getters and Setters
    @PropertyName("name")
    public String getName() {
        return name;
    }

    // Setters
    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    // Getters and Setters
    @PropertyName("icon")
    public String getIcon() {
        return icon;
    }

    // Setters
    @PropertyName("icon")
    public void setIcon(String icon) {
        this.icon = icon;
    }

    // Getters and Setters
    @PropertyName("summary")
    public String getSummary() { return summary; }

    // Setters
    @PropertyName("summary")
    public void setSummary(String summary) {
        this.summary = summary;
    }

    // Getters and Setters
    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public boolean isSelected() {
        return selected;
    }

    // Setters
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}