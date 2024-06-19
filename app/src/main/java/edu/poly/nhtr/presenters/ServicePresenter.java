package edu.poly.nhtr.presenters;

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
                })
                .addOnFailureListener(e -> {
                    listener.ShowToast("Thêm dịch vụ thất bại");
                });
    }
}
