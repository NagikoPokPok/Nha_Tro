package edu.poly.nhtr.presenters;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.models.PlusOrMinusMoney;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.utilities.Constants;
import timber.log.Timber;

public class RoomBillPresenter {

    private final RoomBillListener roomBillListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RoomBillPresenter(RoomBillListener roomBillListener) {
        this.roomBillListener = roomBillListener;
    }

    public void addBill(Room room, int month, int year) {
        roomBillListener.showButtonLoading(R.id.btn_oke);
        Calendar calendar = Calendar.getInstance();
        //int year = calendar.get(Calendar.YEAR);
        //int month = calendar.get(Calendar.MONTH) + 1;

        Date dateMakeBill = new Date();
        Date datePayBill = new Date();

        HashMap<String, Object> billInfo = createBillInfo(room, year, month, dateMakeBill, datePayBill);

        db.collection(Constants.KEY_COLLECTION_BILL)
                .add(billInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        roomBillListener.hideButtonLoading(R.id.btn_oke);
                        roomBillListener.closeDialog();
                        getBill(room, roomBillListener::setBillList);
                    }
                })
                .addOnFailureListener(e -> Timber.e(e, "Error adding bill for roomID: %s", room.getRoomId()));
    }


    public void getBillByMonthYear(Room room, int month, int year, OnGetBillByMonthYearCompleteListener listener) {
        roomBillListener.showLoading();
        queryBillsByMonthYear(room, month, year, listener::onComplete);
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
        billInfo.put(Constants.KEY_MONEY_OF_ROOM, 0);
        billInfo.put(Constants.KEY_MONEY_OF_SERVICE, 0);
        billInfo.put(Constants.KEY_MONEY_OF_ADD_OR_MINUS, 0);
        billInfo.put(Constants.KEY_TOTAL_OF_MONEY, 0);
        billInfo.put(Constants.KEY_TOTAL_OF_MONEY_NEEDED_PAY, 0);
        billInfo.put(Constants.KEY_NUMBER_OF_DAYS_LIVED, 0);
        billInfo.put(Constants.KEY_REASON_OF_ADD_OR_MINUS, "No reason");
        return billInfo;
    }

    public void getBill(Room room, OnGetBillCompleteListener listener) {
        roomBillListener.showLoading();
        queryBills(room, listener);
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


    private void handleQueryResult(Task<QuerySnapshot> task, Room room, OnGetBillCompleteListener listener) {
        List<RoomBill> billList = new ArrayList<>();
        if (task.isSuccessful()) {
            QuerySnapshot querySnapshot = task.getResult();
            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                List<Task<Void>> updateTasks = new ArrayList<>();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    // Kiểm tra nếu trường KEY_DATE_GIVE_BILL chưa tồn tại hoặc cần cập nhật
                    if (!document.contains(Constants.KEY_DATE_GIVE_BILL)) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(Constants.KEY_DATE_GIVE_BILL, new Date());
                        updateTasks.add(document.getReference().update(updates));
                    }


                }

                // Wait for all updates to complete
                Tasks.whenAllSuccess(updateTasks).addOnCompleteListener(updateTask -> {
                    if (updateTask.isSuccessful()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                RoomBill bill = createRoomBillFromDocument(document);
                                billList.add(bill);
                            } catch (NullPointerException e) {
                                Timber.e(e, "Null value encountered in document %s", document.getId());
                            }
                        }
                        billList.sort((bill1, bill2) -> {
                            int yearComparison = Integer.compare(bill2.getYear(), bill1.getYear());
                            if (yearComparison != 0) {
                                return yearComparison;
                            } else {
                                return Integer.compare(bill2.getMonth(), bill1.getMonth());
                            }
                        });

                        listener.onComplete(billList);
                    } else {
                        Timber.e(updateTask.getException(), "Error updating bills for roomID: %s", room.getRoomId());
                    }
                });
            } else {
                listener.onComplete(billList);
            }
        } else {
            Timber.e(task.getException(), "Error getting bills for roomID: %s", room.getRoomId());
            listener.onComplete(billList);
        }
    }

    private RoomBill createRoomBillFromDocument(DocumentSnapshot document) {
        RoomBill bill = new RoomBill();
        bill.billID = document.getId();
        bill.roomID = document.getString(Constants.KEY_ROOM_ID);
        bill.roomName = document.getString(Constants.KEY_NAME_ROOM);

        // Check and log for null values
        Long monthValue = document.getLong(Constants.KEY_MONTH);
        Long yearValue = document.getLong(Constants.KEY_YEAR);
        Date dateCreateBillValue = document.getDate(Constants.KEY_DATE_MAKE_BILL);
        Date datePayBillValue = document.getDate(Constants.KEY_DATE_PAY_BILL);
        Date dayGiveBillValue = document.getDate(Constants.KEY_DATE_GIVE_BILL);
        Long moneyOfRoomValue = document.getLong(Constants.KEY_MONEY_OF_ROOM);
        Long moneyOfServiceValue = document.getLong(Constants.KEY_MONEY_OF_SERVICE);
        Long moneyOfAddOrMinusValue = document.getLong(Constants.KEY_MONEY_OF_ADD_OR_MINUS);
        Long totalOfMoneyValue = document.getLong(Constants.KEY_TOTAL_OF_MONEY);
        Long totalOfMoneyNeededPayValue = document.getLong(Constants.KEY_TOTAL_OF_MONEY_NEEDED_PAY);
        Long numberOfDaysLivedValue = document.getLong(Constants.KEY_NUMBER_OF_DAYS_LIVED);


        bill.month = monthValue != null ? monthValue.intValue() : 0;
        bill.year = yearValue != null ? yearValue.intValue() : 0;
        bill.dateCreateBill = dateCreateBillValue != null ? dateCreateBillValue : new Date();
        bill.datePayBill = datePayBillValue != null ? datePayBillValue : new Date();
        bill.dayGiveBill = dayGiveBillValue != null ? dayGiveBillValue : new Date();
        bill.isNotPayBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_NOT_PAY_BILL));
        bill.isPayedBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_PAYED_BILL));
        bill.isDelayPayBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_DELAY_PAY_BILL));
        bill.isNotGiveBill = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_NOT_GIVE_BILL));
        bill.isMoneyOfAdd = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_MONEY_OF_ADD));
        bill.isMoneyOfMinus = Boolean.TRUE.equals(document.getBoolean(Constants.KEY_IS_MONEY_OF_MINUS));
        bill.moneyOfRoom = moneyOfRoomValue != null ? moneyOfRoomValue.intValue() : 0;
        bill.moneyOfService = moneyOfServiceValue != null ? moneyOfServiceValue.intValue() : 0;
        bill.moneyOfAddOrMinus = moneyOfAddOrMinusValue != null ? moneyOfAddOrMinusValue.intValue() : 0;
        bill.totalOfMoney = totalOfMoneyValue != null ? totalOfMoneyValue.intValue() : 0;
        bill.totalOfMoneyNeededPay = totalOfMoneyNeededPayValue != null ? totalOfMoneyNeededPayValue.intValue() : 0;
        bill.numberOfDaysLived = numberOfDaysLivedValue != null ? numberOfDaysLivedValue.intValue() : 0;
        bill.reasonForAddOrMinusMoney = document.getString(Constants.KEY_REASON_OF_ADD_OR_MINUS);

        // Chuyển đổi danh sách từ HashMap sang danh sách PlusOrMinusMoney
        List<Map<String, Object>> plusOrMinusMoneyListData = (List<Map<String, Object>>) document.get(Constants.KEY_MONEY_PLUS_OR_MINUS);
        List<PlusOrMinusMoney> plusOrMinusMoneyList = new ArrayList<>();

        if (plusOrMinusMoneyListData != null) {
            for (Map<String, Object> item : plusOrMinusMoneyListData) {
                PlusOrMinusMoney money = new PlusOrMinusMoney();
                money.setStt((int) ((Number) item.get("stt")).longValue());
                money.setReason((String) item.get("reason"));
                money.setPlus((Boolean) item.get("plus")); // Assuming there's a 'plus' field
                money.setMoney((int) ((Number) item.get("money")).longValue());
                money.setTitle((String) item.get("title"));
                plusOrMinusMoneyList.add(money);
            }
        }

        bill.setPlusOrMinusMoneyList(plusOrMinusMoneyList);
        bill.setTimeLived(document.getString(Constants.KEY_TIME_LIVED));

        try {
            Long totalMoneyPlus = document.getLong(Constants.KEY_TOTAL_MONEY_PLUS);
            bill.totalMoneyPlus = totalMoneyPlus != null ? totalMoneyPlus : 0L;

            Long totalMoneyMinus = document.getLong(Constants.KEY_TOTAL_MONEY_MINUS);
            bill.totalMoneyMinus = totalMoneyMinus != null ? totalMoneyMinus : 0L;
//            bill.setTotalMoneyPlus(document.getLong(Constants.KEY_TOTAL_MONEY_PLUS));
//            bill.setTotalMoneyMinus(document.getLong(Constants.KEY_TOTAL_MONEY_MINUS));
        } catch (Exception e) {
            Log.e("RoomBillPresenter", "Null data");
        }


        // Update status of isDelayedPayBill
        Date startDate = bill.getDayGiveBill();
        Date endDate = new Date();
//        if (startDate.before(endDate)) {
//            long daysBetween = calculateDaysBetween(startDate, endDate);
//            getContractByRoom(bill.roomID, new OnGetContractByRomListener() {
//                @Override
//                public void onComplete(MainGuest contract) {
//                    int daysUntilDueDate = contract.getDaysUntilDueDate();
//                    if (daysBetween > daysUntilDueDate && bill.isNotPayBill()) {
//                        updateStatusOfBillWhenDelayBill(bill, new DetailBillPresenter.OnUpdateStatusOfBill() {
//                            @Override
//                            public void onComplete() {
//
//                            }
//                        });
//                    }
//                }
//            });
//        }

        if(endDate.before(bill.datePayBill)&& bill.isNotPayBill() ){
            updateStatusOfBillWhenDelayBill(bill, new DetailBillPresenter.OnUpdateStatusOfBill() {
                @Override
                public void onComplete() {

                }
            });
        }

        return bill;
    }

    public void updateStatusOfBillWhenDelayBill(RoomBill bill, DetailBillPresenter.OnUpdateStatusOfBill listener) {
        HashMap<String, Object> data = new HashMap<>();

        //Update status of bill
        data.put(Constants.KEY_IS_NOT_PAY_BILL, false);
        data.put(Constants.KEY_IS_NOT_GIVE_BILL, false);
        data.put(Constants.KEY_IS_PAYED_BILL, false);
        data.put(Constants.KEY_IS_DELAY_PAY_BILL, true);

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_BILL)
                .document(bill.billID)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onComplete();
                    }
                });
    }


    public static long calculateDaysBetween(Date startDate, Date endDate) {
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
    }


    // Interface for the callback
    public interface OnGetBillCompleteListener {
        void onComplete(List<RoomBill> billList);
    }

    public interface OnGetBillByMonthYearCompleteListener {
        void onComplete(List<RoomBill> billList);
    }

    public void deleteBill(RoomBill bill, OnDeleteBillCompleteListener listener) {
        roomBillListener.showButtonLoading(R.id.btn_confirm_delete_bill);
        db.collection(Constants.KEY_COLLECTION_BILL)
                .document(bill.billID)
                .delete()
                .addOnSuccessListener(aVoid ->
                {
                    roomBillListener.hideButtonLoading(R.id.btn_confirm_delete_bill);
                    roomBillListener.closeDialog();
                    listener.onComplete();

                })
                .addOnFailureListener(e -> Timber.e(e, "Error deleting bill with ID: %s", bill.billID));

    }

    public interface OnDeleteBillCompleteListener {
        void onComplete();
    }

    public void deleteListBills(List<RoomBill> billList, OnDeleteBillCompleteListener listener) {
        roomBillListener.showButtonLoading(R.id.btn_confirm_delete_bill);

        // Bắt đầu một batch mới
        WriteBatch batch = db.batch();

        // Duyệt qua danh sách các home cần xóa và thêm thao tác xóa vào batch
        for (RoomBill bill : billList) {
            DocumentReference notificationRef = db.collection(Constants.KEY_COLLECTION_BILL).document(bill.getBillID());
            batch.delete(notificationRef); // Thêm thao tác xóa vào batch
        }

        // Commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    roomBillListener.hideButtonLoading(R.id.btn_confirm_delete_bill);
                    roomBillListener.closeDialog();
                    //notificationListener.showDialogActionSuccess("Bạn đã xoá thông báo thành công");
                    //notificationListener.closeLayoutDeleteNotification();
                    listener.onComplete();
                })
                .addOnFailureListener(e -> {
                    listener.onComplete();
                });

    }

    public void filterBills(List<RoomBill> billList) {
        if (billList.isEmpty()) {
            roomBillListener.hideButtonLoading(R.id.btn_confirm_apply_bill);
            roomBillListener.closeDialog();
            roomBillListener.hideLoading();
            roomBillListener.showLayoutNoData();
        } else {

            roomBillListener.hideButtonLoading(R.id.btn_confirm_apply_bill);
            roomBillListener.closeDialog();
            roomBillListener.setBillList(billList);
        }
    }

    public void getContractByRoom(String roomID, OnGetContractByRomListener listener) {

        db.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                                MainGuest contract = new MainGuest();
                                contract.setPayDate(documentSnapshot.getString(Constants.KEY_CONTRACT_PAY_DATE));
                                contract.setDaysUntilDueDate(Math.toIntExact(documentSnapshot.getLong(Constants.KEY_CONTRACT_DAYS_UNTIL_DUE_DATE)));

                                listener.onComplete(contract);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    public interface OnGetContractByRomListener {
        void onComplete(MainGuest contract);
    }

    public void checkNotificationIsGiven(String roomID, String homeID, OnGetNotificationCompleteListener listener) {
        List<Notification> notificationList = new ArrayList<Notification>();
        db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    Notification notification = new Notification();
                                    notification.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                    notificationList.add(notification);
                                }
                            }
                            notificationList.sort(Comparator.comparing(Notification::getDateObject).reversed());
                            listener.onComplete(notificationList);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onComplete(notificationList);
                    }
                });
    }

    public interface OnGetNotificationCompleteListener {
        void onComplete(List<Notification> notificationList);
    }


    public void checkContractIsCreated(Room room, OnGetContractCompleteListener listener) {
        roomBillListener.showLoading();
        db.collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                listener.onComplete(true);
                            } else {
                                listener.onComplete(false);
                            }

                        } else {
                            listener.onComplete(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onComplete(false);
                    }
                });
    }

    public interface OnGetContractCompleteListener {
        void onComplete(boolean isHave);
    }


    public void checkBillIsCreated(Room room, int month, int year, OnCheckBillIsCreatedCompleteListener listener) {
        db.collection(Constants.KEY_COLLECTION_BILL)
                .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
                .whereEqualTo(Constants.KEY_MONTH, month)
                .whereEqualTo(Constants.KEY_YEAR, year)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                listener.onComplete(true);
                            } else {
                                listener.onComplete(false);
                            }
                        } else {
                            listener.onComplete(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onComplete(false);
                    }
                });
    }

    public interface OnCheckBillIsCreatedCompleteListener {
        void onComplete(boolean isCreated);
    }

}
