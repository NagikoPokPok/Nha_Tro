package edu.poly.nhtr.listeners;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.models.MainGuest;

public interface MainGuestListener {
    void showToast(String message);
    void showLoading();

    String getInfoHomeFromGoogleAccount();
    String getInfoRoomFromGoogleAccount();
    void getListMainGuest(List<MainGuest> listContracts);
    void putContractInfoInPreferences(String nameGuest, String phoneGuest, String cccdNumber, String dateOfBirth, String gender, int totalMembers, String createDate, double roomPrice, String expirationDate, String payDate, int daysUntilDueDate, String cccdImageFront, String cccdImageBack, String contractImageFront, String contractImageBack, boolean status, String homeId, String roomId, DocumentReference documentReference);

    void onMainGuestsLoaded(List<MainGuest> mainGuests, String action);

    void onMainGuestsLoadFailed();
    boolean isAdded2();

    void initializeViews();

    void setUpDropDownMenuGender();
    void setUpDropDownMenuTotalMembers();

    void setUpDropDownMenuDays();

    void showErrorMessage(String message);

    void setNameErrorMessage(String message);

    void setPhoneErrorMessage(String message);

    void setCCCDNumberErrorMessage(String message);

    void setNameErrorEnabled(Boolean isEmpty);

    void setPhoneNumberlErrorEnabled(Boolean isEmpty);

    void setCCCDNumberlErrorEnabled(Boolean isEmpty);

    boolean saveContract();
}
