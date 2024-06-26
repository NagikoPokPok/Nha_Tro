package edu.poly.nhtr.presenters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import edu.poly.nhtr.listeners.ServiceListener;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class ServicePresenter {
    private final ServiceListener listener;

    public ServicePresenter(ServiceListener listener) {
        this.listener = listener;
    }

    public void saveToFirebase(Service service) {
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
                })
                .addOnFailureListener(e -> {
                    listener.ShowToast("Thêm dịch vụ thất bại");
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
        Log.e("Delete", ""+service.getIdService());
    }

    public void updateStatusOfApplyToFirebase(Service service) {
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(service.getIdService())
                .update(Constants.KEY_SERVICE_ISAPPLY, service.getApply())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            listener.showResultUpdateStatusApply(service);
                        }
                    }
                });
    }

    public void updateService(Service service, RecyclerView recyclerView, int position) {
        HashMap<String, Object> newServiceInfo = new HashMap<>();
        newServiceInfo.put(Constants.KEY_SERVICE_FEE_BASE, service.getFee_base());
        newServiceInfo.put(Constants.KEY_SERVICE_FEE, service.getPrice());
        newServiceInfo.put(Constants.KEY_SERVICE_UNIT, service.getUnit());
        newServiceInfo.put(Constants.KEY_SERVICE_NOTE, service.getNote());

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                .document(service.getIdService())
                .update(newServiceInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            listener.showResultUpdateService(service, recyclerView, position);
                        }
                    }
                });
    }
}
