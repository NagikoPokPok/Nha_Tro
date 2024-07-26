package edu.poly.nhtr.presenters;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.listeners.DetailBillListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.utilities.Constants;
import timber.log.Timber;

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

    public interface OnGetRoomPriceCompleteListener {
        void onComplete(Long price);
    }
}
