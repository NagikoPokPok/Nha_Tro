package edu.poly.nhtr.listeners;


import android.view.View;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.models.Home;

public interface HomeListener {
    void onUserClicked(Home home);
    void openPopup(View view, Home home, ItemContainerHomesBinding binding);

    void showToast(String message);
    String getInfoUserFromGoogleAccount();

    void putHomeInfoInPreferences(String nameHome, String address, DocumentReference documentReference);
    void dialogClose();
    void hideLoading();
    void showLoading();
    void addHome(List<Home> homes);
    void addHomeFailed();
    boolean isAdded2();
    void openDialogSuccess();
    void showLoadingAdd();
    void hideLoadingAdd();
    void showLoadingDelete();
    void hideLoadingDelete();
}
