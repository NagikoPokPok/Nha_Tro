package edu.poly.nhtr.listeners;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.models.MainGuest;

public interface MainGuestListener {
    void showToast(String message);
    void showLoading();

    String getInfoRoomFromGoogleAccount();

    String getInfoHomeFromGoogleAccount();


    boolean isAdded2();

    void initializeViews();

    void putContractInfoInPreferences(String nameGuest, String phoneGuest, String cccdNumber, String dateOfBirth, String gender, int totalMembers, String createDate, String dateIn, double roomPrice, String expirationDate, String payDate, int daysUntilDueDate, String cccdImageFront, String cccdImageBack, String contractImageFront, String contractImageBack, boolean status, String roomId, String homeId, DocumentReference documentReference);

    void putMainGuestInfoInPreferences(String nameGuest, String phoneGuest, String dateIn, boolean status, String roomId, String homeId, String cccdNumber, String cccdImageFront, String cccdImageBack, DocumentReference documentReference);

    void setUpDropDownMenuGender();

    void setUpDropDownMenuTotalMembers();

    void setUpDropDownMenuDays();

    void showErrorMessage(String message);

    boolean saveContract();

}
