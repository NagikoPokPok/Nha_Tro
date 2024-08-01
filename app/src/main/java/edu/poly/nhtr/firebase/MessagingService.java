package edu.poly.nhtr.firebase;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.Activity.MainRoomActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MessagingService extends FirebaseMessagingService {
    public static final String ACTION_NOTIFICATION_RECEIVED = "edu.poly.nhtr.ACTION_NOTIFICATION_RECEIVED";
    private static final String CHANNEL_ID = "ALARM_MANAGER_CHANNEL";
    private PreferenceManager preferenceManager;
    private Room room;
    private Home home;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("MessagingService", "onMessageReceived called");

        // Use this code when FCM using data structure
        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");

        // Use this code when FCM using notification structure
        // String title = Objects.requireNonNull(message.getNotification()).getTitle();
        // String body = message.getNotification().getBody();

        preferenceManager = new PreferenceManager(this);
        String notificationID = preferenceManager.getString(Constants.KEY_NOTIFICATION_ID, getInfoUserFromGoogleAccount(this, preferenceManager));
        home = preferenceManager.getHome(Constants.KEY_COLLECTION_HOMES, getInfoUserFromGoogleAccount(this, preferenceManager));
        if (home != null) {
            room = preferenceManager.getRoom(Constants.KEY_COLLECTION_ROOMS, home.getIdHome());
        }

        buildNotification(title, body, notificationID, home, room);
    }

    private String getInfoUserFromGoogleAccount(Context context, PreferenceManager preferenceManager) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            return account.getId();
        } else {
            return preferenceManager.getString(Constants.KEY_USER_ID, "");
        }
    }

    private void updateNewToken(String token) {
        Log.d("Token", "New token: " + token);
        PreferenceManager preferenceManager = new PreferenceManager(this);
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
    }

    private void buildNotification(String title, String body, String notificationID, Home home, Room room) {
        // Ensure permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Notification", "Notification permission not granted");
            return;
        }

        createNotificationChannel(this);

        PendingIntent resultPendingIntent = getResultPendingIntent(notificationID, home, room);

        // Convert drawable resource to Bitmap
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon_home_for_app);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        Log.d("Notification", "Notification built and sent");
    }

    private PendingIntent getResultPendingIntent(String notificationID, Home home, Room room) {
        Intent resultIntent;
        int requestCode = (int) System.currentTimeMillis(); // Unique request code for each PendingIntent

        if (room == null) {
            resultIntent = new Intent(this, MainRoomActivity.class);
            resultIntent.putExtra("FRAGMENT_TO_LOAD", "IndexFragment");
            resultIntent.putExtra("home", home);
            resultIntent.putExtra("notification_document_id", notificationID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            Intent intentMainRoom = new Intent(this, MainRoomActivity.class);
            intentMainRoom.putExtra("FRAGMENT_TO_LOAD", "HomeFragment");
            intentMainRoom.putExtra("home", home);

            Intent intentDetailedRoom = new Intent(this, MainDetailedRoomActivity.class);
            intentDetailedRoom.putExtra("target_fragment_index", 2);
            intentDetailedRoom.putExtra("room", room);
            intentDetailedRoom.putExtra("notification_document_id", notificationID);
            intentDetailedRoom.putExtra("home", home);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainDetailedRoomActivity.class);
            stackBuilder.addNextIntent(intentMainRoom);
            stackBuilder.addNextIntent(intentDetailedRoom);

            return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Manager Channel";
            String description = "Channel for Alarm Manager notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
