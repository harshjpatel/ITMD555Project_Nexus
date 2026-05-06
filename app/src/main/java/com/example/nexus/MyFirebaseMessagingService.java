package com.example.nexus;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// WidgetUpdateWorker
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Tag for logging
    private static final String TAG = "NexusFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Handle incoming messages
        super.onMessageReceived(remoteMessage);
        // Log the message
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            
            // Trigger widget update
            updateWidget();
        }

        // Check if message contains notification payload
        if (remoteMessage.getNotification() != null) {
            // Handle notification payload
            String notificationBody = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + notificationBody);

            // Show toast for foreground presentation
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                android.widget.Toast.makeText(getApplicationContext(),
                        "FCM Notification: " + notificationBody,
                        android.widget.Toast.LENGTH_LONG).show();
            });
        }
    }

    private void updateWidget() {
        // Create an intent to update the widget
        androidx.work.OneTimeWorkRequest tempUpdateRequest =
                // Create a new one-time work request
                new androidx.work.OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
                        // Set the input data
                        .build();

        // Use enqueueUniqueWork to prevent multiple simultaneous updates
        androidx.work.WorkManager.getInstance(this).enqueueUniqueWork(
                // Unique name for the work
                "widget_update",
                // REPLACE means if there's an existing work with the same name, it will be replaced
                androidx.work.ExistingWorkPolicy.REPLACE,
                // The work request
                tempUpdateRequest
        );
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Handle new token
        super.onNewToken(token);
        // Log the token
        Log.d(TAG, "Refreshed token: " + token);
        // You might want to send this token to your Firestore user document 
        // if you want to target specific users later.
    }
}