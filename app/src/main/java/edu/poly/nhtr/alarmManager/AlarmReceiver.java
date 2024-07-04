package edu.poly.nhtr.alarmManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.format.DateFormat;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;
import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_MANAGER_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        long timeInMillis = intent.getLongExtra(Constants.EXTRA_EXACT_ALARM_TIME, 0L);
        switch (Objects.requireNonNull(intent.getAction())) {
            case Constants.ACTION_SET_EXACT:
                buildNotification(context, "Set Exact Time", convertDate(timeInMillis));
                break;
            case Constants.ACTION_SET_REPETITIVE_EXACT:
                setRepetitiveAlarm(new AlarmService(context));
                buildNotification(context, "Set Repetitive Exact Time", convertDate(timeInMillis));
                break;
        }
    }

    private void buildNotification(Context context, String title, String message) {
        createNotificationChannel(context);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_notification) // Replace with your app's icon
                .setContentTitle(title)
                .setContentText("I got triggered at - " + message)
                .setSound(uri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel(Context context) {
        CharSequence name = "Alarm Manager Channel";
        String description = "Channel for Alarm Manager notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void setRepetitiveAlarm(AlarmService alarmService) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(7));
        Timber.d("Set alarm for next week same time - %s", convertDate(cal.getTimeInMillis()));
        alarmService.setRepetitiveAlarm(cal.getTimeInMillis());
    }

    private String convertDate(long timeInMillis) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString();
    }
}
