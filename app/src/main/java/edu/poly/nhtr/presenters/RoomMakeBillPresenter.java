package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.RoomMakeBillListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class RoomMakeBillPresenter {
    RoomMakeBillListener listener;

    public RoomMakeBillPresenter(RoomMakeBillListener listener) {
        this.listener = listener;
    }


    public void getMainGuest(String roomId, OnGetContractFromFirebaseListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        MainGuest mainGuest = new MainGuest(
                                document.getString(Constants.KEY_CONTRACT_CREATED_DATE),
                                document.getString(Constants.KEY_CONTRACT_EXPIRATION_DATE),
                                document.getString(Constants.KEY_CONTRACT_PAY_DATE),
                                Math.toIntExact(document.getLong(Constants.KEY_CONTRACT_DAYS_UNTIL_DUE_DATE)),
                                Math.toIntExact(document.getLong(Constants.KEY_ROOM_TOTAl_MEMBERS)),
                                Math.toIntExact(document.getLong(Constants.KEY_CONTRACT_ROOM_PRICE)),
                                document.getString(Constants.KEY_GUEST_PHONE),
                                document.getString(Constants.KEY_GUEST_DATE_IN)
                        );

                        callback.onGetContractFromFirebase(mainGuest);
                    }
                });
    }

    public void getListRoomService(String roomId, OnGetRoomServiceFromFirebaseListener callback) {
        List<RoomService> roomServices = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                        for (DocumentSnapshot document : task.getResult()){
                            RoomService roomService = new RoomService(
                                    document.getId(),
                                    document.getString(Constants.KEY_ROOM_ID),
                                    document.getString(Constants.KEY_SERVICE_ID)
                            );
                            try {
                                roomService.setQuantity(Math.toIntExact(document.getLong(Constants.KEY_ROOM_SERVICE_QUANTITY)));
                            }catch (Exception e){
                                listener.showToast("Hãy cập nhật đầy đủ thông tin cho dịch vụ phòng");
                                roomService.setQuantity(0);
                            }
                            setObjectServiceForListRoom(roomService, roomService1 -> {
                                roomServices.add(roomService1);
                                callback.onGetRoomServiceFromFirebase(roomServices);
                            });

                        }


                    }
                });
    }

    private void setObjectServiceForListRoom(RoomService roomService, OnGetServiceFromFirebaseListener callback) {

            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                    .document(roomService.getServiceId())
                    .get()
                    .addOnCompleteListener(task -> {
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
                    });


    }

    public void setQuantityToServiceWithIndex(List<RoomService> roomServices, RoomBill bill, OnGetQuantityForServiceWithIndexListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_INDEX)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomServices.get(0).getRoomId())
                .whereEqualTo(Constants.KEY_MONTH, bill.month)
                .whereEqualTo(Constants.KEY_YEAR, bill.year)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                        int i = 0;
                        for (RoomService roomService : roomServices){
                            DocumentSnapshot document = task.getResult().getDocuments().get(i);
                            if (roomService.getService().getFee_base() == 0 ){
                                int quantity , oldIndex =0, newIndex =0;
                                if (roomService.getServiceName().equalsIgnoreCase("điện")){
                                    oldIndex = Integer.parseInt(document.getString(Constants.KEY_ELECTRICITY_INDEX_OLD));
                                    newIndex = Integer.parseInt(document.getString(Constants.KEY_ELECTRICITY_INDEX_NEW));
                                } else if (roomService.getServiceName().equalsIgnoreCase("nước") && Boolean.TRUE.equals(task.getResult().getDocuments().get(0).getBoolean(Constants.KEY_WATER_IS_INDEX))) {
                                    oldIndex = Integer.parseInt(document.getString(Constants.KEY_WATER_INDEX_OLD));
                                    newIndex = Integer.parseInt(document.getString(Constants.KEY_WATER_INDEX_NEW));
                                }
                                quantity = newIndex-oldIndex;
                                if (quantity == 0) quantity = 404;
                                roomService.setQuantity(quantity);
                                if (i < task.getResult().getDocuments().size()-1) i++;
                            }
                        }
                        callback.onGetQuantityForServiceWithIndex();
                    }
                    else if (task.isSuccessful() && (task.getResult() == null || task.getResult().isEmpty())){
                        for (RoomService roomService : roomServices){
                            if (roomService.getService().getFee_base() == 0 ){
                                int quantity = 0;
                                roomService.setQuantity(quantity);
                            }
                        }
                        callback.onGetQuantityForServiceWithIndex();
                    }
                });

    }



    public void updateBill(RoomBill bill) {
        HashMap<String, Object> data = new HashMap<>();

        data.put(Constants.KEY_DATE_MAKE_BILL, bill.dateCreateBill);
        data.put(Constants.KEY_DATE_PAY_BILL, bill.datePayBill);
        data.put(Constants.KEY_TIME_LIVED, bill.getTimeLived());

        data.put(Constants.KEY_MONEY_OF_ROOM, bill.moneyOfRoom);
        data.put(Constants.KEY_MONEY_OF_SERVICE, bill.moneyOfService);
        data.put(Constants.KEY_MONEY_OF_ADD_OR_MINUS, bill.moneyOfAddOrMinus);
        data.put(Constants.KEY_TOTAL_OF_MONEY, bill.totalOfMoney);
        data.put(Constants.KEY_TOTAL_MONEY_PLUS, bill.getTotalMoneyPlus());
        data.put(Constants.KEY_TOTAL_MONEY_MINUS, bill.getTotalMoneyMinus());

        //Update status of bill
        data.put(Constants.KEY_IS_NOT_GIVE_BILL, true);
        data.put(Constants.KEY_IS_NOT_PAY_BILL, false);
        data.put(Constants.KEY_IS_PAYED_BILL, false);
        data.put(Constants.KEY_IS_DELAY_PAY_BILL, false);

        data.put(Constants.KEY_MONEY_PLUS_OR_MINUS, bill.getPlusOrMinusMoneyList());

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_BILL)
                .document(bill.billID)
                .update(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        listener.hideButtonLoading(R.id.btn_confirm_make_bill);
                        listener.closeDialog();
                        listener.makeBillSuccessfully();
                    }
                });
    }


    public interface OnGetContractFromFirebaseListener{
        void onGetContractFromFirebase(MainGuest mainGuest);
    }
    public interface OnGetRoomServiceFromFirebaseListener{
        void onGetRoomServiceFromFirebase(List<RoomService> roomServices);
    }
    public interface OnGetServiceFromFirebaseListener{
        void onGetServiceFromFirebase(RoomService roomService);
    }
    public interface OnGetQuantityForServiceWithIndexListener{
        void onGetQuantityForServiceWithIndex();
    }

}
