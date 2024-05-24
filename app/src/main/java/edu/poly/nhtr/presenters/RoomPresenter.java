package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;

public class RoomPresenter {
    private int position = 0;

    private final RoomListener roomListener;
    private int count;
    public RoomPresenter(RoomListener roomListener) {
        this.roomListener = roomListener;
    }
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addRoom(Room room) {
        if (room.getNameRoom().isEmpty()) {
            roomListener.showErrorMessage("Nhập tên phòng trọ", R.id.layout_name_room);
        } else if (room.getPrice().isEmpty()) {
            roomListener.showErrorMessage("Nhập giá phòng trọ", R.id.layout_price);
        } else {
            checkDuplicateData(room, () -> addRoomToFirestore(room));
        }
    }
    private void checkDuplicateData(Room room, Runnable onSuccess) {
        roomListener.showLoadingOfFunctions(R.id.btn_add_room);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ROOMS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameRoomFromFirestore = document.getString(Constants.KEY_NAME_ROOM);
                    String homeIdFromFirestore = document.getString(Constants.KEY_HOME_ID);
                    if(isDuplicate(nameRoomFromFirestore, room.getNameRoom(), homeIdFromFirestore, room) )
                    {
                        roomListener.hideLoadingOfFunctions(R.id.btn_add_room);
                        roomListener.showErrorMessage("Tên phòng đã tồn tại", R.id.layout_name_room);

                        return;
                    }
                }
                onSuccess.run();
            } else {
                // Handle errors
            }
        });
    }


    private void addRoomToFirestore(Room room) {
        HashMap<String, Object> roomInfo = new HashMap<>();
        roomInfo.put(Constants.KEY_NAME_ROOM, room.getNameRoom());
        roomInfo.put(Constants.KEY_PRICE, room.getPrice());
        roomInfo.put(Constants.KEY_DESCRIBE, room.getDescribe());
        roomInfo.put(Constants.KEY_TIMESTAMP, new Date());
        roomInfo.put(Constants.KEY_HOME_ID, roomListener.getInfoHomeFromGoogleAccount());

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOMS)
                .add(roomInfo)
                .addOnSuccessListener(documentReference -> {
                    roomListener.putRoomInfoInPreferences(room.getNameRoom(), room.getPrice(), room.getDescribe(), documentReference);
                    roomListener.showToast("Thêm nhà trọ thành công");
                    getRooms("add");
                    roomListener.dialogClose();
                    roomListener.hideLoadingOfFunctions(R.id.btn_add_room);
                })
                .addOnFailureListener(e -> {
                    roomListener.showToast("Add failed");
                    roomListener.hideLoadingOfFunctions(R.id.btn_add_room);
                });
    }
    public void getRooms(String action) {
        roomListener.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, roomListener.getInfoHomeFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {

                        roomListener.hideLoading();
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Room> rooms = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Room room = new Room();
                                room.nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                room.price = document.getString(Constants.KEY_PRICE);
                                room.describe = document.getString(Constants.KEY_DESCRIBE);
                                room.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                room.roomId = document.getId();
                                rooms.add(room);
                            }
                            if (!rooms.isEmpty()) {
                                roomListener.addRoom(rooms, action);
                            } else {
                                roomListener.addRoomFailed();
                            }
                        } else {
                            roomListener.addRoomFailed();
                        }

                });
    }

    private Boolean isDuplicate(String fieldFromFirestore, String fieldFromRoom, String homeIdFromFirestore, Room room) {
        return fieldFromFirestore != null && fieldFromFirestore.equalsIgnoreCase(fieldFromRoom) && homeIdFromFirestore.equals(roomListener.getInfoHomeFromGoogleAccount());
    }

    public void searchRoom(String nameRoom)
    {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Room> rooms = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nameRoomFromFirestore = document.getString(Constants.KEY_NAME_ROOM);
                            String prriceRoomFromFirestore = document.getString(Constants.KEY_PRICE);
                            String describeRoomFromFirestore = document.getString(Constants.KEY_DESCRIBE);
                            String homeIdFromFirestore = document.getString(Constants.KEY_HOME_ID);

                            if (nameRoomFromFirestore != null && nameRoomFromFirestore.toLowerCase().contains(nameRoom.toLowerCase()) && Objects.equals(homeIdFromFirestore, roomListener.getInfoHomeFromGoogleAccount())) {
                                Room room = new Room();
                                room.nameRoom = nameRoomFromFirestore;
                                room.price = prriceRoomFromFirestore;
                                room.describe = describeRoomFromFirestore;
                                room.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                room.roomId = document.getId();
                                rooms.add(room);
                            }
                        }

                        if (!rooms.isEmpty()) {
                            roomListener.addRoom(rooms, "search");
                        } else {
                            roomListener.addRoomFailed();
                        }
                    } else {
                        roomListener.showToast("Không thể tìm kiếm phòng trọ");
                    }
                });
    }

}
