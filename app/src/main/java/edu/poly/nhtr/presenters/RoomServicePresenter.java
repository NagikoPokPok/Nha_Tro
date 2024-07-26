package edu.poly.nhtr.presenters;

import android.icu.text.Collator;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.Adapter.ServiceAdapter;
import edu.poly.nhtr.listeners.RoomServiceListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class RoomServicePresenter {
    private final RoomServiceListener listener;

    public RoomServicePresenter(RoomServiceListener listener) {
        this.listener = listener;
    }

    public void getRoomServices(String roomId, OnGetRoomServiceListener callback) {
        List<RoomService> roomServices = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult()!=null && !task.getResult().isEmpty()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                RoomService roomService = new RoomService(
                                        document.getId(),
                                        document.getString(Constants.KEY_ROOM_ID),
                                        document.getString(Constants.KEY_SERVICE_ID)
                                );

                                roomService.setRoom(getRoom(roomService.getRoomId()));
                                roomService.setService(getService(roomService, new OnGetServiceListener() {
                                    @Override
                                    public void onGetService(RoomService roomService) {
                                        final int[] quantity = {0};
                                        try {
                                            quantity[0] = Math.toIntExact(document.getLong(Constants.KEY_ROOM_SERVICE_QUANTITY));
                                        } catch (Exception e) {
                                            if(roomService.getService() != null){
                                                if(roomService.getService().getFee_base() == 1){
                                                    quantity[0] = 1;
                                                } else if (roomService.getService().getFee_base() == 2) {
                                                    getNumberOfGuest(roomId, new OnGetGuestListener() {
                                                        @Override
                                                        public void onGetQuantityOfGuestInRoom(int quantityOfGuest) {
                                                            quantity[0] = quantityOfGuest;
                                                        }
                                                    });
                                                } else if (roomService.getService().getFee_base() == 0) {
                                                    quantity[0] = -1;
                                                }
                                            }
                                        }
                                        roomService.setQuantity(quantity[0]);
                                        Log.e("serviceList", "quantity: "+roomService.getQuantity());
                                        Log.e("serviceList", roomService.getService().getName());

                                        roomServices.add(roomService);
                                        roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));
                                        callback.onGetRoomService(roomServices);
                                    }
                                }));

//                                roomServices.add(roomService);

//                                Log.e("serviceList", roomService.getQuantity()+" ");

                            }

//                            callback.onGetRoomService(roomServices);

                        } else if (task.isSuccessful() && task.getResult().isEmpty()) {
                            callback.onGetRoomService(roomServices);
                        }
                    }
                });

    }

    private void getNumberOfGuest(String roomId, OnGetGuestListener callback) {
        final int[] numberOfPeople = {0};
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            numberOfPeople[0] = Math.toIntExact(task.getResult().getDocuments().get(0).getLong(Constants.KEY_ROOM_TOTAl_MEMBERS));
                            callback.onGetQuantityOfGuestInRoom(numberOfPeople[0]);
                        }
                    }
                });
    }

    private Service getService(RoomService roomService, OnGetServiceListener callback) {
        final Service[] service = new Service[1];
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(roomService.getServiceId())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null){
                        DocumentSnapshot document = task.getResult();
                        service[0] = new Service(
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
                        roomService.setService(service[0]);
                        callback.onGetService(roomService);
                    }
                });
        return service[0];
    }

    public Room getRoom(String roomId) {
        final Room[] room = new Room[1];
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOMS)
                .document(roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && task.getResult()!=null){
                            DocumentSnapshot document = task.getResult();
                            room[0] = new Room(
                                    document.getString(Constants.KEY_NAME_ROOM),
                                    document.getString(Constants.KEY_CONTRACT_ROOM_PRICE),
                                    document.getString(Constants.KEY_DESCRIBE)
                            );
                        }
                    }
                });
        return room[0];
    }

    public void deleteService(Service service, String roomId) {
        if(!service.isElectricOrWater())
            removeFromFirebase(service, roomId);
        else
            listener.ShowToast("Dịch vụ này không thể xóa");
    }

    private void removeFromFirebase(Service service, String roomId) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .whereEqualTo(Constants.KEY_SERVICE_ID, service.getIdService())
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            deleteFromFirebase(task.getResult().getDocuments().get(0).getId(), service);
                        }
                    }
                });
    }

    private void deleteFromFirebase(String id, Service service) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .document(id)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            listener.deleteSuccessfully(service);
                        }
                    }
                });
    }

//    public void updateService(int quantity, Service service, Room room) {
//        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
//                .whereEqualTo(Constants.KEY_ROOM_ID, room.getRoomId())
//                .whereEqualTo(Constants.KEY_SERVICE_ID, )
//    }

    public List<Service> getAvailableService(String homeId, List<RoomService> roomServices, OnGetAvailableServiceListener callback) {
        List<Service> services = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .whereEqualTo(Constants.KEY_SERVICE_PARENT_HOME_ID, homeId)
                .whereEqualTo(Constants.KEY_SERVICE_ISAPPLY, true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            for (DocumentSnapshot document : task.getResult()){
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
                                if(!checkServiceExistInRoom(service.getIdService(), roomServices)){
                                    services.add(service);
                                }

                            }
                            callback.onGetAvailableService(services);
                        }
                    }
                });
        return services;
    }

    private boolean checkServiceExistInRoom(String idService, List<RoomService> roomServices) {
        for (RoomService roomService : roomServices){
            if (roomService.getServiceId().equals(idService)) return true;
        }
        return false;
    }

    public void getServiceOfRoom(List<RoomService> roomServices, OnGetServiceOfRoomListener callback) {
        List<Service> services = new ArrayList<>();
        for (RoomService roomService : roomServices){
            services.add(roomService.getService());
//            Log.e("serviceList", roomService.getService().getName());
        }
        callback.OnGetServiceOfRoom(services);
    }

    public void updateServiceQuantity(int quantity, String roomServiceId) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                .document(roomServiceId)
                .update(Constants.KEY_ROOM_SERVICE_QUANTITY, quantity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            listener.updateSuccessfully(quantity, roomServiceId);
                        }
                    }
                });
    }

    public void setNewRoomServiceAuto(String homeId, String roomId, OnRoomServiceAutoSetListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .whereEqualTo(Constants.KEY_SERVICE_PARENT_HOME_ID, homeId)
                .whereEqualTo(Constants.KEY_SERVICE_ISDELETABLE, false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                            for (DocumentSnapshot document : task.getResult()){
                                String serviceName = document.getString(Constants.KEY_SERVICE_NAME);
                                if (serviceName != null && (serviceName.equalsIgnoreCase("điện") || serviceName.equalsIgnoreCase("nước"))) {
                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put(Constants.KEY_SERVICE_ID, document.getId());
                                    data.put(Constants.KEY_ROOM_ID, roomId);

                                    FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                                            .add(data)
                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if (task.isSuccessful()) callback.onRoomServiceAutoSet();
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    public void updateDataBeforeAdd(ServiceAdapter adapter, List<RoomService> roomServices, List<Service> services) {
        List<Service> listServiceAdd = adapter.getSelectedServices();
        String roomId = roomServices.get(0).getRoomId();
        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        final int[] count = {0};

        //Update data of services in room to firebase
        for (Service service : listServiceAdd){
            setNewRoomServiceQuantity(roomId, service, new OnSetNewRoomServiceQuantityListener() {
                @Override
                public void onSetNewRoomServiceQuantity(int quantity) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(Constants.KEY_ROOM_ID, roomId);
                    data.put(Constants.KEY_SERVICE_ID, service.getIdService());
                    data.put(Constants.KEY_ROOM_SERVICE_QUANTITY, quantity);

                    DocumentReference documentRoomService = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION).document();
                    batch.set(documentRoomService, data);
                    count[0]++;

                    if (listServiceAdd.size() == count[0]){
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    getRoomServices(roomId, new OnGetRoomServiceListener() {
                                        @Override
                                        public void onGetRoomService(List<RoomService> listRoomService) {
                                            roomServices.clear();
                                            roomServices.addAll(listRoomService);

                                            // Remove listServiceAdd in services
                                            for (Service service : listServiceAdd){
                                                services.remove(service);
                                            }

                                            listener.updateDataBeforeAddSuccessfully(adapter);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });



        }




    }

    private void setNewRoomServiceQuantity(String roomId, Service service, OnSetNewRoomServiceQuantityListener callback) {
        if(service.getFee_base() == 1){
            callback.onSetNewRoomServiceQuantity(1);
        } else if (service.getFee_base() == 2) {
            getNumberOfGuest(roomId, new OnGetGuestListener() {
                @Override
                public void onGetQuantityOfGuestInRoom(int quantityOfGuest) {
                    callback.onSetNewRoomServiceQuantity(quantityOfGuest);
                }
            });
        } else if (service.getFee_base() == 3) {
            callback.onSetNewRoomServiceQuantity(0);
        } else if (service.getFee_base() == 0) {
            callback.onSetNewRoomServiceQuantity(404);
        }
    }

    public interface OnRoomServiceAutoSetListener {
        void onRoomServiceAutoSet();
    }
    public interface OnGetRoomServiceListener {
        void onGetRoomService(List<RoomService> listRoomService);
    }
    public interface OnGetServiceListener {
        void onGetService(RoomService roomService);
    }
    public interface OnGetServiceOfRoomListener {
        void OnGetServiceOfRoom(List<Service> services);
    }
    public interface OnGetAvailableServiceListener{
        void onGetAvailableService(List<Service> services);
    }
    public interface OnGetGuestListener{
        void onGetQuantityOfGuestInRoom(int quantityOfGuest);
    }
    public interface OnSetNewRoomServiceQuantityListener{
        void onSetNewRoomServiceQuantity(int quantity);
    }
}
