package edu.poly.nhtr.Class;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.models.Service;

public class ServiceUtils {
    public static List<Service> addAvailableService (){
        List<Service> services = new ArrayList<>();
        services.add(new Service("","Điện","",3500, "KWh", 0, false, true));
        services.add(new Service("","Nước","",8500, "Khối", 0, false, true));
        services.add(new Service("","Wifi","",30000, "Phòng", 1, false, false));
        services.add(new Service("","Giữ xe","",30000, "Xe", 3, false, false));
        services.add(new Service("","Rác","",30000, "Phòng", 1, false, false));
        return services;
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

}
