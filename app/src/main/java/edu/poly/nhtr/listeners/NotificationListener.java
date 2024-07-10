package edu.poly.nhtr.listeners;

import java.util.List;

import edu.poly.nhtr.models.Notification;

public interface NotificationListener {
    void setNotificationList(List<Notification> notificationList);
    String getInfoUserFromGoogleAccount();
    boolean isAdded2();
    void showToast(String message);
    void showLayoutNoData();
    void hideLayoutNoData();
    void setNotificationIsRead(int position);
}
