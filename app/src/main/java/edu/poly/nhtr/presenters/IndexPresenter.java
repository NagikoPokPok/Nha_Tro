package edu.poly.nhtr.presenters;

import android.util.Log;

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

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.utilities.Constants;

public class IndexPresenter {

    private IndexInterface indexInterface;

    public IndexPresenter(IndexInterface indexInterface) {
        this.indexInterface = indexInterface;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void fetchRoomsAndAddIndex(String homeId) {
        db.collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<DocumentSnapshot> rooms = querySnapshot.getDocuments();
                            for (DocumentSnapshot roomDoc : rooms) {
                                String roomId = roomDoc.getId();
                                String roomName = roomDoc.getString(Constants.KEY_NAME_ROOM); // Lấy tên phòng
                                // Thêm index cho room này
                                addIndexToRoom(homeId, roomId, roomName);
                            }
                        } else {
                            Log.w("Firestore", "No rooms found for the given homeId.");
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }

    public void addIndexToRoom(String homeId, String roomId, String roomName) {
        // Lấy tháng và năm hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Tháng trong Calendar bắt đầu từ 0

        // Tạo dữ liệu index
        Map<String, Object> indexData = new HashMap<>();
        indexData.put(Constants.KEY_HOME_ID, homeId);
        indexData.put(Constants.KEY_ROOM_ID, roomId);
        indexData.put(Constants.KEY_NAME_ROOM, roomName);
        indexData.put(Constants.KEY_MONTH, month);
        indexData.put(Constants.KEY_YEAR, year);
        indexData.put(Constants.KEY_ELECTRICITY_INDEX_OLD, "100"); // Thay thế bằng giá trị thực tế
        indexData.put(Constants.KEY_ELECTRICITY_INDEX_NEW, 150+""); // Thay thế bằng giá trị thực tế
        indexData.put(Constants.KEY_WATER_INDEX_OLD, 50+""); // Thay thế bằng giá trị thực tế
        indexData.put(Constants.KEY_WATER_INDEX_NEW, 75+""); // Thay thế bằng giá trị thực tế

        // Tạo ID cho document theo định dạng "roomId_year_month"
        String indexID = roomId + "_" + year + "_" + month;

        // Lưu document vào Firestore
        db.collection(Constants.KEY_COLLECTION_INDEX).document(indexID).set(indexData)
                .addOnSuccessListener(aVoid -> {
                    // Xử lý khi lưu thành công
                    Log.d("Firestore", "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener(e -> {
                    // Xử lý khi lưu thất bại
                    Log.w("Firestore", "Error writing document", e);
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
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String nameRoom = document.getString(Constants.KEY_NAME_ROOM);
                                String electricityIndexOld = document.getString(Constants.KEY_ELECTRICITY_INDEX_OLD);
                                String electricityIndexNew = document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW);
                                String waterIndexOld = document.getString(Constants.KEY_WATER_INDEX_OLD);
                                String waterIndexNew = document.getString(Constants.KEY_WATER_INDEX_NEW);

                                Index index = new Index(nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew);
                                indexList.add(index);
                            }
                            indexInterface.setIndexList(indexList);
                        } else {
                            Log.w("Firestore", "No indexes found for the given homeId.");
                        }
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
    }


}
