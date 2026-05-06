// Name: Harsh Patel (A20369913)

package com.example.nexus;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// WidgetUpdateWorker.java
public class WidgetUpdateWorker extends Worker {

    // Constructor
    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        // Call super
        super(context, workerParams);
    }

    // Override doWork
    @NonNull
    @Override
    public Result doWork() {
        // Log the action for debugging
        Log.d("WidgetUpdateWorker", "Performing periodic widget update");

        // Create an intent to update the widget
        Context tempContext = getApplicationContext();
        // ACTION_UPDATE_MANUAL
        Intent tempIntent = new Intent(tempContext, PostWidget.class);
        // ACTION_UPDATE_MANUAL
        tempIntent.setAction("com.example.nexus.UPDATE_WIDGET_MANUAL");

        // Update the widget
        AppWidgetManager tempAppWidgetManager = AppWidgetManager.getInstance(tempContext);
        // Get the widget IDs
        int[] tempIDs = tempAppWidgetManager.getAppWidgetIds(new ComponentName(tempContext, PostWidget.class));
        // Set the widget IDs
        tempIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, tempIDs);
        // Send the broadcast
        tempContext.sendBroadcast(tempIntent);
        // Return success
        return Result.success();
    }
}