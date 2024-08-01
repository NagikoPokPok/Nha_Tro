package edu.poly.nhtr.presenters;

import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;

public class RoomPresenter {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
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
    public int getPosition() {
        return position;
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
        roomListener.showLoadingOfFunctions(R.id.btn_update_room);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ROOMS).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameRoomFromFirestore = document.getString(Constants.KEY_NAME_ROOM);
                    String homeIdFromFirestore = document.getString(Constants.KEY_HOME_ID);
                    if(isDuplicate(nameRoomFromFirestore, room.getNameRoom(), homeIdFromFirestore, room) )
                    {
                        roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
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
                    roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
                })
                .addOnFailureListener(e -> {
                    roomListener.showToast("Add failed");
                    roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
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
                            room.roomId = document.getId();
                            room.nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                            room.price = document.getString(Constants.KEY_PRICE);
                            room.describe = document.getString(Constants.KEY_DESCRIBE);
                            room.status = document.getString(Constants.KEY_STATUS_PAID);
                            room.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                            rooms.add(room);
                        }
                        if (!rooms.isEmpty()) {
                            updateInfoRooms(rooms, action);

                        } else {
                            roomListener.addRoomFailed();
                        }
                    } else {
                        roomListener.addRoomFailed();
                    }
                });
    }

    private void updateInfoRooms(List<Room> rooms, String action) {
        for (Room room : rooms) {
            getNameGuest(room.roomId, new OnGetInfoOfMainGuest() {
                @Override
                public void onComplete(MainGuest mainGuest) {
                    if (mainGuest != null) {
                        room.nameUser = mainGuest.getNameGuest();
                        room.phoneNumer = mainGuest.getPhoneGuest();
                    }
                    // Gọi addRoom hoặc addRoomFailed sau khi lấy thông tin cho tất cả các phòng
                    if (room.equals(rooms.get(rooms.size() - 1))) {
                        roomListener.addRoom(rooms, action);
                    }
                }
            });
        }


    }

    private void getStatusOFBill()
    {

    }
    private void getNameGuest(String roomID, OnGetInfoOfMainGuest listener) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        MainGuest mainGuest = new MainGuest();
                        mainGuest.setPhoneGuest(task.getResult().getDocuments().get(0).getString(Constants.KEY_GUEST_PHONE));
                        mainGuest.setNameGuest(task.getResult().getDocuments().get(0).getString(Constants.KEY_GUEST_NAME));
                        listener.onComplete(mainGuest);
                    } else {
                        listener.onComplete(null);
                        Log.e("GetNameGuest", "No contracts found or error occurred: ", task.getException());
                    }
                });
    }

    public interface OnGetInfoOfMainGuest {
        void onComplete(MainGuest mainGuest);
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
                            String priceRoomFromFirestore = document.getString(Constants.KEY_PRICE);
                            String describeRoomFromFirestore = document.getString(Constants.KEY_DESCRIBE);
                            String homeIdFromFirestore = document.getString(Constants.KEY_HOME_ID);

                            if (nameRoomFromFirestore != null && nameRoomFromFirestore.toLowerCase().contains(nameRoom.toLowerCase()) && Objects.equals(homeIdFromFirestore, roomListener.getInfoHomeFromGoogleAccount())) {
                                Room room = new Room();
                                room.nameRoom = nameRoomFromFirestore;
                                room.price = priceRoomFromFirestore;
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
    public void deleteRoom(Room room) {
        roomListener.showLoadingOfFunctions(R.id.btn_delete_room);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Truy vấn để lấy tất cả các tài liệu có cùng userId
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, roomListener.getInfoHomeFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        // Sắp xếp danh sách tài liệu theo thời gian tăng dần
                        documents.sort((doc1, doc2) -> {
                            Timestamp timestamp1 = doc1.getTimestamp(Constants.KEY_TIMESTAMP);
                            Timestamp timestamp2 = doc2.getTimestamp(Constants.KEY_TIMESTAMP);
                            assert timestamp1 != null;
                            assert timestamp2 != null;
                            return timestamp1.compareTo(timestamp2);
                        });

                        position = 0;
                        boolean found = false;

                        // Duyệt qua danh sách tài liệu đã sắp xếp
                        for (DocumentSnapshot document : documents) {
                            position++;
                            String homeIdFromFirestore = document.getId();

                            // Kiểm tra nếu roomId khớp
                            if (homeIdFromFirestore.equals(room.getRoomId())) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            // Sau khi tìm thấy vị trí, thực hiện xoá tài liệu
                            database.collection(Constants.KEY_COLLECTION_ROOMS)
                                    .document(room.getRoomId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        getRooms("delete");
                                        roomListener.hideLoadingOfFunctions(R.id.btn_delete_room);
                                        roomListener.dialogClose();
                                        roomListener.openDialogSuccess(R.layout.layout_dialog_delete_room_success);
                                    })
                                    .addOnFailureListener(e -> {
                                        roomListener.hideLoadingOfFunctions(R.id.btn_delete_room);
                                        roomListener.showToast("Xoá phòng trọ thất bại");
                                    });
                        } else {
                            roomListener.hideLoadingOfFunctions(R.id.btn_delete_room);
                            roomListener.showToast("Không tìm thấy homeId");
                        }
                    } else {
                        roomListener.hideLoadingOfFunctions(R.id.btn_delete_room);
                        roomListener.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }
    void check(String newNameRoom, String newPrice, String newDescribe){
        if(newNameRoom.equals(Constants.KEY_NAME_ROOM) && newPrice.equals(Constants.KEY_PRICE) && newDescribe.equals(Constants.KEY_DESCRIBE))
        {
            //
        }
    }
    public void updateRoom(String newNameRoom, String newPrice, String newDescribe, Room room) {
        roomListener.showLoadingOfFunctions(R.id.btn_update_room);
        if (newNameRoom.isEmpty()) {
            roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
            roomListener.showErrorMessage("Nhập tên phòng trọ", R.id.layout_name_room);
        } else if (newPrice.isEmpty()) {
            roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
            roomListener.showErrorMessage("Nhập giá phòng trọ", R.id.layout_price);
        } else {
            checkDuplicateDataForUpdate(newNameRoom, newPrice, newDescribe, room);
        }
    }
    private void checkDuplicateDataForUpdate(String newNameRoom, String newPrice, String newDescribe, Room room) {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, roomListener.getInfoHomeFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nameFromFirestore = document.getString(Constants.KEY_NAME_ROOM);
                            String homeIdFromFirestore = document.getString(Constants.KEY_HOME_ID);
                            String roomIdFromFirestore = document.getId();

                            if (isDuplicate(nameFromFirestore, newNameRoom, homeIdFromFirestore, room) && !roomIdFromFirestore.equals(room.getRoomId())) {
                                roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
                                roomListener.showErrorMessage("Tên phòng đã tồn tại", R.id.layout_name_room);
                                return;
                            }
                        }
                        roomListener.hideLoadingOfFunctions(R.id.btn_update_room);
                        roomListener.openConfirmUpdateRoom(Gravity.CENTER, newNameRoom, newPrice, newDescribe, room);

                    } else {
                        // Handle errors
                    }
                });
    }
    public void updateSuccess(String newNameRoom, String newPrice, String newDescribe, Room room) {
        roomListener.showLoadingOfFunctions(R.id.btn_confirm_update_room);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Truy vấn để lấy tất cả các tài liệu có cùng userId
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, roomListener.getInfoHomeFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        // Sắp xếp danh sách tài liệu theo thời gian tăng dần
                        documents.sort((doc1, doc2) -> {
                            Timestamp timestamp1 = doc1.getTimestamp(Constants.KEY_TIMESTAMP);
                            Timestamp timestamp2 = doc2.getTimestamp(Constants.KEY_TIMESTAMP);
                            assert timestamp1 != null;
                            assert timestamp2 != null;
                            return timestamp1.compareTo(timestamp2);
                        });

                        position = 0;
                        boolean found = false;

                        // Duyệt qua danh sách tài liệu đã sắp xếp
                        for (DocumentSnapshot document : documents) {
                            position++;
                            String roomIdFromFirestore = document.getId();

                            // Kiểm tra nếu homeId khớp
                            if (roomIdFromFirestore.equals(room.getRoomId())) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            // Sau khi tìm thấy vị trí, thực hiện cập nhật tài liệu
                            HashMap<String, Object> updateInfo = new HashMap<>();
                            updateInfo.put(Constants.KEY_NAME_ROOM, newNameRoom);
                            updateInfo.put(Constants.KEY_PRICE, newPrice);
                            updateInfo.put(Constants.KEY_DESCRIBE, newDescribe);

                            database.collection(Constants.KEY_COLLECTION_ROOMS)
                                    .document(room.getRoomId())
                                    .update(updateInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        getRooms("update");
                                        //homeListener.showToast("Cập nhật thành công ở vị trí: " + getPosition());
                                        roomListener.hideLoadingOfFunctions(R.id.btn_confirm_update_room);
                                        roomListener.dialogClose();
                                        roomListener.openDialogSuccess(R.layout.layout_dialog_update_room_success);
                                    })
                                    .addOnFailureListener(e -> {
                                        roomListener.hideLoadingOfFunctions(R.id.btn_confirm_update_room);
                                        roomListener.showToast("Cập nhật thông tin phòng trọ thất bại");
                                    });
                        } else {
                            roomListener.hideLoadingOfFunctions(R.id.btn_confirm_update_room);
                            roomListener.showToast("Không tìm thấy phòng");
                        }
                    } else {
                        roomListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                        roomListener.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }
    public void sortRooms(String typeOfSort) {
        roomListener.showLoadingOfFunctions(R.id.btn_confirm_apply);
        //homeListener.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String homeId = roomListener.getInfoHomeFromGoogleAccount();

        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (roomListener.isAdded2()) {
                        roomListener.hideLoading();
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Room> rooms = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Room room = new Room();
                                room.nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                room.nameUser = document.getString(Constants.KEY_NAME);
                                room.price = document.getString(Constants.KEY_PRICE);
                                room.status=document.getString(Constants.KEY_STATUS_PAID);
                                room.phoneNumer= document.getString(Constants.KEY_PHONE_NUMBER);
                                room.describe = document.getString(Constants.KEY_DESCRIBE);
                                room.numberOfMemberLiving = document.getString(Constants.KEY_NUMBER_OF_PEOPLE_LIVING);
                                room.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                room.roomId = document.getId();
                                rooms.add(room);
                            }
                            // Sắp xếp danh sách các nhà trọ dựa trên số lượng phòng
                            if (typeOfSort.equals("price_asc")) {
                                rooms = sortRoomByPriceAscending(rooms);
                            } else if (typeOfSort.equals("number_of_people_living_asc")) {
                                rooms = sortRoomByNumberOfMemberLivingAscending(rooms);
                            } else if (typeOfSort.equals("name_room")) {
                                rooms = sortByName(rooms);
                            }
                            roomListener.hideLoadingOfFunctions(R.id.btn_confirm_apply);
                            roomListener.dialogClose();
                            roomListener.addRoom(rooms, "sort");
                        } else {
                            roomListener.addRoomFailed();
                        }
                    }
                });
    }
    private List<Room> sortRoomByPriceAscending(List<Room> rooms) {
        rooms.sort((o1, o2) -> {
            String price1 = o1.price.replace(".", "");
            String price2 = o2.price.replace(".", "");

            int a = Integer.parseInt(price1);
            int b = Integer.parseInt(price2);

            return Integer.compare(a, b);
        });

        // Optional: Show a toast with the price of the first room
        // roomListener.showToast(rooms.get(0).price);

        return rooms;
    }

//    private List<Room> sortRoomByPriceAscending(List<Room> rooms) {
//        rooms.sort(Comparator.comparingInt(room -> Integer.parseInt(room.getPrice())));
//        return rooms;
//    }

    private List<Room> sortRoomByNumberOfMemberLivingAscending(List<Room> rooms) {
        rooms.sort(new Comparator<Room>() {
            @Override
            public int compare(Room o1, Room o2) {
                return Integer.compare(Integer.parseInt(o1.numberOfMemberLiving),Integer.parseInt(o2.numberOfMemberLiving));
            }
        });
        return rooms;
    }
    private List<Room> sortByName(List<Room> rooms) {
        rooms.sort(new Comparator<Room>() {
            @Override
            public int compare(Room o1, Room o2) {
                int a = o1.getNameRoom().compareToIgnoreCase(o2.getNameRoom());
                return a ;
            }
        });
        return rooms;
    }
    public void getListRooms(OnCompleteListener<Void> onCompleteListener) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String homeId = roomListener.getInfoHomeFromGoogleAccount();
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (roomListener.isAdded2()) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Room> rooms = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Room room = new Room();
                                room.nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                room.nameUser = document.getString(Constants.KEY_NAME);
                                room.price = document.getString(Constants.KEY_PRICE);
                                room.status=document.getString(Constants.KEY_STATUS_PAID);
                                room.phoneNumer= document.getString(Constants.KEY_PHONE_NUMBER);
                                room.describe = document.getString(Constants.KEY_DESCRIBE);
                                room.numberOfMemberLiving = document.getString(Constants.KEY_NUMBER_OF_PEOPLE_LIVING);
                                room.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                room.roomId = document.getId();
                                rooms.add(room);
                            }

                            roomListener.getListRooms(rooms);
                            onCompleteListener.onComplete(null);

                        } else {
                            roomListener.addRoomFailed();
                        }
                    }
                });


    }
    public void deleteListRooms(List<Room> roomsToDelete) {
        if(roomsToDelete.isEmpty()){
            roomListener.showToast("123456");
        }
        else{
            roomListener.showToast("qwerty");
        roomListener.showLoadingOfFunctions(R.id.btn_delete_room);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Bắt đầu một batch mới
        WriteBatch batch = database.batch();

        // Duyệt qua danh sách các home cần xóa và thêm thao tác xóa vào batch
        for (Room room : roomsToDelete) {
            DocumentReference roomRef = database.collection(Constants.KEY_COLLECTION_ROOMS).document(room.getRoomId());
            batch.delete(roomRef); // Thêm thao tác xóa vào batch
        }

        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {

                    //homeListener.showToast("Xóa thành công " + homesToDelete.size() + " homes.");
                    getRooms("init");
                    roomListener.hideLoadingOfFunctions(R.id.btn_delete_room);
                    //roomListener.dialogAndModeClose(mode);
                    roomListener.openDialogSuccess(R.layout.layout_dialog_delete_room_success);
                })
                .addOnFailureListener(e -> {
                    roomListener.hideLoadingOfFunctions(R.id.btn_delete_room);
                    roomListener.showToast("Xóa phòng thất bại: " + e.getMessage());
                });
    }}
    public void filterRoom(List<Room> rooms) {
        if (rooms.isEmpty()) {
            roomListener.hideLoadingOfFunctions(R.id.btn_confirm_apply);
            roomListener.dialogClose();
            roomListener.noRoomData();
        } else {
            roomListener.hideLoadingOfFunctions(R.id.btn_confirm_apply);
            roomListener.dialogClose();
            roomListener.addRoom(rooms, "init");
        }
    }

}
