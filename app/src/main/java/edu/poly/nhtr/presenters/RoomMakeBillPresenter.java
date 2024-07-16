package edu.poly.nhtr.presenters;

import android.icu.text.Collator;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.listeners.RoomMakeBillListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class RoomMakeBillPresenter {
    RoomMakeBillListener listener;

    public RoomMakeBillPresenter(RoomMakeBillListener listener) {
        this.listener = listener;
    }

    public Room getRoomFromFirebase(String roomId) {
        final Room[] room = new Room[1];
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOMS)
                .document(roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            room[0] = new Room(
                                    document.getString(Constants.KEY_NAME_ROOM),
                                    document.getString(Constants.KEY_PRICE),
                                    document.getString(Constants.KEY_DESCRIBE)
                            );
                        }
                    }
                });
        return room[0];
    }

    public void getMainGuest(String roomId, OnGetContractFromFirebaseListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            MainGuest mainGuest1 = new MainGuest(
//                                    Math.toIntExact(document.getLong(Constants.KEY_ROOM_TOTAl_MEMBERS)),

                            );

                            callback.onGetContractFromFirebase(mainGuest1);
                        }
                    }
                });
    }

    public void getListRoomService(String roomId, OnGetRoomServiceFromFirebaseListener callback) {
        List<RoomService> roomServices = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            for (DocumentSnapshot document : task.getResult()){
                                RoomService roomService = new RoomService(
                                        document.getId(),
                                        document.getString(Constants.KEY_ROOM_ID),
                                        document.getString(Constants.KEY_SERVICE_ID),
                                        Math.toIntExact(document.getLong(Constants.KEY_ROOM_SERVICE_QUANTITY))
                                );
                                roomServices.add(roomService);
                            }


                            setObjectServiceForListRoom(roomServices, new OnGetServiceFromFirebaseListener() {
                                @Override
                                public void onGetServiceFromFirebase(List<RoomService> roomServices) {
                                    roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));
                                    callback.onGetRoomServiceFromFirebase(roomServices);
                                }
                            });



                        }
                    }
                });
    }

    private void setObjectServiceForListRoom(List<RoomService> roomServices, OnGetServiceFromFirebaseListener callback) {
        for (RoomService roomService : roomServices){
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
                            }
                        }
                    });
        }
        callback.onGetServiceFromFirebase(roomServices);
    }


    public interface OnGetRoomFromFirebaseListener{
        void onGetRoomFromFirebase(Room room);
    }
    public interface OnGetContractFromFirebaseListener{
        void onGetContractFromFirebase(MainGuest mainGuest);
    }
    public interface OnGetRoomServiceFromFirebaseListener{
        void onGetRoomServiceFromFirebase(List<RoomService> roomServices);
    }
    public interface OnGetServiceFromFirebaseListener{
        void onGetServiceFromFirebase(List<RoomService> roomServices);
    }
}
