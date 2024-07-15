package edu.poly.nhtr.presenters;

import android.icu.text.Collator;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.listeners.ServiceListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class ServicePresenter {
    private final ServiceListener listener;

    public ServicePresenter(ServiceListener listener) {
        this.listener = listener;
    }

    public void saveToFirebase(Service service, List<Room> listRoom, List<Boolean> checkedStates) {
        HashMap<String, Object> serviceInfo = new HashMap<>();
        serviceInfo.put(Constants.KEY_SERVICE_PARENT_HOME_ID, service.getIdHomeParent());
        serviceInfo.put(Constants.KEY_SERVICE_NAME, service.getName());
        serviceInfo.put(Constants.KEY_SERVICE_IMAGE, service.getCodeImage());
        serviceInfo.put(Constants.KEY_SERVICE_FEE_BASE, service.getFee_base());
        serviceInfo.put(Constants.KEY_SERVICE_FEE, service.getPrice());
        serviceInfo.put(Constants.KEY_SERVICE_UNIT, service.getUnit());
        serviceInfo.put(Constants.KEY_SERVICE_NOTE, service.getNote());
        serviceInfo.put(Constants.KEY_SERVICE_ISDELETABLE, service.getDeletable());
        serviceInfo.put(Constants.KEY_SERVICE_ISAPPLY, service.getApply());

        //Update to firestore
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .add(serviceInfo)
                .addOnSuccessListener(documentReference -> {
                    listener.ShowToast("Thêm dịch vụ thành công");
                    listener.CloseDialog();
                    listener.addServiceSuccess(service);
                    setNewRoomServiceAndApply(service, listRoom, checkedStates, () -> {

                    });
//                    applyServiceOfRoom(service, listRoom, checkedStates);
                })
                .addOnFailureListener(e -> listener.ShowToast("Thêm dịch vụ thất bại"));
    }

    private void setNewRoomServiceAndApply(Service service, List<Room> listRoom, List<Boolean> checkedStates, OnRoomServiceUpdatedListener callback) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .whereEqualTo(Constants.KEY_SERVICE_NAME, service.getName())
                .whereEqualTo(Constants.KEY_SERVICE_PARENT_HOME_ID, service.getIdHomeParent())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()){
                        service.setIdService(task.getResult().getDocuments().get(0).getId());
                        updateApplyStatusServiceOfRoom(service, listRoom, checkedStates, callback);
                    }
                });
    }


    public void removeFromFirebase(Service service){
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(service.getIdService())
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        listener.ShowToast("Xóa dịch vụ thành công");
                        listener.deleteService(service);
                    }
                });
    }

    public void deleteService(Service service) {
        if(service.getDeletable())
            removeFromFirebase(service);
        else
            listener.ShowToast("Dịch vụ này không thể xóa");
    }

    public void updateStatusOfApplyToFirebase(Service service) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(service.getIdService())
                .update(Constants.KEY_SERVICE_ISAPPLY, service.getApply())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        listener.showResultUpdateStatusApply(service);
                    }
                });
    }

    public void updateService(Service service, RecyclerView recyclerView, int position, List<Room> listRoom, List<Boolean> checkedStates) {
        HashMap<String, Object> newServiceInfo = new HashMap<>();
        newServiceInfo.put(Constants.KEY_SERVICE_FEE_BASE, service.getFee_base());
        newServiceInfo.put(Constants.KEY_SERVICE_FEE, service.getPrice());
        newServiceInfo.put(Constants.KEY_SERVICE_UNIT, service.getUnit());
        newServiceInfo.put(Constants.KEY_SERVICE_NOTE, service.getNote());

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(service.getIdService())
                .update(newServiceInfo)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        setNewRoomServiceAndApply(service, listRoom, checkedStates, new OnRoomServiceUpdatedListener() {
                            @Override
                            public void onRoomServiceUpdated() {
                                listener.showResultUpdateService(service, recyclerView, position);
                            }
                        });

                    }
                });
    }


    public List<Room> getListRoom(String homeId) {
        List<Room> listRoom = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOMS)
                .whereEqualTo(Constants.KEY_HOME_ID, homeId)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot document : task.getResult()){
                            Room room = new Room(
                                    document.getString(Constants.KEY_NAME_ROOM),
                                    document.getId()
                            );
                            listRoom.add(room);
                            Log.e("nameRoom", room.getNameRoom());
                        }
                        listRoom.sort(Comparator.comparing(Room::getNameRoom, Collator.getInstance(new Locale("vi", "VN"))));
                    }
                });
        return listRoom;
    }

    public List<Boolean> getCheckedStates(List<Room> listRoom) {
        List<Boolean> checkedStates = new ArrayList<>();
        for (Room room : listRoom){
            checkedStates.add(false);
        }
        return checkedStates;
    }


    public void updateApplyStatusServiceOfRoom(Service service, List<Room> listRoom, List<Boolean> checkedStates, OnRoomServiceUpdatedListener callback) {
        for (int i=0; i< listRoom.size(); i++){
            if(checkedStates.get(i)){
                HashMap<String, Object> data = new HashMap<>();
                data.put(Constants.KEY_SERVICE_ID, service.getIdService());
                data.put(Constants.KEY_ROOM_ID, listRoom.get(i).getRoomId());
//                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
//                        .document(listRoom.get(i).getRoomId())
//                        .set(data, SetOptions.merge())
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) Log.e("ApplyServiceNewCollection", "successfully");
//                                else Log.e("ApplyServiceNewCollection", "fail");
//                            }
//                        });
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                        .add(data)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onRoomServiceUpdated();
                                Log.e("ApplyServiceNewCollection", "successfully");
                            }
                            else Log.e("ApplyServiceNewCollection", "fail");
                        });
            }
            else {
                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                        .whereEqualTo(Constants.KEY_SERVICE_ID, service.getIdService())
                        .whereEqualTo(Constants.KEY_ROOM_ID, listRoom.get(i).getRoomId())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult()!=null && !task.getResult().isEmpty())
                                FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                                        .document(task.getResult().getDocuments().get(0).getId())
                                        .delete()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()){
                                                Log.e("ApplyServiceNewCollection", "delete Successfully");
                                                callback.onRoomServiceUpdated();
                                            }

                                            else
                                                Log.e("ApplyServiceNewCollection", "delete fail");
                                        });
                        });
            }
        }
    }

    public void setCheckedStates(List<Boolean> checkedStates, List<Room> listRoom, Service service, OnCheckedStatesLoadedListener callBack) {
        for (int i =0; i < listRoom.size(); i++){
            checkedStates.add(false);
            int finalI = i;
            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_ROOM_SERVICES_INFORMATION)
                    .whereEqualTo(Constants.KEY_ROOM_ID, listRoom.get(i).getRoomId())
                    .whereEqualTo(Constants.KEY_SERVICE_ID, service.getIdService())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult()!=null  && !task.getResult().isEmpty())
                            checkedStates.set(finalI, true);

                        // Check if this is the last item
                        if (finalI == listRoom.size() - 1) {
                            callBack.onCheckedStatesLoaded(checkedStates);
                        }
                    });

        }
    }



    public interface OnCheckedStatesLoadedListener {
        void onCheckedStatesLoaded(List<Boolean> checkedStates);
    }

    public interface OnRoomServiceUpdatedListener {
        void onRoomServiceUpdated();
    }

}
