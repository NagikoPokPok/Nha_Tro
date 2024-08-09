package edu.poly.nhtr.listeners;

import android.view.ActionMode;
import android.view.View;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.User;

public interface RoomListener {
    void openPopup(View view, Room room, ItemContainerRoomBinding binding);
    void showErrorMessage(String message, int id);
    void showToast(String message);
    String getInfoHomeFromGoogleAccount();
    void putRoomInfoInPreferences(String nameRoom, String priceRoom, String describeRoom, DocumentReference documentReference);
    void dialogClose();
    void hideLoading();
    void showLoading();
    boolean isAdded2();
    void hideFrameTop();
    void showFrameTop();
    void addRoom(List<Room> rooms, String action);
    void addRoomFailed();
    void openDialogSuccess(int layout);
    void showLoadingOfFunctions(int id);
    void hideLoadingOfFunctions(int id);
    void openConfirmUpdateRoom(int gravity, String newNameRoom, String newPrice, String newDescribe, Room room);


    // Xử lí click vào container Room
    void onRoomClick(Room room);

    void getListRooms(List<Room> listRoom);
    void noRoomData();
    void setDelectAllUI();
    void cancelDelectAll();
    void openDeleteListDialog(List<Room> listRoom);
    void deleteListAll(List<Room> list);
    void dialogAndModeClose(ActionMode mode);
    void openDiaLogCannotDelete();
}
