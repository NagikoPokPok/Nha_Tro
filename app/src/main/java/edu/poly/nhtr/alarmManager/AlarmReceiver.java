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
            case Constants.ACTION_SET_EXACT:
            case Constants.ACTION_SET_REPETITIVE_EXACT:
                notificationIndex.put(Constants.KEY_NOTIFICATION_HEADER, header);
                notificationIndex.put(Constants.KEY_NOTIFICATION_BODY, body);
                notificationIndex.put(Constants.KEY_USER_ID, getInfoUserFromGoogleAccount(context, preferenceManager));
                notificationIndex.put(Constants.KEY_HOME_ID, home.getIdHome());
                notificationIndex.put(Constants.KEY_NAME_HOME, home.getNameHome());
                notificationIndex.put(Constants.KEY_TIMESTAMP, new Date());

                if (Constants.ACTION_SET_REPETITIVE_EXACT.equals(intent.getAction()) && room==null) {
                    setRepetitiveAlarm(new AlarmService(context, home, null, header, body));
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_INDEX, true);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_OF_BILL, false);
                    notificationIndex.put(Constants.KEY_NOTIFICATION_IS_READ, false);
                }

                if( room != null){
                    setRepetitiveAlarm(new AlarmService(context, home, room, header, body));
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
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String header = document.getString(Constants.KEY_NOTIFICATION_HEADER);
                                String body = document.getString(Constants.KEY_NOTIFICATION_BODY);
                                String homeID = document.getString(Constants.KEY_HOME_ID);
                                String roomID = document.getString(Constants.KEY_ROOM_ID);
                                String notificationID = document.getId();

                                if (homeID != null) {
                                    db.collection(Constants.KEY_COLLECTION_HOMES)
                                            .document(homeID)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot homeDocumentSnapshot) {
                                                    if (homeDocumentSnapshot.exists()) {
                                                        Home home = homeDocumentSnapshot.toObject(Home.class);
                                                        if (home != null) {
                                                            home.idHome = homeDocumentSnapshot.getId();
                                                            home.isHaveService = homeDocumentSnapshot.getBoolean(Constants.KEY_HOME_IS_HAVE_SERVICE);
                                                            home.numberOfRooms = Objects.requireNonNull(homeDocumentSnapshot.getLong(Constants.KEY_NUMBER_OF_ROOMS)).intValue();
                                                            home.userID = homeDocumentSnapshot.getString(Constants.KEY_USER_ID);
                                                            home.dateObject = homeDocumentSnapshot.getDate(Constants.KEY_TIMESTAMP);

                                                            if(roomID!=null){
                                                                db.collection(Constants.KEY_COLLECTION_ROOMS)
                                                                        .document(roomID)
                                                                        .get()
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot roomDocumentSnapshot) {
                                                                                if (roomDocumentSnapshot.exists()) {
                                                                                    Room room = roomDocumentSnapshot.toObject(Room.class);
                                                                                    if (room != null) {
                                                                                        room.roomId = roomDocumentSnapshot.getId();
                                                                                        room.nameRoom = roomDocumentSnapshot.getString(Constants.KEY_NAME_ROOM);
                                                                                        room.price = roomDocumentSnapshot.getString(Constants.KEY_PRICE);

                                                                                        // There are two ways to push notification to user
                                                                                        // C1: push notification by alarm service: buildNotification(context, header, body, home, documentReference);
                                                                                        // buildNotification(context, header, body, home, documentReference);


                                                                                        // C2: User FCM HTTP V1 to push notification, assure using true data payload structure, which use data instead notification
                                                                                        preferenceManager.putString(Constants.KEY_NOTIFICATION_ID, notificationID, getInfoUserFromGoogleAccount(context,preferenceManager));
                                                                                        preferenceManager.putHome(Constants.KEY_COLLECTION_HOMES, home, getInfoUserFromGoogleAccount(context,preferenceManager));
                                                                                        preferenceManager.putRoom(Constants.KEY_COLLECTION_ROOMS, room, homeID);

                                                                                        FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                                                                                                preferenceManager.getString(Constants.KEY_FCM_TOKEN),
                                                                                                header, body, context.getApplicationContext()
                                                                                        );
                                                                                        fcmNotificationSender.SendNotifications();

                                                                                        // Gửi broadcast để cập nhật badge
                                                                                        Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
                                                                                        context.getApplicationContext().sendBroadcast(intent);

                                                                                    }
                                                                                }
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                // Handle failure
                                                                            }
                                                                        });
                                                            }

                                                            // There are two ways to push notification to user
                                                            // C1: push notification by alarm service: buildNotification(context, header, body, home, documentReference);
                                                           // buildNotification(context, header, body, home, documentReference);


                                                            // C2: User FCM HTTP V1 to push notification, assure using true data payload structure, which use data instead notification
                                                            preferenceManager.putString(Constants.KEY_NOTIFICATION_ID, notificationID, getInfoUserFromGoogleAccount(context,preferenceManager));
                                                            preferenceManager.putHome(Constants.KEY_COLLECTION_HOMES, home, getInfoUserFromGoogleAccount(context,preferenceManager));

                                                            FcmNotificationSender fcmNotificationSender = new FcmNotificationSender(
                                                                    preferenceManager.getString(Constants.KEY_FCM_TOKEN),
                                                                    header, body, context.getApplicationContext()
                                                            );
                                                            fcmNotificationSender.SendNotifications();

                                                            // Gửi broadcast để cập nhật badge
                                                            Intent intent = new Intent("edu.poly.nhtr.ACTION_UPDATE_BADGE");
                                                            context.getApplicationContext().sendBroadcast(intent);

                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Handle failure
                                                }
                                            });
                                }


                            }
                        }
                    }
                });
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void buildNotification(Context context,String header, String body, Home home, DocumentReference documentReference) {
        createNotificationChannel(context);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent resultIntent = new Intent(context, MainRoomActivity.class);
        resultIntent.putExtra("FRAGMENT_TO_LOAD", "IndexFragment");
        resultIntent.putExtra("home", home);
        resultIntent.putExtra("notification_document_id", documentReference.getId());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(header)
                .setContentText(body)
                .setSound(uri)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setRepetitiveAlarm(AlarmService alarmService) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        Timber.d("Set alarm for next month same time - %s", convertDate(cal.getTimeInMillis()));
        alarmService.setRepetitiveAlarm(cal.getTimeInMillis());
    }

    private String convertDate(long timeInMillis) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString();
    }
}
