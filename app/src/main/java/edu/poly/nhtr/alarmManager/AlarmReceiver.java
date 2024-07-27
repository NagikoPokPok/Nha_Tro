package edu.poly.nhtr.alarmManager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
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
import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.Activity.MainActivity;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        int requestCode = intent.getIntExtra("requestCode", -1);
        Home home = (Home) intent.getSerializableExtra("home");
        Room room = (Room) intent.getSerializableExtra("room");
        String header = (String) intent.getSerializableExtra("header");
        String body = (String) intent.getSerializableExtra("body");
        if (home == null) return;

        preferenceManager = new PreferenceManager(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> notificationIndex = new HashMap<>();
        //String header = "Nhập chỉ số cho nhà trọ " + home.getNameHome();
        //String body = "Hôm nay là ngày bạn cần nhập thông tin chỉ số cho tất cả các phòng ở nhà trọ " + home.getNameHome();

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
                    setRepetitiveAlarm(new AlarmService(context, home, null, header, body), requestCode);
                    notificationIndex.put(Constants.KEY_ROOM_ID, "");
                    notificationIndex.put(Constants.KEY_NAME_ROOM, "");
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_INDEX, true);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_BILL, false);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_IS_READ, false);
                }

                if( room != null){
                    setRepetitiveAlarm(new AlarmService(context, home, room, header, body), requestCode);
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

        FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                preferenceManager.getString(Constants.KEY_FCM_TOKEN),
                header, body, context.getApplicationContext()
        );
        fcmNotificationSender.SendNotifications();

        sendBadgeUpdateBroadcast(context);
    }

    private void notifyUserWithRoom(String header, String body, Home home, Room room, String notificationID, Context context, PreferenceManager preferenceManager) {
        preferenceManager.putString(Constants.KEY_NOTIFICATION_ID, notificationID, getInfoUserFromGoogleAccount(context, preferenceManager));
        preferenceManager.putHome(Constants.KEY_COLLECTION_HOMES, home, getInfoUserFromGoogleAccount(context, preferenceManager));
        preferenceManager.putRoom(Constants.KEY_COLLECTION_ROOMS, room, home.idHome);

        FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                preferenceManager.getString(Constants.KEY_FCM_TOKEN),
                header, body, context.getApplicationContext()
        );
        fcmNotificationSender.SendNotifications();

        sendBadgeUpdateBroadcast(context);
    }

    private void sendBadgeUpdateBroadcast(Context context) {
        Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
        context.getApplicationContext().sendBroadcast(intent);
    }


    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    private void setRepetitiveAlarm(AlarmService alarmService, int requestCode) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        Timber.d("Set alarm for next month same time - %s", convertDate(cal.getTimeInMillis()));
        alarmService.setRepetitiveAlarm(cal.getTimeInMillis(),requestCode);
    }

    private String convertDate(long timeInMillis) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString();
    }
}
