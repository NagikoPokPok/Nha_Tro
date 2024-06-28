package edu.poly.nhtr.presenters;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.utilities.Constants;

public class IndexPresenter {

    private final IndexInterface indexInterface;
    private String homeID;

    public IndexPresenter(IndexInterface indexInterface, String homeID) {
        this.indexInterface = indexInterface;
        this.homeID = homeID;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void fetchRoomsAndAddIndex(String homeId, OnCompleteListener<Void> onCompleteListener) {
        indexInterface.showLoading();
        db.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<DocumentSnapshot> rooms = querySnapshot.getDocuments();
                            int[] pendingTasks = {rooms.size()}; // Biến đếm số tác vụ còn lại
                            for (DocumentSnapshot roomDoc : rooms) {
                                String roomId = roomDoc.getId();
                                String roomName = roomDoc.getString(Constants.KEY_NAME_ROOM);
                                Date dateCreateRoom = roomDoc.getDate(Constants.KEY_TIMESTAMP);
                                // Thêm index cho room này
                                checkAndAddIndexToRoom(homeId, roomId, roomName, dateCreateRoom, () -> {
                                    // Giảm biến đếm và kiểm tra nếu tất cả các tác vụ đã hoàn thành
                                    pendingTasks[0]--;
                                    if (pendingTasks[0] == 0) {
                                        onCompleteListener.onComplete(null);
                                    }
                                });
                            }
                        } else {
                            Log.w("Firestore", "No rooms found for the given homeId.");
                            onCompleteListener.onComplete(null);
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        onCompleteListener.onComplete(null);
                    }
                });
    }

    private void checkAndAddIndexToRoom(String homeId, String roomId, String roomName, Date dateRoom, Runnable onComplete) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        String indexID = roomId + "_" + year + "_" + month;

        db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && !documentSnapshot.exists()) {
                            addIndexToRoom(homeId, roomId, roomName, indexID, year, month, dateRoom, onComplete);
                        } else {
                            Log.d("Firestore", "Index already exists for room: " + roomId);
                            onComplete.run();
                        }
                    } else {
                        Log.w("Firestore", "Error checking index for room: " + roomId, task.getException());
                        onComplete.run();
                    }
                });
    }

    public void addIndexToRoom(String homeId, String roomId, String roomName, String indexID, int year, int month, Date date, Runnable onComplete) {
        Map<String, Object> indexData = new HashMap<>();
        indexData.put(Constants.KEY_HOME_ID, homeId);
        indexData.put(Constants.KEY_ROOM_ID, roomId);
        indexData.put(Constants.KEY_NAME_ROOM, roomName);
        indexData.put(Constants.KEY_MONTH, month);
        indexData.put(Constants.KEY_YEAR, year);
        indexData.put(Constants.KEY_ELECTRICITY_INDEX_OLD, "000000");
        indexData.put(Constants.KEY_ELECTRICITY_INDEX_NEW, "000000");
        indexData.put(Constants.KEY_WATER_INDEX_OLD, "000000");
        indexData.put(Constants.KEY_WATER_INDEX_NEW, "000000");
        indexData.put(Constants.KEY_TIMESTAMP, date);

        db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID).set(indexData)
                .addOnSuccessListener(aVoid -> {
                    createNextMonthIndex(homeId, roomId, roomName, year, month);
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error writing document", e);
                    onComplete.run();
                });
    }

    private void createNextMonthIndex(String homeId, String roomId, String roomName, int currentYear, int currentMonth) {
        int nextYear = (currentMonth == 12) ? currentYear + 1 : currentYear;
        int nextMonth = (currentMonth == 12) ? 1 : currentMonth + 1;
        String nextIndexID = roomId + "_" + nextYear + "_" + nextMonth;

        String currentIndexID = roomId + "_" + currentYear + "_" + currentMonth;

        db.collection(Constants.KEY_COLLECTION_INDEX).document(currentIndexID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();

                        String electricityIndexNew = documentSnapshot.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                        String waterIndexNew = documentSnapshot.getString(Constants.KEY_WATER_INDEX_NEW);
                        Date date = documentSnapshot.getDate(Constants.KEY_TIMESTAMP);


                        Map<String, Object> nextIndexData = new HashMap<>();
                        nextIndexData.put(Constants.KEY_HOME_ID, homeId);
                        nextIndexData.put(Constants.KEY_ROOM_ID, roomId);
                        nextIndexData.put(Constants.KEY_NAME_ROOM, roomName);
                        nextIndexData.put(Constants.KEY_MONTH, nextMonth);
                        nextIndexData.put(Constants.KEY_YEAR, nextYear);
                        nextIndexData.put(Constants.KEY_ELECTRICITY_INDEX_OLD, electricityIndexNew != null ? electricityIndexNew : "000000");
                        nextIndexData.put(Constants.KEY_ELECTRICITY_INDEX_NEW, "000000");
                        nextIndexData.put(Constants.KEY_WATER_INDEX_OLD, waterIndexNew != null ? waterIndexNew : "000000");
                        nextIndexData.put(Constants.KEY_WATER_INDEX_NEW, "000000");
                        nextIndexData.put(Constants.KEY_TIMESTAMP, date);

                        db.collection(Constants.KEY_COLLECTION_INDEX).document(nextIndexID).set(nextIndexData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "Next month's index created successfully for room: " + roomId);
                                })
                                .addOnFailureListener(e -> Log.w("Firestore", "Error creating next month's index for room: " + roomId, e));
                    } else {
                        Log.w("Firestore", "Failed to get current month's index document for room: " + roomId, task.getException());
                    }
                });
    }


    public void fetchIndexesAndStoreInList(String homeId) {
        indexInterface.showLoading();
        List<Index> indexList = new ArrayList<>();

        db.collection(Constants.KEY_COLLECTION_INDEX)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            AtomicInteger count = new AtomicInteger(0); // Biến đếm số lượng chỉ số đã được xử lý

                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String indexID = document.getId();
                                String roomID = document.getString(Constants.KEY_ROOM_ID);
                                String homeID = document.getString(Constants.KEY_HOME_ID);
                                String nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                String electricityIndexOld = document.getString(Constants.KEY_ELECTRICITY_INDEX_OLD);
                                String electricityIndexNew = document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                                String waterIndexOld = document.getString(Constants.KEY_WATER_INDEX_OLD);
                                String waterIndexNew = document.getString(Constants.KEY_WATER_INDEX_NEW);

                                int month = Integer.parseInt(document.getString(Constants.KEY_MONTH));
                                int year = Integer.parseInt(document.getString(Constants.KEY_YEAR));
                                Date date = document.getDate(Constants.KEY_TIMESTAMP);

                                // Kiểm tra sự tồn tại của phòng
                                db.collection(Constants.KEY_COLLECTION_ROOMS).document(roomID)
                                        .get()
                                        .addOnCompleteListener(roomTask -> {
                                            if (roomTask.isSuccessful()) {
                                                DocumentSnapshot roomDoc = roomTask.getResult();
                                                if (roomDoc != null && roomDoc.exists()) {
                                                    // Nếu phòng tồn tại, thêm chỉ số vào danh sách
                                                    Index index = new Index(homeID, indexID, nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew, month, year);
                                                    index.setDateObject(date);
                                                    indexList.add(index);
                                                } else {
                                                    // Nếu phòng không tồn tại, xóa chỉ số khỏi Firestore
                                                    db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID)
                                                            .delete()
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d("Firestore", "Index successfully deleted for non-existent room: " + roomID);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.w("Firestore", "Error deleting index for non-existent room: " + roomID, e);
                                                            });
                                                }
                                            } else {
                                                Log.w("Firestore", "Error checking room existence: " + roomID, roomTask.getException());
                                            }

                                            // Tăng biến đếm số lượng chỉ số đã được xử lý
                                            count.incrementAndGet();

                                            // Kiểm tra và cập nhật giao diện khi đã xử lý hết các chỉ số
                                            if (count.get() == querySnapshot.size()) {
                                                indexList.sort(Comparator.comparing(Index::getDateObject)); // Sắp xếp danh sách chỉ số theo thời gian
                                                indexInterface.setIndexList(indexList);
                                            }
                                        });
                            }
                        } else {
                            Log.w("Firestore", "No indexes found for the given homeId.");
                            indexInterface.setIndexList(indexList); // Trả về danh sách trống
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        indexInterface.setIndexList(indexList); // Trả về danh sách trống
                    }
                });
    }

    public void saveIndex(Index index) {
        indexInterface.showButtonLoading(R.id.btn_save_index);
        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_OLD, index.getElectricityIndexOld());
        updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_NEW, index.getElectricityIndexNew());
        updateInfo.put(Constants.KEY_WATER_INDEX_OLD, index.getWaterIndexOld());
        updateInfo.put(Constants.KEY_WATER_INDEX_NEW, index.getWaterIndexNew());

        db.collection(Constants.KEY_COLLECTION_INDEX)
                .document(index.getIndexID())
                .update(updateInfo)
                .addOnSuccessListener(aVoid -> {
                    indexInterface.hideButtonLoading(R.id.btn_save_index);

                    int year = index.getYear();
                    int month = index.getMonth();

                    // Kiểm tra và cập nhật chỉ số của tháng tiếp theo
                    updateNextMonthOldIndex(index.getHomeID(), index.getRoomID(), index.getNameRoom(), year, month);
                    fetchIndexesByMonthAndYear(index.getHomeID(), month, year, "init");

                    indexInterface.closeDialog();
                    indexInterface.showDialogActionSuccess("Bạn đã cập nhật chỉ số thành công");
                })
                .addOnFailureListener(e -> {
                    indexInterface.hideButtonLoading(R.id.btn_save_index);
                    Log.w("Firestore", "Error saving index", e);
                });
    }

    private void updateNextMonthOldIndex(String homeId, String roomId, String nameRoom, int currentYear, int currentMonth) {
        int nextYear = currentMonth == 12 ? currentYear + 1 : currentYear;
        int nextMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        String nextIndexID = roomId + "_" + nextYear + "_" + nextMonth;

        db.collection(Constants.KEY_COLLECTION_INDEX).document(nextIndexID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Cập nhật chỉ số cũ của tháng tiếp theo
                        String currentIndexID = roomId + "_" + currentYear + "_" + currentMonth;
                        db.collection(Constants.KEY_COLLECTION_INDEX).document(currentIndexID)
                                .get()
                                .addOnSuccessListener(currentSnapshot -> {
                                    if (currentSnapshot.exists()) {
                                        String electricityIndexNew = currentSnapshot.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                                        String waterIndexNew = currentSnapshot.getString(Constants.KEY_WATER_INDEX_NEW);

                                        Map<String, Object> updateNextIndex = new HashMap<>();
                                        updateNextIndex.put(Constants.KEY_ELECTRICITY_INDEX_OLD, electricityIndexNew != null ? electricityIndexNew : "000000");
                                        updateNextIndex.put(Constants.KEY_WATER_INDEX_OLD, waterIndexNew != null ? waterIndexNew : "000000");

                                        db.collection(Constants.KEY_COLLECTION_INDEX).document(nextIndexID)
                                                .update(updateNextIndex)
                                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Updated next month's old index successfully"))
                                                .addOnFailureListener(e -> Log.w("Firestore", "Error updating next month's old index", e));
                                    }
                                });
                    } else {
                        // Tạo chỉ số cho tháng tiếp theo nếu chưa tồn tại
                        createNextMonthIndex(homeId, roomId, nameRoom, currentYear, currentMonth);
                    }
                });
    }


    public void deleteIndex(Index index) {
        indexInterface.showButtonLoading(R.id.btn_confirm_delete_index);
        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_OLD, "000000");
        updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_NEW, "000000");
        updateInfo.put(Constants.KEY_WATER_INDEX_OLD, "000000");
        updateInfo.put(Constants.KEY_WATER_INDEX_NEW, "000000");

        db.collection(Constants.KEY_COLLECTION_INDEX)
                .document(index.getIndexID())
                .update(updateInfo)
                .addOnSuccessListener(aVoid -> {
                    indexInterface.hideButtonLoading(R.id.btn_confirm_delete_index);

                    int year = index.getYear();
                    int month = index.getMonth();

                    // Kiểm tra và cập nhật chỉ số của tháng tiếp theo
                    updateNextMonthOldIndex(index.getHomeID(), index.getRoomID(), index.getNameRoom(), year, month);
                    fetchIndexesByMonthAndYear(index.getHomeID(), month, year, "init");

                    indexInterface.closeDialog();
                    indexInterface.showDialogActionSuccess("Bạn đã xoá chỉ số thành công");
                })
                .addOnFailureListener(e -> {
                    indexInterface.hideButtonLoading(R.id.btn_confirm_delete_index);
                    Log.w("Firestore", "Error saving index", e);
                });
    }

    public void deleteSelectedIndexes(List<Index> selectedIndexes) {
        indexInterface.showButtonLoading(R.id.btn_confirm_delete_index);
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            indexInterface.showToast("No indexes selected");
            return;
        }

        int month = selectedIndexes.get(0).getMonth();
        int year = selectedIndexes.get(0).getYear();

        AtomicInteger completedCount = new AtomicInteger(0);
        int totalIndexes = selectedIndexes.size();

        for (Index index : selectedIndexes) {
            HashMap<String, Object> updateInfo = new HashMap<>();
            updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_OLD, "000000");
            updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_NEW, "000000");
            updateInfo.put(Constants.KEY_WATER_INDEX_OLD, "000000");
            updateInfo.put(Constants.KEY_WATER_INDEX_NEW, "000000");

            db.collection(Constants.KEY_COLLECTION_INDEX)
                    .document(index.getIndexID())
                    .update(updateInfo)
                    .addOnSuccessListener(aVoid -> {
                        // Kiểm tra và cập nhật chỉ số của tháng tiếp theo
                        updateNextMonthOldIndex(index.getHomeID(), index.getRoomID(), index.getNameRoom(), year, month);

                        if (completedCount.incrementAndGet() == totalIndexes) {
                            indexInterface.hideButtonLoading(R.id.btn_confirm_delete_index);
                            // Tất cả các truy vấn đã hoàn thành
                            fetchIndexesByMonthAndYear(homeID, month, year, "init");
                            indexInterface.closeDialog();
                            indexInterface.closeLayoutDeleteManyRows();
                            indexInterface.showDialogActionSuccess("Bạn đã xoá chỉ số thành công");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error saving index", e);
                        if (completedCount.incrementAndGet() == totalIndexes) {
                            // Tất cả các truy vấn đã hoàn thành
                            fetchIndexesByMonthAndYear(homeID, month, year, "init");
                            indexInterface.closeDialog();
                        }
                    });
        }
    }


    public void fetchIndexesByMonthAndYear(String homeId, int month, int year, String action) {
        indexInterface.showLoading();
        List<Index> indexList = new ArrayList<>();

        db.collection(Constants.KEY_COLLECTION_INDEX)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .whereEqualTo(Constants.KEY_MONTH, month)
                .whereEqualTo(Constants.KEY_YEAR, year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            AtomicInteger count = new AtomicInteger(0); // Biến đếm số lượng chỉ số đã được xử lý

                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String indexID = document.getId();
                                String roomID = document.getString(Constants.KEY_ROOM_ID);
                                String homeID = document.getString(Constants.KEY_HOME_ID);
                                String nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                String electricityIndexOld = document.getString(Constants.KEY_ELECTRICITY_INDEX_OLD);
                                String electricityIndexNew = document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                                String waterIndexOld = document.getString(Constants.KEY_WATER_INDEX_OLD);
                                String waterIndexNew = document.getString(Constants.KEY_WATER_INDEX_NEW);
                                Date date = document.getDate(Constants.KEY_TIMESTAMP);

                                // Kiểm tra sự tồn tại của phòng
                                db.collection(Constants.KEY_COLLECTION_ROOMS).document(roomID)
                                        .get()
                                        .addOnCompleteListener(roomTask -> {
                                            if (roomTask.isSuccessful()) {
                                                DocumentSnapshot roomDoc = roomTask.getResult();
                                                if (roomDoc != null && roomDoc.exists()) {
                                                    // Nếu phòng tồn tại, thêm chỉ số vào danh sách
                                                    Index index = new Index(homeID, indexID, nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew, month, year);
                                                    index.setDateObject(date);
                                                    indexList.add(index);
                                                } else {
                                                    // Nếu phòng không tồn tại, xóa chỉ số khỏi Firestore
                                                    db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID)
                                                            .delete()
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d("Firestore", "Index successfully deleted for non-existent room: " + roomID);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.w("Firestore", "Error deleting index for non-existent room: " + roomID, e);
                                                            });
                                                }
                                            } else {
                                                Log.w("Firestore", "Error checking room existence: " + roomID, roomTask.getException());
                                            }

                                            // Tăng biến đếm số lượng chỉ số đã được xử lý
                                            count.incrementAndGet();

                                            // Kiểm tra và cập nhật giao diện khi đã xử lý hết các chỉ số
                                            if (count.get() == querySnapshot.size()) {
                                                if (Objects.equals(action, "init") || Objects.equals(action, "search")) {
                                                    indexList.sort(Comparator.comparing(Index::getDateObject)); // Sắp xếp danh sách chỉ số theo thời gian
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "electricityIndexOldAscending")) {
                                                    indexList.sort(Comparator.comparing(Index::getElectricityIndexOld));
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "electricityIndexOldDescending")) {
                                                    indexList.sort(Comparator.comparing(Index::getElectricityIndexOld).reversed());
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "electricityIndexNewAscending")) {
                                                    indexList.sort(Comparator.comparing(Index::getElectricityIndexNew));
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "electricityIndexNewDescending")) {
                                                    indexList.sort(Comparator.comparing(Index::getElectricityIndexNew).reversed());
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "waterIndexOldAscending")) {
                                                    indexList.sort(Comparator.comparing(Index::getWaterIndexOld));
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "waterIndexOldDescending")) {
                                                    indexList.sort(Comparator.comparing(Index::getWaterIndexOld).reversed());
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "waterIndexNewAscending")) {
                                                    indexList.sort(Comparator.comparing(Index::getWaterIndexNew));
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "waterIndexNewDescending")) {
                                                    indexList.sort(Comparator.comparing(Index::getWaterIndexNew).reversed());
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "nameRoomAscending")) {
                                                    indexList.sort(Comparator.comparing(Index::getNameRoom));
                                                    indexInterface.setIndexList(indexList);
                                                } else if (Objects.equals(action, "nameRoomDescending")) {
                                                    indexList.sort(Comparator.comparing(Index::getWaterIndexNew).reversed());
                                                    indexInterface.setIndexList(indexList);
                                                }

                                            }
                                        });
                            }
                        } else {
                            Log.w("Firestore", "No indexes found for the given homeId.");
                            indexInterface.setIndexList(indexList); // Trả về danh sách trống
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        indexInterface.setIndexList(indexList); // Trả về danh sách trống
                    }
                });
    }

    public void checkIndexes(String indexID, int month, int year, IndexCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_INDEX)
                .document(indexID) // Đổi thành document(indexID) để truy vấn theo ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isElectricityNewFilled = false;
                        boolean isWaterNewFilled = false;
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            String electricityIndexNew = document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                            String waterIndexNew = document.getString(Constants.KEY_WATER_INDEX_NEW);

                            if (electricityIndexNew != null && !electricityIndexNew.equals("000000")) {
                                isElectricityNewFilled = true;
                            }
                            if (waterIndexNew != null && !waterIndexNew.equals("000000")) {
                                isWaterNewFilled = true;
                            }
                        } else {
                            indexInterface.showToast("Document not found");
                        }

                        callback.onCheckCompleted(isElectricityNewFilled, isWaterNewFilled);
                    } else {
                        indexInterface.showToast("Error getting document: " + task.getException());
                        // Xử lý lỗi nếu cần thiết
                        callback.onCheckCompleted(false, false);
                    }
                });
    }




    public interface IndexCheckCallback {
        void onCheckCompleted(boolean isElectricityNewFilled, boolean isWaterNewFilled);
    }




    public void getCurrentListIndex(String homeId, int month, int year) {
        List<Index> indexList = new ArrayList<>();

        db.collection(Constants.KEY_COLLECTION_INDEX)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .whereEqualTo(Constants.KEY_MONTH, month)
                .whereEqualTo(Constants.KEY_YEAR, year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            AtomicInteger count = new AtomicInteger(0); // Biến đếm số lượng chỉ số đã được xử lý

                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String indexID = document.getId();
                                String roomID = document.getString(Constants.KEY_ROOM_ID);
                                String homeID = document.getString(Constants.KEY_HOME_ID);
                                String nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                String electricityIndexOld = document.getString(Constants.KEY_ELECTRICITY_INDEX_OLD);
                                String electricityIndexNew = document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                                String waterIndexOld = document.getString(Constants.KEY_WATER_INDEX_OLD);
                                String waterIndexNew = document.getString(Constants.KEY_WATER_INDEX_NEW);
                                Date date = document.getDate(Constants.KEY_TIMESTAMP);

                                // Kiểm tra sự tồn tại của phòng
                                db.collection(Constants.KEY_COLLECTION_ROOMS).document(roomID)
                                        .get()
                                        .addOnCompleteListener(roomTask -> {
                                            if (roomTask.isSuccessful()) {
                                                DocumentSnapshot roomDoc = roomTask.getResult();
                                                if (roomDoc != null && roomDoc.exists()) {
                                                    // Nếu phòng tồn tại, thêm chỉ số vào danh sách
                                                    Index index = new Index(homeID, indexID, nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew, month, year);
                                                    index.setDateObject(date);
                                                    indexList.add(index);
                                                } else {
                                                    // Nếu phòng không tồn tại, xóa chỉ số khỏi Firestore
                                                    db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID)
                                                            .delete()
                                                            .addOnSuccessListener(aVoid -> {
                                                                Log.d("Firestore", "Index successfully deleted for non-existent room: " + roomID);
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.w("Firestore", "Error deleting index for non-existent room: " + roomID, e);
                                                            });
                                                }
                                            } else {
                                                Log.w("Firestore", "Error checking room existence: " + roomID, roomTask.getException());
                                            }

                                            // Tăng biến đếm số lượng chỉ số đã được xử lý
                                            count.incrementAndGet();
                                            indexInterface.getListIndexes(indexList);


                                        });
                            }
                        } else {
                            Log.w("Firestore", "No indexes found for the given homeId.");
                            indexInterface.setIndexList(indexList); // Trả về danh sách trống
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                        indexInterface.setIndexList(indexList); // Trả về danh sách trống
                    }
                });
    }


    public void filterIndexes(List<Index> indexList) {
        if (indexList.isEmpty()) {
            indexInterface.hideButtonLoading(R.id.btn_confirm_apply);
            indexInterface.closeDialog();
            indexInterface.showLayoutNoData();
        } else {
            indexInterface.hideButtonLoading(R.id.btn_confirm_apply);
            indexInterface.closeDialog();
            indexInterface.setIndexList(indexList);
        }
    }


    public void checkWaterIsIndexOrNot(OnCheckWaterIsIndexCompleteListener listener) {
        db.collection(Constants.KEY_COLLECTION_SERVICES)
                .whereEqualTo(Constants.KEY_SERVICE_PARENT_HOME_ID, homeID)
                .whereEqualTo(Constants.KEY_SERVICE_NAME, "Nước")
                .whereEqualTo(Constants.KEY_SERVICE_FEE_BASE, 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean isWaterIndex = false;
                        if (task.isSuccessful() && task.getResult() != null) {
                            if (!task.getResult().isEmpty()) {
                                isWaterIndex = true;
                            }
                        }
                        listener.onComplete(isWaterIndex);
                    }
                });
    }

    // Interface for the callback
    public interface OnCheckWaterIsIndexCompleteListener {
        void onComplete(boolean isWaterIndex);
    }

}
