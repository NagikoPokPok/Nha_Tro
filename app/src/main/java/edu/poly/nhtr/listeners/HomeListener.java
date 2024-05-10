package edu.poly.nhtr.listeners;


import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.models.Home;

public interface HomeListener {
    void onUserClicked(Home home);

    void showToast(String message);
    String getInfoUserFromGoogleAccount();

    void putHomeInfoInPreferences(String nameHome, String address, DocumentReference documentReference);
    void dialogClose();
    void hideLoading();
    void showLoading();
    void addHome(List<Home> homes);
    void addHomeFailed();
    boolean isAdded2();
}
