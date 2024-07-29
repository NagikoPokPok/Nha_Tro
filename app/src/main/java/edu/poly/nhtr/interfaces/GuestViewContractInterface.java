package edu.poly.nhtr.interfaces;

import edu.poly.nhtr.models.MainGuest;

public interface GuestViewContractInterface {
    void displayContractData(MainGuest mainGuest);
    void showToast(String message);
}

