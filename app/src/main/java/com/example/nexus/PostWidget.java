package com.example.nexus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

// PostWidget class
public class PostWidget extends AppWidgetProvider {

    private static final String TAG = "PostWidget";
    private static final String ACTION_UPDATE_MANUAL = "com.example.nexus.UPDATE_WIDGET_MANUAL";
    private static final long REFRESH_INTERVAL_MS = 60000; // 1 minute to ensure system stability

    // Called when the widget is first created
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the update cycle
        startUpdateCycle(context);
    }

    // Called when the widget is updated
    @Override
    public void onReceive(Context context, Intent intent) {
        // Handle manual updates
        String action = intent.getAction();
        // Log the action for debugging
        if (ACTION_UPDATE_MANUAL.equals(action)) {
            // Log the action for debugging
            startUpdateCycle(context);
        } else {
            // Handle other actions if needed
            super.onReceive(context, intent);
        }
    }

    // Called when the widget is enabled
    private void startUpdateCycle(Context context) {
        // tempAppWidgetManager
        AppWidgetManager tempAppWidgetManager = AppWidgetManager.getInstance(context);
        // tempComponentName
        ComponentName tempComponentName = new ComponentName(context, PostWidget.class);
        // tempAppWidgetIds
        int[] tempAppWidgetIds = tempAppWidgetManager.getAppWidgetIds(tempComponentName);

        // Log the action for debugging
        if (tempAppWidgetIds.length > 0) {
            // fetchDataAndUpdate
            fetchDataAndUpdate(context, tempAppWidgetManager);
            // scheduleNextUpdate
            scheduleNextUpdate(context);
        }
    }

    // Helper method to create the base RemoteViews
    private static RemoteViews createBaseRemoteViews(Context context) {
        // Create the base RemoteViews
        RemoteViews tempViews = new RemoteViews(context.getPackageName(), R.layout.post_widget);

        // Set colors for icons
        int tempSecondaryColor = Color.parseColor("#A0AABF");
        int tempAccentColor = Color.parseColor("#6C7BFF");
        // Set the tempViews and set setColorFilter
        tempViews.setInt(R.id.widgetUpvoteIcon, "setColorFilter", tempSecondaryColor);
        tempViews.setInt(R.id.widgetDownvoteIcon, "setColorFilter", tempSecondaryColor);
        tempViews.setInt(R.id.widgetBookmarkIcon, "setColorFilter", tempSecondaryColor);
        tempViews.setInt(R.id.widgetCommentIcon, "setColorFilter", tempAccentColor);

        // Set the click action
        Intent tempIntent = new Intent(context, DashboardActivity.class);
        // Set the pending intent
        PendingIntent tempPendingIntent = PendingIntent.getActivity(context, 0, tempIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        // Set the click action
        tempViews.setOnClickPendingIntent(R.id.widgetRoot, tempPendingIntent);
        // Return the tempViews
        return tempViews;
    }

    // fetchDataAndUpdate
    private void fetchDataAndUpdate(Context context, AppWidgetManager appWidgetManager) {
        // Check if user is logged in
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        // If not logged in, update UI
        if (fbAuth.getCurrentUser() == null) {
            // updateUI
            updateUI(context, appWidgetManager, "Please log in", "");
            return;
        }

        // Fetch user data
        String tempUID = fbAuth.getCurrentUser().getUid();
        // FirebaseFirestore
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();

        // Fetch user profile
        firebaseDB.collection("users").document(tempUID).get().addOnSuccessListener(userDoc -> {
            if (!userDoc.exists()) {
                // updateUI
                updateUI(context, appWidgetManager, "User profile not found", "");
                return;
            }
            // Check if communities are selected
            @SuppressWarnings("unchecked")
            List<String> tempSelected = (List<String>) userDoc.get("selectedCommunities");
            // If not selected, update UI
            if (tempSelected == null || tempSelected.isEmpty()) {
                // updateUI
                updateUI(context, appWidgetManager, "No communities selected", "");
                return;
            }

            // Fetch latest post
            String tempCommunityId = tempSelected.get(0);
            // Fetch community name
            firebaseDB.collection("communities").document(tempCommunityId).get().addOnSuccessListener(commDoc -> {
                // If community doesn't exist, update UI
                String tempCommunityName = commDoc.getString("name");

                // Fetch latest post
                firebaseDB.collection("communities").document(tempCommunityId).collection("posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            // If no posts, update UI
                            RemoteViews tempViews = createBaseRemoteViews(context);
                            // Set community name
                            tempViews.setTextViewText(R.id.widgetCommunityName, tempCommunityName != null ? tempCommunityName.toUpperCase() : "COMMUNITY");

                            // If posts exist, update UI
                            if (!querySnapshot.isEmpty()) {
                                // Set post details
                                DocumentSnapshot tempDoc = querySnapshot.getDocuments().get(0);
                                // Title
                                tempViews.setTextViewText(R.id.widgetPostTitle, tempDoc.getString("title"));
                                // Content
                                tempViews.setTextViewText(R.id.widgetPostContent, tempDoc.getString("content"));
                                // Upvotes
                                tempViews.setTextViewText(R.id.widgetUpvoteCount, String.valueOf(tempDoc.getLong("upvotes") != null ? tempDoc.getLong("upvotes") : 0));
                                // Downvotes
                                tempViews.setTextViewText(R.id.widgetDownvoteCount, String.valueOf(tempDoc.getLong("downvotes") != null ? tempDoc.getLong("downvotes") : 0));
                                // Bookmark count
                                tempViews.setTextViewText(R.id.widgetBookmarkCount, String.valueOf(tempDoc.getLong("bookmarkCount") != null ? tempDoc.getLong("bookmarkCount") : 0));

                                // Fetch comment count separately as it might not be synced in the post document
                                tempDoc.getReference().collection("comments").get().addOnSuccessListener(commentSnapshot -> {
                                    // Set comment count
                                    tempViews.setTextViewText(R.id.widgetCommentCount, String.valueOf(commentSnapshot.size()));
                                    // Update widget
                                    appWidgetManager.updateAppWidget(new ComponentName(context, PostWidget.class), tempViews);
                                });
                            } else {
                                // Set default UI
                                tempViews.setTextViewText(R.id.widgetPostTitle, "No posts yet");
                                // Set default UI
                                tempViews.setTextViewText(R.id.widgetPostContent, "Be the first to post!");
                            }
                            // Update widget
                            appWidgetManager.updateAppWidget(new ComponentName(context, PostWidget.class), tempViews);
                        });
            });
        }).addOnFailureListener(e -> updateUI(context, appWidgetManager, "Network Error", ""));
    }

    // updateUI
    private void updateUI(Context context, AppWidgetManager manager, String title, String content) {
        // tempViews
        RemoteViews tempViews = createBaseRemoteViews(context);
        // setTextViewText title
        tempViews.setTextViewText(R.id.widgetPostTitle, title);
        // setTextViewText content
        tempViews.setTextViewText(R.id.widgetPostContent, content);
        // updateAppWidget
        manager.updateAppWidget(new ComponentName(context, PostWidget.class), tempViews);
    }

    // scheduleNextUpdate
    private void scheduleNextUpdate(Context context) {
        // intent
        Intent intent = new Intent(context, PostWidget.class);
        // intent
        intent.setAction(ACTION_UPDATE_MANUAL);
        // pendingIntent
        PendingIntent tempPendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // alarmManager
        AlarmManager tempAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (tempAlarmManager != null) {
            // nextUpdate
            long nextUpdate = System.currentTimeMillis() + REFRESH_INTERVAL_MS;
            // setAndAllowWhileIdle is safer than setExact for avoiding ANRs
            tempAlarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextUpdate, tempPendingIntent);
        }
    }

    // onDisabled() method.
    @Override
    public void onDisabled(Context context) {
        // Cancel the alarm
        super.onDisabled(context);
        // Cancel the alarm
        Intent intent = new Intent(context, PostWidget.class);
        // ACTION_UPDATE_MANUAL
        intent.setAction(ACTION_UPDATE_MANUAL);
        // pendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent != null) {
            // alarmManager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // cancel
            alarmManager.cancel(pendingIntent);
            // pendingIntent.cancel();
            pendingIntent.cancel();
        }
    }
}