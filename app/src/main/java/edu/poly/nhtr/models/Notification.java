package edu.poly.nhtr.models;

import java.util.Date;

public class Notification {

    public String header, body, notificationID;
    public String userID, homeID, homeName;
    public Boolean notificationOfIndex, notificationOfBill;
    public Date dateObject;
    public Boolean isRead;

    public Date getDateObject() {
        return dateObject;
    }

    public Boolean getRead() {
        return isRead;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public String getUserID() {
        return userID;
    }

    public String getHomeID() {
        return homeID;
    }

    public String getHomeName() {
        return homeName;
    }

    public Boolean getNotificationOfIndex() {
        return notificationOfIndex;
    }

    public Boolean getNotificationOfBill() {
        return notificationOfBill;
    }
}
