package edu.poly.nhtr.Class;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {
    private static final String CHANNEL_ID = "ALARM_MANAGER_CHANNEL";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(this);
    }

    private void createNotificationChannel(Context context) {
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
