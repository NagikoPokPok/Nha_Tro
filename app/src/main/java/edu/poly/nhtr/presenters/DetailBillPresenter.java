package edu.poly.nhtr.presenters;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.listeners.DetailBillListener;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class DetailBillPresenter {

    private DetailBillListener detailBillListener;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public DetailBillPresenter(DetailBillListener detailBillListener) {
        this.detailBillListener = detailBillListener;
    }

    public void getRoomPrice(String roomID, OnGetRoomPriceCompleteListener listener) {
        db.collection(Constants.KEY_COLLECTION_ROOMS)
                .document(roomID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            // Lấy thông tin giá phòng từ documentSnapshot
                            String priceString = documentSnapshot.getString(Constants.KEY_PRICE);
                            long roomPrice = 0;
                            if (priceString != null) {
                                // Loại bỏ các dấu chấm phân tách hàng nghìn
                                priceString = priceString.replace(".", "");
                                try {
                                    roomPrice = Long.parseLong(priceString);
                                    // Sử dụng roomPrice theo nhu cầu của bạn
                                } catch (NumberFormatException e) {
                                    // Xử lý lỗi khi chuỗi không phải là số hợp lệ
                                    e.printStackTrace();
                                }
                            } else {
                                // Xử lý trường hợp chuỗi giá trị là null
                            } // Giả sử field giá phòng là "price"
                            listener.onComplete(roomPrice);
                        } else {
                            // Document không tồn tại
                            // handleDocumentNotFound();
                        }
                    } else {
                        // Task không thành công
                        // handleTaskFailure(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi khi lấy dữ liệu thất bại
                    // handleFailure(e);
                });
    }

    public void getRoomServiceAndQuantity(RoomBill bill, OnGetRoomServiceAndQuantityListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .whereEqualTo(Constants.KEY_ROOM_ID, bill.roomID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            List<RoomService> roomServices = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()){
                                RoomService roomService = new RoomService(
                                        document.getId(),
                                        document.getString(Constants.KEY_ROOM_ID),
                                        document.getString(Constants.KEY_SERVICE_ID)
                                );
                                try {
                                    roomService.setQuantity(Math.toIntExact(document.getLong(Constants.KEY_ROOM_SERVICE_QUANTITY)));
                                }catch (Exception e){
//                                    listener.showToast("Hãy cập nhật đầy đủ thông tin cho dịch vụ phòng");
                                    roomService.setQuantity(404);
                                }
                                setObjectServiceForListRoom(roomService, new RoomMakeBillPresenter.OnGetServiceFromFirebaseListener() {
                                    @Override
                                    public void onGetServiceFromFirebase(RoomService roomService) {
                                        if (roomService.getService().getFee_base() == 0){
                                            setQuantityToServiceWithIndex(roomService, bill, new RoomMakeBillPresenter.OnGetQuantityForServiceWithIndexListener() {
                                                @Override
                                                public void onGetQuantityForServiceWithIndex() {
                                                    roomServices.add(roomService);
                                                    callback.onGetRoomServiceAndQuantity(roomServices);
                                                }
                                            });
                                        }else{
                                            roomServices.add(roomService);
                                            callback.onGetRoomServiceAndQuantity(roomServices);
                                        }

                                    }
                                });
                            }
                        }
                    }
                });
    }
    private void setObjectServiceForListRoom(RoomService roomService, RoomMakeBillPresenter.OnGetServiceFromFirebaseListener callback) {

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(roomService.getServiceId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            Service service = new Service(
                                    document.getString(Constants.KEY_SERVICE_PARENT_HOME_ID),
                                    document.getId(),
                                    document.getString(Constants.KEY_SERVICE_NAME),
                                    document.getString(Constants.KEY_SERVICE_IMAGE),
                                    Math.toIntExact(document.getLong(Constants.KEY_SERVICE_FEE)),
                                    document.getString(Constants.KEY_SERVICE_UNIT),
                                    Math.toIntExact(document.getLong(Constants.KEY_SERVICE_FEE_BASE)),
                                    document.getString(Constants.KEY_SERVICE_NOTE),
                                    document.getBoolean(Constants.KEY_SERVICE_ISDELETABLE),
                                    document.getBoolean(Constants.KEY_SERVICE_ISAPPLY)
                            );
                            roomService.setService(service);
                            callback.onGetServiceFromFirebase(roomService);
                        }
                    }
                });
    }

    public void setQuantityToServiceWithIndex(RoomService roomService, RoomBill bill, RoomMakeBillPresenter.OnGetQuantityForServiceWithIndexListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_INDEX)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomService.getRoomId())
                .whereEqualTo(Constants.KEY_MONTH, bill.month)
                .whereEqualTo(Constants.KEY_YEAR, bill.year)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            Log.e("IndexServiceInDetailBill", "InSetIndex " );
                            int quantity = 0, oldIndex =0, newIndex =0;
                            if (roomService.getServiceName().equalsIgnoreCase("điện")){
                                oldIndex = Integer.parseInt(document.getString(Constants.KEY_ELECTRICITY_INDEX_OLD));
                                newIndex = Integer.parseInt(document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW));
                                Log.e("IndexServiceInDetailBill", "Electric " + newIndex);
                            } else if (roomService.getServiceName().equalsIgnoreCase("nước") && Boolean.TRUE.equals(task.getResult().getDocuments().get(0).getBoolean(Constants.KEY_WATER_IS_INDEX))) {
                                oldIndex = Integer.parseInt(document.getString(Constants.KEY_WATER_INDEX_OLD));
                                newIndex = Integer.parseInt(document.getString(Constants.KEY_WATER_INDEX_NEW));
                                Log.e("IndexServiceInDetailBill", "Water " + newIndex);
                            }
                            quantity = newIndex-oldIndex;
                            if (quantity == 0) quantity = 404;

                            roomService.setOldIndex(String.valueOf(oldIndex));
                            roomService.setNewIndex(String.valueOf(newIndex));
                            roomService.setQuantity(quantity);
                            callback.onGetQuantityForServiceWithIndex();
                        }
                    }
                });

    }

    public interface OnGetRoomPriceCompleteListener {
        void onComplete(Long price);
    }

    public interface OnGetRoomServiceAndQuantityListener{
        void onGetRoomServiceAndQuantity(List<RoomService> roomServices);
    }
    public interface OnGetServiceFromFirebaseListener{
        void onGetServiceFromFirebase(RoomService roomService);
    }
}
