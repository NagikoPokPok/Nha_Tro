package edu.poly.nhtr.Class;

import static edu.poly.nhtr.utilities.Constants.KEY_SERVICE_FEE_BASE;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class ServiceUtils {
    public static List<Service> addAvailableService (String homeParentId){
        List<Service> services = new ArrayList<>();
        services.add(new Service(homeParentId,"Điện","",3500, "KWh", 0, "", false, true));
        services.add(new Service(homeParentId,"Nước","",8500, "Khối", 0,"" , false, true));
        services.add(new Service(homeParentId,"Wifi","",30000, "Phòng", 1, "", false, false));
        services.add(new Service(homeParentId,"Giữ xe","",30000, "Xe", 3, "", false, false));
        services.add(new Service(homeParentId,"Rác","",30000, "Phòng", 1, "", false, false));
        addToFireStore(services);
        return services;
    }

    private static void addToFireStore(List<Service> services) {
        for(Service service : services){
            HashMap<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put(Constants.KEY_SERVICE_PARENT_HOME_ID, service.getIdHomeParent());
            serviceInfo.put(Constants.KEY_SERVICE_NAME, service.getName());
            serviceInfo.put(Constants.KEY_SERVICE_IMAGE, service.getCodeImage());
            serviceInfo.put(KEY_SERVICE_FEE_BASE, service.getFee_base());
            serviceInfo.put(Constants.KEY_SERVICE_FEE, service.getPrice());
            serviceInfo.put(Constants.KEY_SERVICE_UNIT, service.getUnit());
            serviceInfo.put(Constants.KEY_SERVICE_NOTE, service.getNote());
            serviceInfo.put(Constants.KEY_SERVICE_ISDELETABLE, service.getDeletable());
            serviceInfo.put(Constants.KEY_SERVICE_ISAPPLY, service.getApply());

            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                    .add(serviceInfo)
                    .addOnSuccessListener(documentReference -> {
                        Log.e("service","add successful");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("service","add fail");
                    });
        }
    }

    public static List<Service> usedService(List<Service> services){
        List<Service> usedServices = new ArrayList<>();
        for (Service service : services){
            if(service.getApply()) usedServices.add(service);
        }
        return usedServices;
    }

    public static List<Service> unusedService(List<Service> services){
        List<Service> unusedServices = new ArrayList<>();
        for (Service service : services){
            if(!service.getApply()) unusedServices.add(service);
        }
        return unusedServices;
    }

    public static List<Service> getAvailableService(FirebaseFirestore data, String idHomeParent) {
        List<Service> services = new ArrayList<>();
        data.collection(Constants.KEY_COLLECTION_SERVICES)
                .whereEqualTo(Constants.KEY_SERVICE_PARENT_HOME_ID, idHomeParent)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot document : task.getResult()){
                            Service service = new Service(
                                    document.getString(Constants.KEY_SERVICE_PARENT_HOME_ID),
                                    document.getString(Constants.KEY_SERVICE_NAME),
                                    document.getString(Constants.KEY_SERVICE_IMAGE),
                                    Objects.requireNonNull(document.getLong(Constants.KEY_SERVICE_FEE)).intValue(),
                                    document.getString(Constants.KEY_SERVICE_UNIT),
                                    Objects.requireNonNull(document.getLong(KEY_SERVICE_FEE_BASE)).intValue(),
                                    document.getString(Constants.KEY_SERVICE_NOTE),
                                    document.getBoolean(Constants.KEY_SERVICE_ISDELETABLE),
                                    document.getBoolean(Constants.KEY_SERVICE_ISAPPLY)
                            );
                            services.add(service);
                        }

                    }
                });
        return services;
    }
}
