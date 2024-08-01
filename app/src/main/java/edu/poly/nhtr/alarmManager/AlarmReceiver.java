package edu.poly.nhtr.alarmManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.poly.nhtr.Activity.MainActivity;
import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.Activity.MainRoomActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.firebase.FcmNotificationSender;
import edu.poly.nhtr.firebase.MessagingService;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_MANAGER_CHANNEL";
    private PreferenceManager preferenceManager;
    private static final String PREFS_NAME = "AlarmsPrefs"; // Đổi tên để phù hợp với SharedPreferences trong AlarmService
    private static final String PREFS_KEY_ALARMS = "alarms";
    private Home home;
    private Room room;
    private String header, body;
    private int requestCode;

    @Override
    public void onReceive(Context context, Intent intent) {
        requestCode = intent.getIntExtra("requestCode", -1);
        home = (Home) intent.getSerializableExtra("home");
        room = (Room) intent.getSerializableExtra("room");
        header = (String) intent.getSerializableExtra("header");
        body = (String) intent.getSerializableExtra("body");
        if (home == null) return;

        preferenceManager = new PreferenceManager(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> notificationIndex = new HashMap<>();

        switch (Objects.requireNonNull(intent.getAction())) {
            case Constants.ACTION_SET_EXACT: // For bil

            case Constants.ACTION_SET_REPETITIVE_EXACT: // For index
                notificationIndex.put(Constants.KEY_NOTIFICATION_HEADER, header);
                notificationIndex.put(Constants.KEY_NOTIFICATION_BODY, body);
                notificationIndex.put(Constants.KEY_USER_ID, getInfoUserFromGoogleAccount(context, preferenceManager));
                notificationIndex.put(Constants.KEY_HOME_ID, home.getIdHome());
                notificationIndex.put(Constants.KEY_NAME_HOME, home.getNameHome());
                notificationIndex.put(Constants.KEY_TIMESTAMP, new Date());

                if (Constants.ACTION_SET_REPETITIVE_EXACT.equals(intent.getAction()) && room==null) {
                    setRepetitiveAlarm(new AlarmService(context, home, null, header, body), requestCode, context, preferenceManager);
                    notificationIndex.put(Constants.KEY_ROOM_ID, "");
                    notificationIndex.put(Constants.KEY_NAME_ROOM, "");
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_INDEX, true);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_BILL, false);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_IS_READ, false);
                }

                if( room != null){
                    setRepetitiveAlarm(new AlarmService(context, home, room, header, body), requestCode, context, preferenceManager);
                    notificationIndex.put(Constants.KEY_ROOM_ID, room.getRoomId());
                    notificationIndex.put(Constants.KEY_NAME_ROOM, room.getNameRoom());
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_BILL, true);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_INDEX, false);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_IS_READ, false);
                }

                db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                        .add(notificationIndex)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                getNotificationIndex(documentReference, context, preferenceManager);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showToast(context, "failure");
                            }
                        });
                break;
        }
    }

    public String getInfoUserFromGoogleAccount(Context context, PreferenceManager preferenceManager) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        String currentUserId = "";
        if (account != null) {
            currentUserId = account.getId();
        } else {
            currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        }
        return currentUserId;
    }

    private void getNotificationIndex(DocumentReference documentReference, Context context, PreferenceManager preferenceManager) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                .document(documentReference.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        handleNotificationDocument(document, context, preferenceManager, db);
                    }
                });
    }

    private void handleNotificationDocument(DocumentSnapshot document, Context context, PreferenceManager preferenceManager, FirebaseFirestore db) {
        String header = document.getString(Constants.KEY_NOTIFICATION_HEADER);
        String body = document.getString(Constants.KEY_NOTIFICATION_BODY);
        String homeID = document.getString(Constants.KEY_HOME_ID);
        String roomID = document.getString(Constants.KEY_ROOM_ID);
        String notificationID = document.getId();

        if (homeID != null) {
            db.collection(Constants.KEY_COLLECTION_HOMES)
                    .document(homeID)
                    .get()
                    .addOnSuccessListener(homeDocumentSnapshot -> {
                        if (homeDocumentSnapshot.exists()) {
                            Home home = homeDocumentSnapshot.toObject(Home.class);
                            if (home != null) {
                                setupHome(home, homeDocumentSnapshot);
                                if (!Objects.equals(roomID, "")) {
                                    fetchRoomAndNotify(roomID, home, header, body, notificationID, context, preferenceManager, db);
                                } else {
                                    notifyUser(header, body, home, notificationID, context, preferenceManager);
                                }
                            }
                        }
                    });
        }
    }

    private void setupHome(Home home, DocumentSnapshot homeDocumentSnapshot) {
        home.idHome = homeDocumentSnapshot.getId();
        home.isHaveService = homeDocumentSnapshot.getBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE);
        home.numberOfRooms = Objects.requireNonNull(homeDocumentSnapshot.getLong(Constants.KEY_NUMBER_OF_ROOMS)).intValue();
        home.userID = homeDocumentSnapshot.getString(Constants.KEY_USER_ID);
        home.dateObject = homeDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);
    }

    private void fetchRoomAndNotify(String roomID, Home home, String header, String body, String notificationID, Context context, PreferenceManager preferenceManager, FirebaseFirestore db) {
        db.collection(Constants.KEY_COLLECTION_ROOMS)
                .document(roomID)
                .get()
                .addOnSuccessListener(roomDocumentSnapshot -> {
                    if (roomDocumentSnapshot.exists()) {
                        Room room = roomDocumentSnapshot.toObject(Room.class);
                        if (room != null) {
                            setupRoom(room, roomDocumentSnapshot);
                            notifyUserWithRoom(header, body, home, room, notificationID, context, preferenceManager);
                        }
                    }
                });
    }

    private void setupRoom(Room room, DocumentSnapshot roomDocumentSnapshot) {
        room.roomId = roomDocumentSnapshot.getId();
        room.nameRoom = roomDocumentSnapshot.getString(Constants.KEY_NAME_ROOM);
        room.price = roomDocumentSnapshot.getString(Constants.KEY_PRICE);
    }

    private void notifyUser(String header, String body, Home home, String notificationID, Context context, PreferenceManager preferenceManager) {
        preferenceManager.putString(Constants.KEY_NOTIFICATION_ID, notificationID, getInfoUserFromGoogleAccount(context, preferenceManager));
        preferenceManager.putHome(Constants.KEY_COLLECTION_HOMES, home, getInfoUserFromGoogleAccount(context, preferenceManager));

        buildNotification(context, header, body, notificationID, home, room);
       sendBadgeUpdateBroadcast(context);
    }

    private void notifyUserWithRoom(String header, String body, Home home, Room room, String notificationID, Context context, PreferenceManager preferenceManager) {
        preferenceManager.putString(Constants.KEY_NOTIFICATION_ID, notificationID, getInfoUserFromGoogleAccount(context, preferenceManager));
        preferenceManager.putHome(Constants.KEY_COLLECTION_HOMES, home, getInfoUserFromGoogleAccount(context, preferenceManager));
        preferenceManager.putRoom(Constants.KEY_COLLECTION_ROOMS, room, home.idHome);


        buildNotification(context, header, body, notificationID, home, room);

        sendBadgeUpdateBroadcast(context);
    }

    private void sendBadgeUpdateBroadcast(Context context) {
        Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
        context.getApplicationContext().sendBroadcast(intent);
    }

    private void buildNotification(Context context, String title, String body, String notificationID, Home home, Room room) {
        // Ensure permission is granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Notification", "Notification permission not granted");
            return;
        }

        createNotificationChannel(context);

        PendingIntent resultPendingIntent = getResultPendingIntent(context, notificationID, home, room);

        // Convert drawable resource to Bitmap
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_home_for_app);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_notification)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private PendingIntent getResultPendingIntent(Context context, String notificationID, Home home, Room room) {
        Intent resultIntent;
        int requestCode = (int) System.currentTimeMillis(); // Unique request code for each PendingIntent

        if (room == null) {
            resultIntent = new Intent(context, MainRoomActivity.class);
            resultIntent.putExtra("FRAGMENT_TO_LOAD", "IndexFragment");
            resultIntent.putExtra("home", home);
            resultIntent.putExtra("notification_document_id", notificationID);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            Intent intentMainRoom = new Intent(context, MainRoomActivity.class);
            intentMainRoom.putExtra("FRAGMENT_TO_LOAD", "HomeFragment");
            intentMainRoom.putExtra("home", home);

            Intent intentDetailedRoom = new Intent(context, MainDetailedRoomActivity.class);
            intentDetailedRoom.putExtra("target_fragment_index", 2);
            intentDetailedRoom.putExtra("room", room);
            intentDetailedRoom.putExtra("notification_document_id", notificationID);
            intentDetailedRoom.putExtra("home", home);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
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


    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    private void setRepetitiveAlarm(AlarmService alarmService, int requestCode, Context context, PreferenceManager preferenceManager) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        Log.d("Set alarm for next month same time - %s", convertDate(cal.getTimeInMillis()));
        alarmService.setRepetitiveAlarm(cal.getTimeInMillis(),requestCode);

        // Save data of alarm
        saveAlarmData(home, room, header, body, requestCode, context, preferenceManager, cal.getTimeInMillis());
    }

    private void saveAlarmData(Home home, Room room, String header, String body, int requestCode, Context context, PreferenceManager preferenceManager, long timeInMillis) {

        String userID = getInfoUserFromGoogleAccount(context, preferenceManager);
        preferenceManager.putHome(Constants.KEY_COLLECTION_HOMES, home, userID);
        preferenceManager.putRoom(Constants.KEY_COLLECTION_ROOMS, room, home.getIdHome());


        Set<String> alarms = preferenceManager.getSet(Constants.KEY_NOTIFICATION_SET);

        // Xóa thông tin alarm cũ (nếu có)
        alarms.removeIf(alarm -> alarm.contains(String.valueOf(requestCode)));

        // Thêm alarm mới
        if(room==null){
            alarms.add(timeInMillis + "," + requestCode+ "," + home.getIdHome()+ "," + home.getNameHome()+ "," + null + "," + null + "," + header + "," + body + "," + userID);
        }else{
            alarms.add(timeInMillis + "," + requestCode+ "," + home.getIdHome()+ "," + home.getNameHome()+ "," +room.getRoomId() + "," + room.getNameRoom() + "," + header + "," + body + "," + userID);
        }

        preferenceManager.putSet(Constants.KEY_NOTIFICATION_SET, alarms);
    }


    private String convertDate(long timeInMillis) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString();
    }
}
