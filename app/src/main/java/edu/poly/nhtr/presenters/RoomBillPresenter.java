package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.utilities.Constants;
import timber.log.Timber;

public class RoomBillPresenter {

    RoomBillListener roomBillListener;

    public RoomBillPresenter(RoomBillListener roomBillListener) {
        this.roomBillListener = roomBillListener;
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addBill(Room room) {
        LocalDate datePayBill = LocalDate.now();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        LocalDate dateMakeBill = LocalDate.now();

        // Chuyển đổi LocalDate thành Date
        LocalDate localDate = LocalDate.now();
        Date dateMakeBill1 = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date datePyBill1 = Date.from(datePayBill.atStartOfDay(ZoneId.systemDefault()).toInstant());

        HashMap<String, Object> billInfo = new HashMap<>();
        billInfo.put(Constants.KEY_ROOM_ID, room.getRoomId());
        billInfo.put(Constants.KEY_NAME_ROOM, room.getNameRoom());
        billInfo.put(Constants.KEY_MONTH, month);
        billInfo.put(Constants.KEY_YEAR, year);
        billInfo.put(Constants.KEY_DATE_MAKE_BILL,dateMakeBill1);
        billInfo.put(Constants.KEY_DATE_PAY_BILL, datePyBill1);
        billInfo.put(Constants.KEY_IS_NOT_PAY_BILL, false);
        billInfo.put(Constants.KEY_IS_PAYED_BILL, false);
        billInfo.put(Constants.KEY_IS_DELAY_PAY_BILL, false);
        billInfo.put(Constants.KEY_IS_NOT_GIVE_BILL, false);
        billInfo.put(Constants.KEY_IS_MONEY_OF_ADD, false);
        billInfo.put(Constants.KEY_IS_MONEY_OF_MINUS, false);
        billInfo.put(Constants.KEY_MONEY_OF_ROOM, 0);
        billInfo.put(Constants.KEY_MONEY_OF_SERVICE, 0);
        billInfo.put(Constants.KEY_MONEY_OF_ADD_OR_MINUS, 0);
        billInfo.put(Constants.KEY_TOTAL_OF_MONEY, 0);
        billInfo.put(Constants.KEY_TOTAL_OF_MONEY_NEEDED_PAY, 0);
        billInfo.put(Constants.KEY_NUMBER_OF_DAYS_LIVED, 0);
        billInfo.put(Constants.KEY_REASON_OF_ADD_OR_MINUS, "No reason");

        db.collection(Constants.KEY_COLLECTION_BILL)
                .add(billInfo)
                .addOnSuccessListener(documentReference -> {
                    getBill(room, new OnGetBillCompleteListener() {
                        @Override
                        public void onComplete(List<RoomBill> billList) {
                            roomBillListener.setBillList(billList);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    // Failure logic
                });
    }

    public void getBill(Room room, OnGetBillCompleteListener listener) {
        List<RoomBill> billList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0); // Biến đếm số lượng chỉ số đã được xử lý

        // Truy vấn database để lấy danh sách các hóa đơn dựa trên roomID và nameRoom
        db.collection(Constants.KEY_COLLECTION_BILL)
                .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
                .whereEqualTo(Constants.KEY_NAME_ROOM, room.getNameRoom())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Duyệt qua từng document trong querySnapshot
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                RoomBill bill = new RoomBill();
                                bill.billID = document.getId();
                                bill.roomID = document.getString(Constants.KEY_ROOM_ID);
                                bill.roomName = document.getString(Constants.KEY_NAME_ROOM);
                                bill.month = Objects.requireNonNull(document.getLong(Constants.KEY_MONTH)).intValue();
                                bill.year = Objects.requireNonNull(document.getLong(Constants.KEY_YEAR)).intValue();
                                bill.dateCreateBill = Objects.requireNonNull(document.getDate(Constants.KEY_DATE_MAKE_BILL)).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                                bill.datePayBill = Objects.requireNonNull(document.getDate(Constants.KEY_DATE_PAY_BILL)).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();

                                bill.isNotPayBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_NOT_PAY_BILL));
                                bill.isPayedBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_PAYED_BILL));
                                bill.isDelayPayBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_DELAY_PAY_BILL));
                                bill.isNotGiveBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_NOT_GIVE_BILL));
                                bill.isMoneyOfAdd = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_MONEY_OF_ADD));
                                bill.isMoneyOfMinus = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_MONEY_OF_MINUS));
                                bill.moneyOfRoom = Objects.requireNonNull(document.getLong(Constants.KEY_MONEY_OF_ROOM)).intValue();
                                bill.moneyOfService = Objects.requireNonNull(document.getLong(Constants.KEY_MONEY_OF_SERVICE)).intValue();
                                bill.moneyOfAddOrMinus = Objects.requireNonNull(document.getLong(Constants.KEY_MONEY_OF_ADD_OR_MINUS)).intValue();
                                bill.totalOfMoney = Objects.requireNonNull(document.getLong(Constants.KEY_TOTAL_OF_MONEY)).intValue();
                                bill.totalOfMoneyNeededPay = Objects.requireNonNull(document.getLong(Constants.KEY_TOTAL_OF_MONEY_NEEDED_PAY)).intValue();
                                bill.numberOfDaysLived = Objects.requireNonNull(document.getLong(Constants.KEY_NUMBER_OF_DAYS_LIVED)).intValue();
                                bill.reasonForAddOrMinusMoney = document.getString(Constants.KEY_REASON_OF_ADD_OR_MINUS);

                                billList.add(bill);
                            }
                        }
                    } else {
                        // Xử lý khi không thành công
                        Timber.e(task.getException(), "Error getting bills for roomID: %s", room.getRoomId());
                    }

                    // Tăng biến đếm sau mỗi lần truy vấn hoàn thành
                    count.incrementAndGet();
                    billList.sort(Comparator.comparing(RoomBill::getMonth).reversed());
                    listener.onComplete(billList);
                })
                .addOnFailureListener(e -> {
                    count.incrementAndGet(); // Tăng biến đếm để tránh deadlock
                    Timber.e(e, "Error fetching bills for roomID: %s", room.getRoomId());
                    listener.onComplete(new ArrayList<>()); // Trả về danh sách rỗng trong trường hợp lỗi
                });
    }

    // Interface for the callback
    public interface OnGetBillCompleteListener {
        void onComplete(List<RoomBill> billList);
    }
}
