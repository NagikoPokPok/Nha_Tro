package edu.poly.nhtr.presenters;

import android.view.ActionMode;
import android.view.Gravity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.RoomBill;
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
                    if (isDuplicate(nameFromFirestore, home.getNameHome(), userIdFromFirestore, home) && isDuplicate(addressFromFirestore, home.getAddressHome(), userIdFromFirestore, home)) {
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

//    public void getHomes(String action) {
//        homeListener.showLoading();
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        String userId = homeListener.getInfoUserFromGoogleAccount();
//
//        database.collection(Constants.KEY_COLLECTION_HOMES)
//                .whereEqualTo(Constants.KEY_USER_ID, userId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (homeListener.isAdded2()) {
//                        homeListener.hideLoading();
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            List<Home> homes = new ArrayList<>();
//                            List<String> homeIds = new ArrayList<>();
//
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                homeIds.add(document.getId());
//                            }
//
//                            if (!homeIds.isEmpty()) {
//                                database.collection(Constants.KEY_COLLECTION_ROOMS)
//                                        .whereIn(Constants.KEY_HOME_ID, homeIds)
//                                        .get()
//                                        .addOnCompleteListener(roomTask -> {
//                                            if (roomTask.isSuccessful() && roomTask.getResult() != null) {
//                                                // Tạo Map để lưu trữ số lượng rooms cho từng home
//                                                Map<String, Integer> homeRoomCount = new HashMap<>();
//                                                for (QueryDocumentSnapshot roomDocument : roomTask.getResult()) {
//                                                    String homeId = roomDocument.getString(Constants.KEY_HOME_ID);
//                                                    homeRoomCount.put(homeId, homeRoomCount.getOrDefault(homeId, 0) + 1);
//                                                }
//
//                                                // Duyệt qua danh sách homes và cập nhật số lượng rooms từ Map
//                                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                                    Home home = new Home();
//                                                    home.nameHome = document.getString(Constants.KEY_NAME_HOME);
//                                                    home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
//                                                    home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
//                                                    home.idHome = document.getId();
//                                                    home.numberOfRooms = homeRoomCount.getOrDefault(home.idHome, 0);
//                                                    // Update the number of rooms in Firestore
//                                                    document.getReference().update(Constants.KEY_NUMBER_OF_ROOMS, home.numberOfRooms);
//                                                    homes.add(home);
//                                                }
//
//                                                // Sắp xếp các homes theo thứ tự từ thời gian khi theem vào
//                                                homes.sort(Comparator.comparing(obj -> obj.dateObject));
//
//                                                homeListener.addHome(homes, action);
//                                            } else {
//                                                homeListener.addHomeFailed();
//                                            }
//                                        });
//                            } else {
//                                homeListener.addHomeFailed();
//                            }
//                        } else {
//                            homeListener.addHomeFailed();
//                        }
//                    }
//                });
//    }

    public void getHomes(String action) {
        homeListener.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = homeListener.getInfoUserFromGoogleAccount();

        // Lấy danh sách homes từ Firestore
        getHomesFromDatabase(database, userId, action);

    }

    private void getHomesFromDatabase(FirebaseFirestore database, String userId, String action) {
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (homeListener.isAdded2()) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<String> homeIds = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                homeIds.add(document.getId());
                            }
                            if (!homeIds.isEmpty()) {
                                getRoomsForHomes(database, homeIds, task, action);
                            } else {
                                homeListener.addHomeFailed();
                            }
                        } else {
                            homeListener.addHomeFailed();
                        }
                    }
                });
    }

    private void getRoomsForHomes(FirebaseFirestore database, List<String> homeIds, Task<QuerySnapshot> homesTask, String action) {
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereIn(Constants.KEY_HOME_ID, homeIds)
                .get()
                .addOnCompleteListener(roomTask -> {
                    if (roomTask.isSuccessful() && roomTask.getResult() != null) {
                        Map<String, Integer> homeRoomCount = countRoomsForHomes(roomTask.getResult());
                        countRoomsAreAvailableForHomes(roomTask.getResult(), countRoomsAreAvailable -> {
                            countRoomsAreDelayedPayBillForHome(roomTask.getResult(), countRoomsAreDelayedPayBill -> {
                                revenueOfMonthForHome(roomTask.getResult(), revenueOfMonth -> {
                                    List<Home> homes = createHomeList(homesTask.getResult(), homeRoomCount, countRoomsAreAvailable, countRoomsAreDelayedPayBill, revenueOfMonth);
                                    updateHomesInDatabase(homesTask.getResult(), homeRoomCount, countRoomsAreAvailable, countRoomsAreDelayedPayBill, revenueOfMonth);
                                    homes.sort(Comparator.comparing(home -> home.dateObject));
                                    homeListener.hideLoading();
                                    homeListener.addHome(homes, action);
                                });
                            });
                        });
                    } else {
                        homeListener.addHomeFailed();
                    }
                });
    }

    private void countRoomsAreDelayedPayBillForHome(QuerySnapshot result, OnCountRoomsAreDelayedPayBillListener listener) {
        Map<String, Integer> count = new HashMap<>();
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (QueryDocumentSnapshot roomDocument : result) {
            String roomId = roomDocument.getId();
            String homeId = roomDocument.getString(Constants.KEY_HOME_ID);

            Task<QuerySnapshot> task = db.collection(Constants.KEY_COLLECTION_BILL)
                    .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                    .get()
                    .addOnCompleteListener(contractTask -> {
                        if (contractTask.isSuccessful() && contractTask.getResult() != null) {
                            List<DocumentSnapshot> documents = contractTask.getResult().getDocuments();
                            if (!documents.isEmpty()) {
                                // Tìm hóa đơn gần nhất
                                RoomBill latestBill = null;
                                for (DocumentSnapshot document : documents) {
                                    RoomBill bill = new RoomBill();
                                    bill.month = Objects.requireNonNull(document.getLong(Constants.KEY_MONTH)).intValue();
                                    bill.year = Objects.requireNonNull(document.getLong(Constants.KEY_YEAR)).intValue();
                                    bill.isPayedBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_PAYED_BILL));

                                    // Cập nhật hóa đơn gần nhất
                                    if (latestBill == null || bill.getYear() > latestBill.getYear() ||
                                            (bill.getYear() == latestBill.getYear() && bill.getMonth() > latestBill.getMonth())) {
                                        latestBill = bill;
                                    }
                                }

                                // Kiểm tra nếu hóa đơn gần nhất bị trễ
                                if (latestBill.isDelayPayBill()) {
                                    count.put(homeId, count.getOrDefault(homeId, 0) + 1);
                                }
                            }
                        }
                    });

            tasks.add(task);
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(allTasks -> listener.onCountRoomsAreDelayedPayBillComplete(count));
    }

    public interface OnCountRoomsAreDelayedPayBillListener {
        void onCountRoomsAreDelayedPayBillComplete(Map<String, Integer> count);
    }

    private void revenueOfMonthForHome(QuerySnapshot result, OnGetRevenueOfMonthListener listener) {
        Map<String, Long> sum = new HashMap<>();
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (QueryDocumentSnapshot roomDocument : result) {
            String roomId = roomDocument.getId();
            String homeId = roomDocument.getString(Constants.KEY_HOME_ID);

            Task<QuerySnapshot> task = db.collection(Constants.KEY_COLLECTION_BILL)
                    .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                    .get()
                    .addOnCompleteListener(contractTask -> {
                        if (contractTask.isSuccessful() && contractTask.getResult() != null) {
                            List<DocumentSnapshot> documents = contractTask.getResult().getDocuments();
                            if (!documents.isEmpty()) {
                                // Tìm hóa đơn gần nhất
                                RoomBill latestBill = null;
                                for (DocumentSnapshot document : documents) {
                                    RoomBill bill = new RoomBill();
                                    bill.month = Objects.requireNonNull(document.getLong(Constants.KEY_MONTH)).intValue();
                                    bill.year = Objects.requireNonNull(document.getLong(Constants.KEY_YEAR)).intValue();
                                    bill.isPayedBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_PAYED_BILL));
                                    bill.totalOfMoney = Objects.requireNonNull(document.getLong(Constants.KEY_TOTAL_OF_MONEY)).intValue();

                                    // Cập nhật hóa đơn gần nhất
                                    if (latestBill == null || bill.getYear() > latestBill.getYear() ||
                                            (bill.getYear() == latestBill.getYear() && bill.getMonth() > latestBill.getMonth())) {
                                        latestBill = bill;
                                    }
                                }

                                // Cộng tổng doanh thu cho hóa đơn gần nhất nếu nó đã được thanh toán
                                if (latestBill != null && latestBill.isPayedBill()) {
                                    long currentSum = sum.getOrDefault(homeId, 0L);
                                    sum.put(homeId, currentSum + latestBill.totalOfMoney);
                                }
                            }
                        }
                    });

            tasks.add(task);
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(allTasks -> listener.onGetRevenueOfMonthComplete(sum));
    }

    public interface OnGetRevenueOfMonthListener {
        void onGetRevenueOfMonthComplete(Map<String, Long> sum);
    }


    private void countRoomsAreAvailableForHomes(QuerySnapshot result, OnCountRoomsAvailableListener listener) {
        Map<String, Integer> count = new HashMap<>();
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (QueryDocumentSnapshot roomDocument : result) {
            String roomId = roomDocument.getId();
            String homeId = roomDocument.getString(Constants.KEY_HOME_ID);

            Task<QuerySnapshot> task = db.collection(Constants.KEY_COLLECTION_CONTRACTS)
                    .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                    .get()
                    .addOnCompleteListener(contractTask -> {
                        if (!contractTask.isSuccessful() || contractTask.getResult() == null || contractTask.getResult().isEmpty()) {
                            // If the contract task is not successful, or there are no contracts, increment the count for the corresponding homeId
                            count.put(homeId, count.getOrDefault(homeId, 0) + 1);
                        }
                    });

            tasks.add(task);
        }

        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(allTasks -> listener.onCountRoomsAvailableComplete(count));
    }

    // Interface to listen for room count results
    public interface OnCountRoomsAvailableListener {
        void onCountRoomsAvailableComplete(Map<String, Integer> count);
    }

    private Map<String, Integer> countRoomsForHomes(QuerySnapshot roomResult) {
        Map<String, Integer> homeRoomCount = new HashMap<>();
        for (QueryDocumentSnapshot roomDocument : roomResult) {
            String homeId = roomDocument.getString(Constants.KEY_HOME_ID);
            homeRoomCount.put(homeId, homeRoomCount.getOrDefault(homeId, 0) + 1);
        }
        return homeRoomCount;
    }

    private List<Home> createHomeList(QuerySnapshot homesResult, Map<String, Integer> homeRoomCount, Map<String, Integer> roomsAreAvailable, Map<String, Integer> roomsAreDelayedPayBill, Map<String, Long> revenue) {
        List<Home> homes = new ArrayList<>();
        for (QueryDocumentSnapshot document : homesResult) {
            Home home = new Home();
            home.nameHome = document.getString(Constants.KEY_NAME_HOME);
            home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
            home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
            home.idHome = document.getId();
            home.numberOfRooms = homeRoomCount.getOrDefault(home.idHome, 0);
            home.numberOfRoomsAvailable = roomsAreAvailable.getOrDefault(home.idHome, 0);
            home.numberOfRoomsAreDelayedPayBill = roomsAreDelayedPayBill.getOrDefault(home.idHome, 0);
            home.revenueOfMonth = revenue.getOrDefault(home.idHome, 0L);
            homes.add(home);
        }
        return homes;
    }


    private void updateHomesInDatabase(QuerySnapshot homesResult, Map<String, Integer> homeRoomCount, Map<String, Integer> roomsAreAvailable, Map<String, Integer> roomsAreDelayedPayBill, Map<String, Long> revenue) {
        for (QueryDocumentSnapshot document : homesResult) {
            String homeId = document.getId();
            int numberOfRooms = homeRoomCount.getOrDefault(homeId, 0);
            int numberOfRoomsAvailable = roomsAreAvailable.getOrDefault(homeId, 0);
            int numberOfRoomsAreDelayedPayBill = roomsAreDelayedPayBill.getOrDefault(homeId, 0);
            long revenueOfMonth = revenue.getOrDefault(homeId, 0L);
            Map<String, Object> updates = new HashMap<>();
            updates.put(Constants.KEY_NUMBER_OF_ROOMS, numberOfRooms);
            updates.put(Constants.KEY_NUMBER_OF_ROOMS_AVAILABLE, numberOfRoomsAvailable);
            updates.put(Constants.KEY_NUMBER_OF_ROOMS_ARE_DELAYED_PAY_BILL, numberOfRoomsAreDelayedPayBill);
            updates.put(Constants.KEY_REVENUE_OF_MONTH_FOR_HOME, revenueOfMonth);

            document.getReference().update(updates);
        }
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
                                        deleteListRoomsByHome(home.getIdHome(), new onCompleteDeleteListRooms() {
                                            @Override
                                            public void onComplete(boolean success) {
                                                if(success) {
                                                    getHomes("delete");
                                                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                                                    homeListener.dialogClose();
                                                    homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                                                }
                                            }
                                        });

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

    private void deleteListRoomsByHome(String homeID, onCompleteDeleteListRooms listener) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            WriteBatch batch = database.batch();
                            for (DocumentSnapshot document : task.getResult()) {
                                batch.delete(document.getReference());
                            }
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> commitTask) {
                                    if (commitTask.isSuccessful()) {
                                        listener.onComplete(true);
                                    } else {
                                        listener.onComplete(false);
                                    }
                                }
                            });
                        } else {
                            listener.onComplete(false);
                        }
                    }
                });
    }

    // Interface for completion callback
    public interface onCompleteDeleteListRooms {
        void onComplete(boolean success);
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
                            if (isDuplicate(nameFromFirestore, newNameHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome()) && isDuplicate(addressFromFirestore, newAddressHome, userIdFromFirestore, home)) {
                                homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                                homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                                homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                                return;
                            } else if (isDuplicate(nameFromFirestore, newNameHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome())) {
                                homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                                homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                                return;
                            } else if (isDuplicate(addressFromFirestore, newAddressHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome())) {
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
                                        homeListener.openDialogSuccess(R.layout.layout_dialog_update_success);
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


    public void searchHome(String nameHome) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Home> homes = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                                String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                                String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                                int numberOfRooms = document.getLong(Constants.KEY_NUMBER_OF_ROOMS) != null ? Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS)).intValue() : 0;
                                int numberOfRoomsAvailable = document.getLong(Constants.KEY_NUMBER_OF_ROOMS_AVAILABLE) != null ? Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS_AVAILABLE)).intValue() : 0;
                                int numberOfRoomsAreDelayedPayBill = document.getLong(Constants.KEY_NUMBER_OF_ROOMS_ARE_DELAYED_PAY_BILL) != null ? Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS_ARE_DELAYED_PAY_BILL)).intValue() : 0;
                                long revenueOfMonth = document.getLong(Constants.KEY_REVENUE_OF_MONTH_FOR_HOME) != null ? Objects.requireNonNull(document.getLong(Constants.KEY_REVENUE_OF_MONTH_FOR_HOME)) : 0L;

                                if (nameFromFirestore != null && nameFromFirestore.toLowerCase().contains(nameHome.toLowerCase()) && Objects.equals(userIdFromFirestore, homeListener.getInfoUserFromGoogleAccount())) {
                                    Home home = new Home();
                                    home.nameHome = nameFromFirestore;
                                    home.addressHome = addressFromFirestore;
                                    home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                    home.idHome = document.getId();
                                    home.numberOfRooms = numberOfRooms;
                                    home.numberOfRoomsAvailable = numberOfRoomsAvailable;
                                    home.numberOfRoomsAreDelayedPayBill = numberOfRoomsAreDelayedPayBill;
                                    home.revenueOfMonth = revenueOfMonth;

                                    homes.add(home);
                                }
                            } catch (NullPointerException e) {
                                // Xử lý lỗi nếu có bất kỳ trường nào là null
                                e.printStackTrace();
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


    public void deleteListHomes(List<Home> homesToDelete) {
        homeListener.showLoadingOfFunctions(R.id.btn_delete_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Bắt đầu một batch mới
        WriteBatch batch = database.batch();

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Duyệt qua danh sách các home cần xóa
        for (Home home : homesToDelete) {
            String homeId = home.getIdHome();
            DocumentReference homeRef = database.collection(Constants.KEY_COLLECTION_HOMES).document(homeId);

            // Lấy danh sách các phòng liên quan đến home này
            Task<QuerySnapshot> task = database.collection(Constants.KEY_COLLECTION_ROOMS)
                    .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                    .get()
                    .addOnCompleteListener(roomTask -> {
                        if (roomTask.isSuccessful()) {
                            for (DocumentSnapshot document : roomTask.getResult()) {
                                batch.delete(document.getReference()); // Thêm thao tác xóa phòng vào batch
                            }
                            // Thêm thao tác xóa home vào batch
                            batch.delete(homeRef);
                        } else {
                            homeListener.showToast("Lỗi khi lấy danh sách phòng: " + roomTask.getException().getMessage());
                        }
                    });

            tasks.add(task);
        }

        // Đợi tất cả các tác vụ lấy phòng hoàn thành trước khi commit batch
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        getHomes("init");
                        homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                        homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                        homeListener.hideLayoutDeleteHomes();
                    })
                    .addOnFailureListener(e -> {
                        homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                        homeListener.showToast("Xóa homes thất bại: " + e.getMessage());
                    });
        });
    }



    public void sortHomes(String typeOfSort) {
        homeListener.showLoadingOfFunctions(R.id.btn_confirm_apply);
        //homeListener.showLoading();
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
                                home.numberOfRoomsAvailable = Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS_AVAILABLE)).intValue();
                                home.numberOfRoomsAreDelayedPayBill = Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_ROOMS_ARE_DELAYED_PAY_BILL)).intValue();
                                home.revenueOfMonth = Objects.requireNonNull(document.getLong(Constants.KEY_REVENUE_OF_MONTH_FOR_HOME)).intValue();
                                homes.add(home);
                            }
                            // Sắp xếp danh sách các nhà trọ dựa trên số lượng phòng
                            if (typeOfSort.equals("number_room_asc")) {
                                homes = sortHomesByNumberOfHomesAscending(homes);
                            } else if (typeOfSort.equals("number_room_desc")) {
                                homes = sortHomesByNumberOfHomesDescending(homes);
                            } else if (typeOfSort.equals("revenue_asc")) {
                                homes = sortHomesByRevenueAscending(homes);
                            } else if (typeOfSort.equals("revenue_desc")) {
                                homes = sortHomesByRevenueDescending(homes);
                            }
                            homeListener.hideLoadingOfFunctions(R.id.btn_confirm_apply);
                            homeListener.dialogClose();
                            homeListener.addHome(homes, "sort");
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

    private List<Home> sortHomesByRevenueAscending(List<Home> homes) {
        homes.sort(new Comparator<Home>() {
            @Override
            public int compare(Home home1, Home home2) {
                return Long.compare(home1.revenueOfMonth, home2.revenueOfMonth);
            }
        });
        return homes;
    }

    private List<Home> sortHomesByNumberOfHomesDescending(List<Home> homes) {
        homes.sort(new Comparator<Home>() {
            @Override
            public int compare(Home home1, Home home2) {
                int a = Integer.compare(home2.numberOfRooms, home1.numberOfRooms);
                return a;
            }
        });
        return homes;
    }

    private List<Home> sortHomesByRevenueDescending(List<Home> homes) {
        homes.sort(new Comparator<Home>() {
            @Override
            public int compare(Home home1, Home home2) {
                return Long.compare(home2.revenueOfMonth, home1.revenueOfMonth);
            }
        });
        return homes;
    }

    public void filterHome(List<Home> homes) {
        if (homes.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_confirm_apply);
            homeListener.dialogClose();
            homeListener.noHomeData();
        } else {
            homeListener.hideLoadingOfFunctions(R.id.btn_confirm_apply);
            homeListener.dialogClose();
            homeListener.addHome(homes, "init");
        }
    }

    public void getListHomes(OnGetHomesCompleteListener listener) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = homeListener.getInfoUserFromGoogleAccount();


        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (homeListener.isAdded2()) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Home> homes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Home home = new Home();
                                home.nameHome = document.getString(Constants.KEY_NAME_HOME);
                                home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
                                home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                home.idHome = document.getId();

                                // Chuyển đổi từ Long thành Integer cho numberOfRooms
                                Long numberOfRoomsLong = document.getLong(Constants.KEY_NUMBER_OF_ROOMS);
                                home.numberOfRooms = numberOfRoomsLong != null ? numberOfRoomsLong.intValue() : 0;

                                // Chuyển đổi từ Long thành Integer cho numberOfRoomsAvailable
                                Long numberOfRoomsAvailableLong = document.getLong(Constants.KEY_NUMBER_OF_ROOMS_AVAILABLE);
                                home.numberOfRoomsAvailable = numberOfRoomsAvailableLong != null ? numberOfRoomsAvailableLong.intValue() : 0;

                                // Chuyển đổi từ Long thành Integer cho numberOfRoomsAreDelayedPayBill
                                Long numberOfRoomsAreDelayedPayBillLong = document.getLong(Constants.KEY_NUMBER_OF_ROOMS_ARE_DELAYED_PAY_BILL);
                                home.numberOfRoomsAreDelayedPayBill = numberOfRoomsAreDelayedPayBillLong != null ? numberOfRoomsAreDelayedPayBillLong.intValue() : 0;

                                // Chuyển đổi từ Long thành Integer cho revenueOfMonth
                                Long revenueOfMonthLong = document.getLong(Constants.KEY_REVENUE_OF_MONTH_FOR_HOME);
                                home.revenueOfMonth = revenueOfMonthLong != null ? revenueOfMonthLong.intValue() : 0;

                                homes.add(home);
                            }

                            // Gọi phương thức getListHomes với danh sách homes đã lấy được
                            homeListener.getListHomes(homes);
                            listener.onComplete(homes);
                        } else {
                            // Xử lý khi truy vấn không thành công hoặc kết quả là null
                            homeListener.addHomeFailed();
                        }
                    }
                });


    }

    public interface OnGetHomesCompleteListener {
        void onComplete(List<Home> homeList);
    }

}
