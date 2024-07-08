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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.Activity.MainActivity;

import edu.poly.nhtr.R;
import edu.poly.nhtr.firebase.FcmNotificationSender;
import edu.poly.nhtr.firebase.MessagingService;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import timber.log.Timber;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "ALARM_MANAGER_CHANNEL";
    private static final String PREFERENCES_FILE = "edu.poly.nhtr.PREFERENCES_FILE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Home home = (Home) intent.getSerializableExtra("home");
        PreferenceManager preferenceManager = new PreferenceManager(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> notificationIndex = new HashMap<>();
        assert home != null;
        String header = "Nhập chỉ số cho nhà trọ " + home.getNameHome();
        String body = "Hôm nay là ngày bạn cần nhập thông tin chỉ số cho tất cả các phòng ở nhà trọ " + home.getNameHome();
        long timeInMillis = intent.getLongExtra(Constants.EXTRA_EXACT_ALARM_TIME, 0L);
        switch (Objects.requireNonNull(intent.getAction())) {
            case Constants.ACTION_SET_EXACT:

                notificationIndex.put(Constants.KEY_NOTIFICATION_HEADER, header);
                notificationIndex.put(Constants.KEY_NOTIFICATION_BODY, body);
                notificationIndex.put(Constants.KEY_USER_ID, getInfoUserFromGoogleAccount(context, preferenceManager));
                notificationIndex.put(Constants.KEY_HOME_ID, home.getIdHome());
                notificationIndex.put(Constants.KEY_NAME_HOME, home.getNameHome());
                notificationIndex.put(Constants.KEY_TIMESTAMP, new Date());

                db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                        .add(notificationIndex)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                getNotificationIndex(documentReference, context);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showToast(context, "failure");
                            }
                        });


                break;
            case Constants.ACTION_SET_REPETITIVE_EXACT:
                setRepetitiveAlarm(new AlarmService(context, home));
                //buildNotification(context);

                notificationIndex.put(Constants.KEY_NOTIFICATION_HEADER, header);
                notificationIndex.put(Constants.KEY_NOTIFICATION_BODY, body);
                notificationIndex.put(Constants.KEY_USER_ID, getInfoUserFromGoogleAccount(context, preferenceManager));
                notificationIndex.put(Constants.KEY_HOME_ID, home.getIdHome());
                notificationIndex.put(Constants.KEY_NAME_HOME, home.getNameHome());
                notificationIndex.put(Constants.KEY_TIMESTAMP, new Date());
                notificationIndex.put(Constants.KEY_NOTIFICATION_OF_INDEX, true);

                db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                        .add(notificationIndex)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                getNotificationIndex(documentReference, context);
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
        // Lấy thông tin người dùng từ tài khoản Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        String currentUserId = "";
        if (account != null) {
            currentUserId = account.getId();
        } else {
            currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        }
        return currentUserId;
    }

    private void getNotificationIndex(DocumentReference documentReference, Context context)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                .document(documentReference.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            String header = document.getString(Constants.KEY_NOTIFICATION_HEADER);
                            String body = document.getString(Constants.KEY_NOTIFICATION_BODY);

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
                                    header, body, context.getApplicationContext()
                            );

                            fcmNotificationSender.SendNotifications();

                        }
                    }
                });
    }


    private void showToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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

