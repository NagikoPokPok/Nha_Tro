package edu.poly.nhtr.presenters;

import android.app.Dialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.utilities.Constants;

public class HomePresenter {
    private int position = 0;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private final HomeListener homeListener;


    public HomePresenter(HomeListener homeListener) {
        this.homeListener = homeListener;
    }



    public void addHome(Home home) {
        if (home.getNameHome().isEmpty()) {
            homeListener.showErrorMessage("Nhập tên nhà trọ", R.id.layout_name_home);
        } else if (home.getAddressHome().isEmpty()) {
            homeListener.showErrorMessage("Nhập địa chỉ nhà trọ", R.id.layout_address_home);
        } else {
            checkDuplicateData(home, () -> addHomeToFirestore(home));
        }
    }

    private Boolean isDuplicate(String fieldFromFirestore, String fieldFromHome, String userIdFromFirestore, Home home) {
        return fieldFromFirestore != null && fieldFromFirestore.equalsIgnoreCase(fieldFromHome) && userIdFromFirestore.equals(homeListener.getInfoUserFromGoogleAccount());
    }

    private void checkDuplicateData(Home home, Runnable onSuccess) {
        homeListener.showLoadingOfFunctions(R.id.btn_add_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                    String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                    String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                    if(isDuplicate(nameFromFirestore, home.getNameHome(), userIdFromFirestore, home) && isDuplicate(addressFromFirestore, home.getAddressHome(), userIdFromFirestore, home))
                    {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        return;
                    }
                    if (isDuplicate(nameFromFirestore, home.getNameHome(), userIdFromFirestore, home)) {
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        return;
                    }
                    if (isDuplicate(addressFromFirestore, home.getAddressHome(), userIdFromFirestore, home)) {
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        return;
                    }
                }
                onSuccess.run();
            } else {
                // Handle errors
            }
        });
    }

    private void addHomeToFirestore(Home home) {
        HashMap<String, Object> homeInfo = new HashMap<>();
        homeInfo.put(Constants.KEY_NAME_HOME, home.getNameHome());
        homeInfo.put(Constants.KEY_ADDRESS_HOME, home.getAddressHome());
        homeInfo.put(Constants.KEY_TIMESTAMP, FieldValue.serverTimestamp());
        homeInfo.put(Constants.KEY_NUMBER_OF_ROOMS, 0);
        homeInfo.put(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount());

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_HOMES)
                .add(homeInfo)
                .addOnSuccessListener(documentReference -> {
                    homeListener.putHomeInfoInPreferences(home.getNameHome(), home.getAddressHome(), documentReference);
                    homeListener.showToast("Thêm nhà trọ thành công");
                    getHomes("add");
                    homeListener.dialogClose();
                    homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                })
                .addOnFailureListener(e -> {
                    homeListener.showToast("Add failed");
                    homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                });
    }

    public void getHomes(String action) {
        homeListener.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = homeListener.getInfoUserFromGoogleAccount();

        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (homeListener.isAdded2()) {
                        homeListener.hideLoading();
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Home> homes = new ArrayList<>();
                            List<String> homeIds = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                homeIds.add(document.getId());
                            }

                            if (!homeIds.isEmpty()) {
                                database.collection(Constants.KEY_COLLECTION_ROOMS)
                                        .whereIn(Constants.KEY_HOME_ID, homeIds)
                                        .get()
                                        .addOnCompleteListener(roomTask -> {
                                            if (roomTask.isSuccessful() && roomTask.getResult() != null) {
                                                // Tạo Map để lưu trữ số lượng rooms cho từng home
                                                Map<String, Integer> homeRoomCount = new HashMap<>();
                                                for (QueryDocumentSnapshot roomDocument : roomTask.getResult()) {
                                                    String homeId = roomDocument.getString(Constants.KEY_HOME_ID);
                                                    homeRoomCount.put(homeId, homeRoomCount.getOrDefault(homeId, 0) + 1);
                                                }

                                                // Duyệt qua danh sách homes và cập nhật số lượng rooms từ Map
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Home home = new Home();
                                                    home.nameHome = document.getString(Constants.KEY_NAME_HOME);
                                                    home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
                                                    home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                                    home.idHome = document.getId();
                                                    home.numberOfRooms = homeRoomCount.getOrDefault(home.idHome, 0);
                                                    // Update the number of rooms in Firestore
                                                    document.getReference().update(Constants.KEY_NUMBER_OF_ROOMS, home.numberOfRooms);
                                                    homes.add(home);
                                                }

                                                // Sắp xếp các homes theo thứ tự từ thời gian khi theem vào
                                                homes.sort(Comparator.comparing(obj -> obj.dateObject));

                                                homeListener.addHome(homes, action);
                                            } else {
                                                homeListener.addHomeFailed();
                                            }
                                        });
                            } else {
                                homeListener.addHomeFailed();
                            }
                        } else {
                            homeListener.addHomeFailed();
                        }
                    }
                });
    }



    public void deleteHome(Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_delete_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Truy vấn để lấy tất cả các tài liệu có cùng userId
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount())
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

                            // Kiểm tra nếu homeId khớp
                            if (homeIdFromFirestore.equals(home.getIdHome())) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            // Sau khi tìm thấy vị trí, thực hiện xoá tài liệu
                            database.collection(Constants.KEY_COLLECTION_HOMES)
                                    .document(home.getIdHome())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        getHomes("delete");
                                        homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                                        homeListener.dialogClose();
                                        homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                                    })
                                    .addOnFailureListener(e -> {
                                        homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                                        homeListener.showToast("Xoá nhà trọ thất bại");
                                    });
                        } else {
                            homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                            homeListener.showToast("Không tìm thấy homeId");
                        }
                    } else {
                        homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                        homeListener.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }


    public void updateHome(String newNameHome, String newAddressHome, Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_add_home);
        if (newNameHome.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
            homeListener.showErrorMessage("Nhập tên nhà trọ", R.id.layout_name_home);
        } else if (newAddressHome.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
            homeListener.showErrorMessage("Nhập địa chỉ nhà trọ", R.id.layout_address_home);
        } else {
            checkDuplicateDataForUpdate(newNameHome, newAddressHome, home);
        }
    }

    private void checkDuplicateDataForUpdate(String newNameHome, String newAddressHome, Home home) {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                    String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                    String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                    String homeIdFromFirestore = document.getId();
                    if(isDuplicate(nameFromFirestore, newNameHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome()) && isDuplicate(addressFromFirestore, newAddressHome, userIdFromFirestore, home))
                    {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        return;
                    }

                    else if (isDuplicate(nameFromFirestore, newNameHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome())) {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        return;
                    }
                    else if (isDuplicate(addressFromFirestore, newAddressHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome())) {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        return;
                    }
                }
                homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                homeListener.openConfirmUpdateHome(Gravity.CENTER, newNameHome, newAddressHome, home);

            } else {
                // Handle errors
            }
        });
    }

    public void updateSuccess(String newNameHome, String newAddressHome, Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_confirm_update_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Truy vấn để lấy tất cả các tài liệu có cùng userId
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount())
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

                            // Kiểm tra nếu homeId khớp
                            if (homeIdFromFirestore.equals(home.getIdHome())) {
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            // Sau khi tìm thấy vị trí, thực hiện cập nhật tài liệu
                            HashMap<String, Object> updateInfo = new HashMap<>();
                            updateInfo.put(Constants.KEY_NAME_HOME, newNameHome);
                            updateInfo.put(Constants.KEY_ADDRESS_HOME, newAddressHome);

                            database.collection(Constants.KEY_COLLECTION_HOMES)
                                    .document(home.getIdHome())
                                    .update(updateInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        getHomes("update");
                                        //homeListener.showToast("Cập nhật thành công ở vị trí: " + getPosition());
                                        homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                                        homeListener.dialogClose();
                                        homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                                    })
                                    .addOnFailureListener(e -> {
                                        homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                                        homeListener.showToast("Cập nhật thông tin nhà trọ thất bại");
                                    });
                        } else {
                            homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                            homeListener.showToast("Không tìm thấy homeId");
                        }
                    } else {
                        homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                        homeListener.showToast("Lỗi khi lấy tài liệu: " + task.getException());
                    }
                });
    }



    public void searchHome(String nameHome)
    {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Home> homes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                            String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                            String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                            Long numberOfRooms = document.getLong(Constants.KEY_NUMBER_OF_ROOMS);

                            if (nameFromFirestore != null && nameFromFirestore.toLowerCase().contains(nameHome.toLowerCase()) && Objects.equals(userIdFromFirestore, homeListener.getInfoUserFromGoogleAccount())) {
                                Home home = new Home();
                                home.nameHome = nameFromFirestore;
                                home.addressHome = addressFromFirestore;
                                home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                home.idHome = document.getId();
                                home.numberOfRooms = numberOfRooms != null ? numberOfRooms.intValue() : 0;
                                homes.add(home);
                            }
                        }

                        if (!homes.isEmpty()) {
                            homeListener.addHome(homes, "search");
                        } else {
                            homeListener.noHomeData();
                        }
                    } else {
                        homeListener.showToast("Không thể tìm kiếm nhà trọ");
                    }
                });
    }

    public void deleteListHomes(List<Home> homesToDelete, ActionMode mode) {
        homeListener.showLoadingOfFunctions(R.id.btn_delete_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Bắt đầu một batch mới
        WriteBatch batch = database.batch();

        // Duyệt qua danh sách các home cần xóa và thêm thao tác xóa vào batch
        for (Home home : homesToDelete) {
            DocumentReference homeRef = database.collection(Constants.KEY_COLLECTION_HOMES).document(home.getIdHome());
            batch.delete(homeRef); // Thêm thao tác xóa vào batch
        }

        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    //homeListener.showToast("Xóa thành công " + homesToDelete.size() + " homes.");
                    getHomes("init");
                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                    homeListener.dialogAndModeClose(mode);
                    homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                })
                .addOnFailureListener(e -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                    homeListener.showToast("Xóa homes thất bại: " + e.getMessage());
                });
    }

    public void sortHomes(String typeOfSort)
    {
        homeListener.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = homeListener.getInfoUserFromGoogleAccount();

        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (homeListener.isAdded2()) {
                        homeListener.hideLoading();
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Home> homes = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Home home = new Home();
                                home.nameHome = document.getString(Constants.KEY_NAME_HOME);
                                home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
                                home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                home.idHome = document.getId();
                                home.numberOfRooms = Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS)).intValue(); // Chuyển đổi thành Integer
                                homes.add(home);
                            }
                            // Sắp xếp danh sách các nhà trọ dựa trên số lượng phòng
                            if(typeOfSort.equals("number_room_asc")){
                                homes = sortHomesByNumberOfHomesAscending(homes);
                            }else if(typeOfSort.equals("number_room_desc")){
                                homes = sortHomesByNumberOfHomesDescending(homes);
                            }
                            homeListener.addHome(homes, "init");
                        } else {
                            homeListener.addHomeFailed();
                        }
                    }
                });
    }

    private List<Home> sortHomesByNumberOfHomesAscending(List<Home> homes) {
        homes.sort(new Comparator<Home>() {
            @Override
            public int compare(Home home1, Home home2) {
                return Integer.compare(home1.numberOfRooms, home2.numberOfRooms);
            }
        });
        return homes;
    }

    private List<Home> sortHomesByNumberOfHomesDescending(List<Home> homes) {
        homes.sort(new Comparator<Home>() {
            @Override
            public int compare(Home home1, Home home2) {
                return Integer.compare(home2.numberOfRooms, home1.numberOfRooms);
            }
        });
        return homes;
    }


}
