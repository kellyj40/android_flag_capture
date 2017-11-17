package com.joekelly.mapsandlocation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by jakek on 13/11/2017.
 */

public class Notification {

    public static void notifier(Context context) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.walking_scene);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.walking_scene)
                        .setContentTitle("1KM walked Today!")
                        .setContentText("Congrats! You have walked a kilometer today!")
                        .setLargeIcon(icon);

        Intent resultIntent = new Intent(context, StatsActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        //autocancel dismissed the notification when its clicked on
        mBuilder.setAutoCancel(true);



        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
