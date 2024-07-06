package edu.poly.nhtr.alarmManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import edu.poly.nhtr.fragment.IndexFragment;
import edu.poly.nhtr.utilities.Constants;


public class AlarmService {
    private final Context context;
    private final AlarmManager alarmManager;
    private IndexFragment indexFragment;

    public AlarmService(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setExactAlarm(long timeInMillis) {
        setAlarm(
                timeInMillis,
                getPendingIntent(
                        getIntent().setAction(Constants.ACTION_SET_EXACT)
                                .putExtra(Constants.EXTRA_EXACT_ALARM_TIME, timeInMillis)
                )
        );
    }

    // 1 Week
    public void setRepetitiveAlarm(long timeInMillis) {
        setAlarm(
                timeInMillis,
                getPendingIntent(
                        getIntent().setAction(Constants.ACTION_SET_REPETITIVE_EXACT)
                                .putExtra(Constants.EXTRA_EXACT_ALARM_TIME, timeInMillis)
                )
        );
    }

    private PendingIntent getPendingIntent(Intent intent) {
        return PendingIntent.getBroadcast(
                context,
               0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void setAlarm(long timeInMillis, PendingIntent pendingIntent) {
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
            );
        }
    }

    private Intent getIntent() {
        return new Intent(context, AlarmReceiver.class);
    }

    private int getRandomRequestCode() {
        return RandomUtil.getRandomInt();
    }

    public void cancelRepetitiveAlarm() {
        Intent intent = getIntent().setAction(Constants.ACTION_SET_REPETITIVE_EXACT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0, // Use the same request code if applicable
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}


