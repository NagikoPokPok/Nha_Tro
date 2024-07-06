package edu.poly.nhtr.firebase;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Random;

import edu.poly.nhtr.Activity.MainActivity;
import edu.poly.nhtr.Activity.MainRoomActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MessagingService extends FirebaseMessagingService {
    public static final String ACTION_NOTIFICATION_RECEIVED = "edu.poly.nhtr.ACTION_NOTIFICATION_RECEIVED";


    private NotificationManager notificationManager;
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Log.d("FCM", "Token: "+token);
        updateNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        //Log.d("FCM", "Token: "+ Objects.requireNonNull(message.getNotification()).getBody());

        // Save notification data
        saveNotificationToPreferences(Objects.requireNonNull(message.getNotification()).getTitle(), message.getNotification().getBody());

        // Notify that the notification data has been saved
        Intent intent = new Intent(ACTION_NOTIFICATION_RECEIVED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


    private void updateNewToken(String token)
    {

    }

    private void saveNotificationToPreferences(String title, String body) {
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        preferenceManager.putString("notification_title", title);
        preferenceManager.putString("notification_body", body);
    }
}
