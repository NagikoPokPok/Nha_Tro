package edu.poly.nhtr.listeners;

public interface RoomMakeBillListener {
    void showToast(String message);

    void makeBillSuccessfully();

    void refreshPlusOrMinusMoney();
}
