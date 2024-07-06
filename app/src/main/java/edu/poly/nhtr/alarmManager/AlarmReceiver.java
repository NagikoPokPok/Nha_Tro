package edu.poly.nhtr.alarmManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.text.format.DateFormat;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.poly.nhtr.Activity.MainActivity;

import edu.poly.nhtr.R;
import edu.poly.nhtr.firebase.FcmNotificationSender;
import edu.poly.nhtr.firebase.MessagingService;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_MANAGER_CHANNEL";
    private static final String PREFERENCES_FILE = "edu.poly.nhtr.PREFERENCES_FILE";

    @Override
    public void onReceive(Context context, Intent intent) {
        long timeInMillis = intent.getLongExtra(Constants.EXTRA_EXACT_ALARM_TIME, 0L);
        switch (Objects.requireNonNull(intent.getAction())) {
            case Constants.ACTION_SET_EXACT:
                // Register broadcast receiver to listen for notification data updates
                LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (MessagingService.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
                            // Build notification when data is updated
                            buildNotification(context);
                        }
                    }
                }, new IntentFilter(MessagingService.ACTION_NOTIFICATION_RECEIVED));

                PreferenceManager preferenceManager = new PreferenceManager(context.getApplicationContext());
                FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                        preferenceManager.getString(Constants.KEY_FCM_TOKEN),
                        "FCM", "This is try test of FCM Part 3", context.getApplicationContext()
                );

                fcmNotificationSender.SendNotifications();


                break;
            case Constants.ACTION_SET_REPETITIVE_EXACT:
                setRepetitiveAlarm(new AlarmService(context));
                buildNotification(context);
                break;
        }
    }


    private void buildNotification(Context context) {
        PreferenceManager preferenceManager = new PreferenceManager(context.getApplicationContext());
        String notificationTitle = preferenceManager.getString("notification_title");
        String notificationBody = preferenceManager.getString("notification_body");

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("FRAGMENT_TO_LOAD", "NotificationFragment");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Alarm Manager Channel", NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            builder.setChannelId(CHANNEL_ID);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1001, builder.build());
    }

    private void setRepetitiveAlarm(AlarmService alarmService) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1); // Thêm 1 tháng vào thời gian hiện tại
        Timber.d("Set alarm for next month same time - %s", convertDate(cal.getTimeInMillis()));
        alarmService.setRepetitiveAlarm(cal.getTimeInMillis());
    }

    private String convertDate(long timeInMillis) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString();
    }
}

