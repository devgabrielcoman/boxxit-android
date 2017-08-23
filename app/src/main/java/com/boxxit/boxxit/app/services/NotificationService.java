package com.boxxit.boxxit.app.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.boxxit.boxxit.R;
import com.boxxit.boxxit.app.activities.explore.ExploreActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void handleIntent(Intent intent) {

        Bundle bundle = intent.getExtras();
        if (bundle != null) {

            String friendId = bundle.getString("friendId");
            String title = bundle.getString("gcm.notification.body");

            Context context = getApplicationContext();

            Intent myIntent = new Intent(context, ExploreActivity.class);
            myIntent.putExtra("profile", friendId);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(ExploreActivity.class);
            stackBuilder.addNextIntent(myIntent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews smallContent = new RemoteViews(context.getPackageName(), R.layout.notification);
            smallContent.setTextViewText(R.id.NotifText, title);

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_notif)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContent(smallContent)
                    .build ();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1001, notification);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }
}
