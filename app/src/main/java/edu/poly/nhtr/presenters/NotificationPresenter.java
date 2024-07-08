package edu.poly.nhtr.presenters;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import edu.poly.nhtr.listeners.NotificationListener;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.utilities.Constants;

public class NotificationPresenter {

    private final NotificationListener notificationListener;
    String homeID;

    public NotificationPresenter(NotificationListener notificationListener, String homeID) {
        this.notificationListener = notificationListener;
        this.homeID = homeID;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getNotification() {
        List<Notification> notificationList = new ArrayList<>();
        db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.KEY_NAME_HOME, homeID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                AtomicInteger count = new AtomicInteger(0); // Biến đếm số lượng chỉ số đã được xử lý

                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Notification notification = new Notification();
                                    notification.userID = document.getString(Constants.KEY_USER_ID);
                                    notification.homeID = document.getString(Constants.KEY_HOME_ID);
                                    notification.homeName = document.getString(Constants.KEY_NAME_HOME);
                                    notification.header = document.getString(Constants.KEY_NOTIFICATION_HEADER);
                                    notification.body = document.getString(Constants.KEY_NOTIFICATION_BODY);
                                    notification.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                    notification.notificationOfIndex = document.getBoolean(Constants.KEY_NOTIFICATION_OF_INDEX);
                                    notificationList.add(notification);
                                    count.incrementAndGet();
                                }
                            }
                        }

                    }
                });
    }
}
