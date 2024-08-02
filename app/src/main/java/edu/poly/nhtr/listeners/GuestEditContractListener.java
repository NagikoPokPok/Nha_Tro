package edu.poly.nhtr.listeners;

import edu.poly.nhtr.models.MainGuest;

public interface GuestEditContractListener {

    void displayContractData(MainGuest mainGuest);
    void showToast(String message);

    void saveSuccessfully();
    String getInfoHomeFromGoogleAccount();
}
