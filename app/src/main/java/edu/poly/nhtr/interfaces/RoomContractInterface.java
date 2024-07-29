package edu.poly.nhtr.interfaces;

import edu.poly.nhtr.models.Room;

public interface RoomContractInterface {
    interface View {

        String getInfoHomeFromGoogleAccount();

        String getInfoRoomFromGoogleAccount();

        void onContractDeleted();

        void onContractPrinted();

        void showToast(String s);
    }

    interface Presenter {
        void deleteContract(Room room);

        void printContract(Room room);
    }
}
