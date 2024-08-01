package edu.poly.nhtr.alarmManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Set;

import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private PreferenceManager preferenceManager;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        preferenceManager = new PreferenceManager(context);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed");

            Set<String> alarms = preferenceManager.getSet(Constants.KEY_NOTIFICATION_SET);
            if (alarms == null || alarms.isEmpty()) {
                Log.d(TAG, "No alarms found in preferences");
                return;
            }

            for (String alarm : alarms) {
                try {
                    String[] parts = alarm.split(",");
                    if (parts.length < 9) {
                        Log.e(TAG, "Invalid alarm format: " + alarm);
                        continue;
                    }
                    long timeInMillis = Long.parseLong(parts[0]);
                    int requestCode = Integer.parseInt(parts[1]);
                    String homeID = parts[2];
                    String homeName = parts[3];
                    String roomID = parts[4];
                    String roomName = parts[5];
                    String header = parts[6];
                    String body = parts[7];
                    String userID = parts[8];

                    // Kiểm tra trạng thái hủy của alarm
                    SharedPreferences prefs = context.getSharedPreferences("AlarmsPrefs", Context.MODE_PRIVATE);
                    boolean isCancelled = prefs.getBoolean("alarm_cancelled_" + requestCode, false);
                    if (isCancelled) {
                        Log.d(TAG, "Alarm with requestCode " + requestCode + " has been cancelled.");
                        continue;
                    }

                    Home home = preferenceManager.getHome(Constants.KEY_COLLECTION_HOMES, userID);
                    Room room = preferenceManager.getRoom(Constants.KEY_COLLECTION_ROOMS, homeID);

                    if (home == null) {
                        Log.e(TAG, "Home not found for ID: " + homeID);
                        continue;
                    }

                    AlarmService alarmService = new AlarmService(context, home, room, header, body);
                    alarmService.setAlarmsAfterBoot(timeInMillis, requestCode, home, room, header, body);

                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing alarm data: " + alarm, e);
                }
            }
        } else {
            Log.d(TAG, "Received unexpected intent: " + intent.getAction());
        }
    }


}
