package edu.poly.nhtr.alarmManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class AlarmService extends Service {
    private final Context context;
    private final AlarmManager alarmManager;
    private final Home home;
    private final String header;
    private final String body;
    private final Room room;
    private PreferenceManager preferenceManager;

    public AlarmService(Context context, AlarmManager alarmManager, Home home, String header, String body, Room room){
        this.context = context;
        this.alarmManager = alarmManager;
        this.home = home;
        this.header = header;
        this.body = body;
        this.room = room;
    }

    public AlarmService(){
        this.context = null;
        this.alarmManager = null;
        this.home = null;
        this.header = null;
        this.body = null;
        this.room = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public AlarmService(Context context, Home home, Room room, String header, String body) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.home = home;
        this.room = room;
        this.header = header;
        this.body = body;

    }

    public Context getContext() {
        return context;
    }


    public void setRepetitiveAlarm(long timeInMillis, int requestCode) {
        //removeAlarmsByAction(requestCode);
        if (canScheduleExactAlarms()) {
            setAlarm(
                    timeInMillis,
                    getPendingIntent(
                            getIntent().setAction(Constants.ACTION_SET_REPETITIVE_EXACT)
                                    .putExtra(Constants.EXTRA_EXACT_ALARM_TIME, timeInMillis)
                                    .putExtra("requestCode", requestCode)
                                    .putExtra("home", home)
                                    .putExtra("room", room)
                                    .putExtra("header", header)
                                    .putExtra("body", body),
                            requestCode
                    )
            );
        } else {
            requestExactAlarmPermission();
        }
    }

    private void updateAlarmInPrefs(long timeInMillis, int requestCode) {
        SharedPreferences prefs = context.getSharedPreferences("AlarmsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Xóa các alarm cũ với cùng một action
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());
        Set<String> updatedAlarms = new HashSet<>();
        for (String alarm : alarms) {
            String[] parts = alarm.split(",");
            String existingAction = parts[1];
            if (!existingAction.equals(String.valueOf(requestCode))) {
                updatedAlarms.add(alarm);
            }
        }

        // Thêm alarm mới
        updatedAlarms.add(timeInMillis + "," + requestCode);
        editor.putStringSet("alarms", updatedAlarms);
        editor.apply();
    }

    private void removeAlarmsByAction(int requestCode) {
        SharedPreferences prefs = context.getSharedPreferences("AlarmsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Xóa các alarm cũ với cùng một action
        Set<String> alarms = prefs.getStringSet("alarms", new HashSet<>());
        Set<String> updatedAlarms = new HashSet<>();
        for (String alarm : alarms) {
            String[] parts = alarm.split(",");
            String existingAction = parts[1];
            if (!existingAction.equals(String.valueOf(requestCode))) {
                updatedAlarms.add(alarm);
            }
        }

        editor.putStringSet("alarms", updatedAlarms);
        editor.apply();
    }

    private boolean canScheduleExactAlarms() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        } else {
            return true; // No permission required for older versions
        }
    }

    private void requestExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);
        }
    }

    private PendingIntent getPendingIntent(Intent intent, int requestCode) {
        return PendingIntent.getBroadcast(
                context,
                requestCode,
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

    public void cancelRepetitiveAlarm(int requestCode) {
        Intent intent = getIntent().setAction(Constants.ACTION_SET_REPETITIVE_EXACT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        // Lưu trạng thái hủy vào SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("AlarmsPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("alarm_cancelled_" + requestCode, true);
        editor.apply();
    }


    public void setAlarmsAfterBoot(long timeInMillis, int requestCode, Home home, Room room, String header, String body) {
        preferenceManager = new PreferenceManager(context);
        Log.d("Alarm Manager", "Setting alarm after boot for requestCode: " + requestCode);

        // Kiểm tra nếu timeInMillis là thời gian trong tương lai
        if (timeInMillis > System.currentTimeMillis()) {
            Intent intent = getIntent()
                    .setAction(Constants.ACTION_SET_REPETITIVE_EXACT)
                    .putExtra(Constants.EXTRA_EXACT_ALARM_TIME, timeInMillis)
                    .putExtra("requestCode", requestCode)
                    .putExtra("home", home)
                    .putExtra("room", room)
                    .putExtra("header", header)
                    .putExtra("body", body);

            PendingIntent pendingIntent = getPendingIntent(intent, requestCode);

            if (canScheduleExactAlarms()) {
                setAlarm(timeInMillis, pendingIntent);
            } else {
                requestExactAlarmPermission();
                Log.e("Alarm Manager", "Exact alarm scheduling permission required.");
            }
        } else {
            Log.e("Alarm Manager", "Invalid timeInMillis: Time must be in the future.");
        }
    }


}

