package edu.poly.nhtr.presenters;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import edu.poly.nhtr.listeners.NotificationListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.utilities.Constants;
import timber.log.Timber;

public class NotificationPresenter {

    private final NotificationListener notificationListener;
    String homeID;

    public NotificationPresenter(NotificationListener notificationListener, String homeID) {
        this.notificationListener = notificationListener;
        this.homeID = homeID;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getNotification(OnSetNotificationListCompleteListener listener, List<Home> homeList) {
        List<Notification> notificationList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0); // Biến đếm số lượng chỉ số đã được xử lý

        // Sử dụng vòng lặp để duyệt qua từng đối tượng Home trong danh sách homeList
        for (Home home : homeList) {
            String homeID = home.getIdHome(); // Lấy homeID từ đối tượng Home hiện tại

            // Truy vấn database để lấy danh sách các thông báo dựa trên homeID
            db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                    .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Duyệt qua từng document trong querySnapshot
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
                                }
                            }
                        } else {
                            // Xử lý khi không thành công
                            Timber.e(task.getException(), "Error getting notifications for homeID: %s", homeID);
                        }

                        // Tăng biến đếm sau mỗi lần truy vấn hoàn thành
                        count.incrementAndGet();

                        // Nếu đã duyệt qua hết tất cả các home trong homeList thì gọi onComplete
                        if (count.get() == homeList.size()) {
                            listener.onComplete();
                            notificationListener.setNotificationList(notificationList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi có lỗi xảy ra trong quá trình truy vấn
                        Timber.e(e, "Error querying notifications for homeID: %s", homeID);
                        count.incrementAndGet(); // Tăng biến đếm để tránh deadlock

                        // Kiểm tra nếu đã duyệt qua hết tất cả các home trong homeList thì gọi onComplete
                        if (count.get() == homeList.size()) {
                            listener.onComplete();
                            notificationListener.setNotificationList(notificationList);
                        }
                    });
        }
    }


    public void getListHomes(OnGetHomeListCompleteListener listener) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = notificationListener.getInfoUserFromGoogleAccount();


        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (notificationListener.isAdded2()) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Home> homes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Home home = new Home();
                                home.nameHome = document.getString(Constants.KEY_NAME_HOME);
                                home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
                                home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                home.idHome = document.getId();
                                home.numberOfRooms = Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS)).intValue(); // Chuyển đổi thành Integer
                                homes.add(home);
                            }

                            listener.onComplete(homes);

                        } else {

                        }
                    }
                });

    }

    // Interface for the callback
    public interface OnSetNotificationListCompleteListener {
        void onComplete();
    }

    // Interface for the callback
    public interface OnGetHomeListCompleteListener {
        void onComplete(List<Home> homeList);
    }
}
