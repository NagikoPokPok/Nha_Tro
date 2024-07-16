package edu.poly.nhtr.presenters;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;
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

    private RoomBillListener roomBillListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RoomBillPresenter(RoomBillListener roomBillListener) {
        this.roomBillListener = roomBillListener;
    }

    public void addBill(Room room) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        Date dateMakeBill = new Date();
        Date datePayBill = new Date();

        HashMap<String, Object> billInfo = createBillInfo(room, year, month, dateMakeBill, datePayBill);

        db.collection(Constants.KEY_COLLECTION_BILL)
                .add(billInfo)
                .addOnSuccessListener(documentReference -> getBill(room, roomBillListener::setBillList))
                .addOnFailureListener(e -> Timber.e(e, "Error adding bill for roomID: %s", room.getRoomId()));
    }

    public void getBill(Room room, OnGetBillCompleteListener listener) {
        roomBillListener.showLoading();
        queryBills(room, listener);
    }

    public void getBillByMonthYear(Room room, int month, int year, OnGetBillByMonthYearCompleteListener listener) {
        roomBillListener.showLoading();
        queryBillsByMonthYear(room, month, year, listener::onComplete);
    }

    private HashMap<String, Object> createBillInfo(Room room, int year, int month, Date dateMakeBill, Date datePayBill) {
        HashMap<String, Object> billInfo = new HashMap<>();
        billInfo.put(Constants.KEY_ROOM_ID, room.getRoomId());
        billInfo.put(Constants.KEY_NAME_ROOM, room.getNameRoom());
        billInfo.put(Constants.KEY_MONTH, month);
        billInfo.put(Constants.KEY_YEAR, year);
        billInfo.put(Constants.KEY_DATE_MAKE_BILL, dateMakeBill);
        billInfo.put(Constants.KEY_DATE_PAY_BILL, datePayBill);
        billInfo.put(Constants.KEY_IS_NOT_PAY_BILL, false);
        billInfo.put(Constants.KEY_IS_PAYED_BILL, false);
        billInfo.put(Constants.KEY_IS_DELAY_PAY_BILL, false);
        billInfo.put(Constants.KEY_IS_NOT_GIVE_BILL, false);
        billInfo.put(Constants.KEY_IS_MONEY_OF_ADD, false);
        billInfo.put(Constants.KEY_IS_MONEY_OF_MINUS, false);
        billInfo.put(Constants.KEY_MONEY_OF_ROOM, 1000000);
        billInfo.put(Constants.KEY_MONEY_OF_SERVICE, 2000000);
        billInfo.put(Constants.KEY_MONEY_OF_ADD_OR_MINUS, 150000);
        billInfo.put(Constants.KEY_TOTAL_OF_MONEY, 3150000);
        billInfo.put(Constants.KEY_TOTAL_OF_MONEY_NEEDED_PAY, 0);
        billInfo.put(Constants.KEY_NUMBER_OF_DAYS_LIVED, 0);
        billInfo.put(Constants.KEY_REASON_OF_ADD_OR_MINUS, "No reason");
        return billInfo;
    }

    private void queryBills(Room room, OnGetBillCompleteListener listener) {
        db.collection(Constants.KEY_COLLECTION_BILL)
                .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
                .whereEqualTo(Constants.KEY_NAME_ROOM, room.getNameRoom())
                .get()
                .addOnCompleteListener(task -> handleQueryResult(task, room, listener))
                .addOnFailureListener(e -> {
                    Timber.e(e, "Error fetching bills for roomID: %s", room.getRoomId());
                    listener.onComplete(new ArrayList<>());
                });
    }

    private void queryBillsByMonthYear(Room room, int month, int year, OnGetBillCompleteListener listener) {
        db.collection(Constants.KEY_COLLECTION_BILL)
                .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
                .whereEqualTo(Constants.KEY_NAME_ROOM, room.getNameRoom())
                .whereEqualTo(Constants.KEY_MONTH, month)
                .whereEqualTo(Constants.KEY_YEAR, year)
                .get()
                .addOnCompleteListener(task -> handleQueryResult(task, room, listener))
                .addOnFailureListener(e -> {
                    Timber.e(e, "Error fetching bills for roomID: %s", room.getRoomId());
                    listener.onComplete(new ArrayList<>());
                });
    }


    private void handleQueryResult(Task<QuerySnapshot> task, Room room, OnGetBillCompleteListener listener) {
        List<RoomBill> billList = new ArrayList<>();
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    RoomBill bill = createRoomBillFromDocument(document);
                    billList.add(bill);
                }
            }
        } else {
            Timber.e(task.getException(), "Error getting bills for roomID: %s", room.getRoomId());
        }
        billList.sort(Comparator.comparing(RoomBill::getMonth).reversed());
        listener.onComplete(billList);
    }

    private RoomBill createRoomBillFromDocument(DocumentSnapshot document) {
        RoomBill bill = new RoomBill();
        bill.billID = document.getId();
        bill.roomID = document.getString(Constants.KEY_ROOM_ID);
        bill.roomName = document.getString(Constants.KEY_NAME_ROOM);
        bill.month = Objects.requireNonNull(document.getLong(Constants.KEY_MONTH)).intValue();
        bill.year = Objects.requireNonNull(document.getLong(Constants.KEY_YEAR)).intValue();
        bill.dateCreateBill = Objects.requireNonNull(document.getDate(Constants.KEY_DATE_MAKE_BILL));
        bill.datePayBill = Objects.requireNonNull(document.getDate(Constants.KEY_DATE_PAY_BILL));
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
        return bill;
    }

    // Interface for the callback
    public interface OnGetBillCompleteListener {
        void onComplete(List<RoomBill> billList);
    }

    public interface OnGetBillByMonthYearCompleteListener {
        void onComplete(List<RoomBill> billList);
    }
}
