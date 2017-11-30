package com.joekelly.mapsandlocation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by jakek on 13/11/2017.
 */

public class Notification {

    public static void notifier(Context context, String message) {
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.walking);
        Resources res = context.getResources();
        String title = String.format(res.getString(R.string.title_message), message);
        String text = String.format(res.getString(R.string.message_body), message);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.walking)
                        .setContentTitle(title)
                        .setContentText(text)
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
