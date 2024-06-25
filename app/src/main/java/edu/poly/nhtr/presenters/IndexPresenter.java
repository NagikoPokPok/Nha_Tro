package edu.poly.nhtr.presenters;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.utilities.Constants;

public class IndexPresenter {

    private final IndexInterface indexInterface;

    public IndexPresenter(IndexInterface indexInterface) {
        this.indexInterface = indexInterface;
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
                                // Thêm index cho room này
                                checkAndAddIndexToRoom(homeId, roomId, roomName, () -> {
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

    private void checkAndAddIndexToRoom(String homeId, String roomId, String roomName, Runnable onComplete) {
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
                            addIndexToRoom(homeId, roomId, roomName, indexID, year, month, onComplete);
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

    public void addIndexToRoom(String homeId, String roomId, String roomName, String indexID, int year, int month, Runnable onComplete) {
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

        db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID).set(indexData)
                .addOnSuccessListener(aVoid -> {
                    indexInterface.showToast("Have a new index");
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error writing document", e);
                    onComplete.run();
                });
    }



    public void fetchIndexesAndStoreInList(String homeId) {
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

                                // Kiểm tra sự tồn tại của phòng
                                db.collection(Constants.KEY_COLLECTION_ROOMS).document(roomID)
                                        .get()
                                        .addOnCompleteListener(roomTask -> {
                                            if (roomTask.isSuccessful()) {
                                                DocumentSnapshot roomDoc = roomTask.getResult();
                                                if (roomDoc != null && roomDoc.exists()) {
                                                    // Nếu phòng tồn tại, thêm chỉ số vào danh sách
                                                    Index index = new Index(homeID, indexID, nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew);
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
                                                indexInterface.setIndexList(indexList);
                                            }
                                        });
                            }
                        } else {
                            Log.w("Firestore", "No indexes found for the given homeId.");
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }



    public void saveIndex(Index index)
    {
//        db.collection(Constants.KEY_COLLECTION_INDEX)
//                .document(index.getIndexID()) // Use the document ID
//                .update(
//                        Constants.KEY_ELECTRICITY_INDEX_OLD, index.getElectricityIndexOld(),
//                        Constants.KEY_ELECTRICITY_INDEX_NEW, index.getElectricityIndexNew(),
//                        Constants.KEY_WATER_INDEX_OLD, index.getWaterIndexOld(),
//                        Constants.KEY_WATER_INDEX_NEW, index.getWaterIndexNew()
//                )
//                .addOnSuccessListener(aVoid -> {
//                    indexInterface.showToast("Success");
//                    fetchIndexesAndStoreInList(index.getHomeID());
//                    indexInterface.closeDialog();
//                    // Refresh data or update UI as needed
//                })
//                .addOnFailureListener(e -> {
//
//                });


        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_OLD, index.getElectricityIndexOld());
        updateInfo.put(Constants.KEY_ELECTRICITY_INDEX_NEW, index.getElectricityIndexNew());
        updateInfo.put(Constants.KEY_WATER_INDEX_OLD, index.getWaterIndexOld());
        updateInfo.put(Constants.KEY_WATER_INDEX_NEW, index.getWaterIndexNew());
        db.collection(Constants.KEY_COLLECTION_INDEX)
                .document(index.getIndexID())
                .update(updateInfo)
                .addOnSuccessListener(aVoid -> {
                    fetchIndexesAndStoreInList(index.getHomeID());
                    indexInterface.closeDialog();
                })
                .addOnFailureListener(e -> {

                });
    }


}
