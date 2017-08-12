package com.example.mohit.friendschat.connection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.mohit.friendschat.MainActivity;
import com.example.mohit.friendschat.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by mohit on 13/6/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData().size()>0){
            Map<String,String> payload=remoteMessage.getData();

            showNotification(payload);
        }
    }

    private void showNotification(Map<String,String> payload){
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(payload.get("name")+"sent Message");
        builder.setContentText(payload.get("text"));
        builder.setSound(defaultSoundUri);


        Intent resultIntent=new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(this);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent= stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
    }
}
