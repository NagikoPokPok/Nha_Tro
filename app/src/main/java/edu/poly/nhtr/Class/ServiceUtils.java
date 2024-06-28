package edu.poly.nhtr.Class;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.Collator;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.models.Service;
import edu.poly.nhtr.utilities.Constants;

public class ServiceUtils {
    public static List<Service> addAvailableService (String homeParentId, Context context){
        List<Service> services = new ArrayList<>();
        services.add(new Service(homeParentId,"Điện",getEncodeImageOfService("image_electricity", context),3500, "KWh", 0, "", false, true));
        services.add(new Service(homeParentId,"Nước",getEncodeImageOfService("image_water", context),8500, "Khối", 0,"" , false, true));
        services.add(new Service(homeParentId,"Wifi",getEncodeImageOfService("image_wifi", context),30000, "Phòng", 1, "", false, false));
        services.add(new Service(homeParentId,"Giữ xe",getEncodeImageOfService("image_motor", context),30000, "Xe", 3, "", false, false));
        services.add(new Service(homeParentId,"Rác",getEncodeImageOfService("image_waste", context),30000, "Phòng", 1, "", false, false));
        addToFireStore(services, homeParentId);
        return services;
    }

    private static String getEncodeImageOfService(String encodeImageOfService, Context context) {
        // Load the image from drawable
        int resourceId = context.getResources().getIdentifier(encodeImageOfService, "drawable", context.getPackageName());
        if (resourceId == 0) {
            return ""; // Handle the case where the resource is not found
        }
        Log.d("resource", "Resource ID for " + encodeImageOfService + " is " + resourceId);

        // Load the image from drawable
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (bitmap == null) {
        Log.d("bitmap", "null");
            return ""; // Handle the case where the bitmap is null
        }


        // Encode the image
        return encodedImage(bitmap);
    }

    public static String encodedImage(Bitmap bitmap) // Hàm mã hoá ảnh thành chuỗi Base64
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap getConversionImage(String encodedImage){
        return getConversionImageAndSize(encodedImage, 150, 150);
    }

    public static Bitmap getConversionImageAndSize(String encodedImage, int width, int height) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }


    private static void addToFireStore(List<Service> services, String homeParentId) {
        for(Service service : services){
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

            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_SERVICES)
                    .add(serviceInfo)
                    .addOnSuccessListener(documentReference -> {
                        Log.e("service","add successful");
                        update_status_isHaveService(homeParentId);
                    })
                    .addOnFailureListener(e -> Log.e("service","add fail"));
        }
    }

    private static void update_status_isHaveService(String homeParentId) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_HOMES).document(homeParentId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("homeIsHaveService", true);
        documentReference.update(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Firestore", "DocumentSnapshot successfully updated!");
            } else {
                Log.w("Firestore", "Error updating document", task.getException());
            }
        });
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

    public static void getAvailableService(FirebaseFirestore data, String idHomeParent, OnServicesLoadedListener listener) {
        List<Service> services = new ArrayList<>();
        data.collection(Constants.KEY_COLLECTION_SERVICES)
                .whereEqualTo(Constants.KEY_SERVICE_PARENT_HOME_ID, idHomeParent)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot document : task.getResult()){
                            Service service = new Service(
                                    document.getString(Constants.KEY_SERVICE_PARENT_HOME_ID),
                                    document.getId(),
                                    document.getString(Constants.KEY_SERVICE_NAME),
                                    document.getString(Constants.KEY_SERVICE_IMAGE),
                                    Objects.requireNonNull(document.getLong(Constants.KEY_SERVICE_FEE)).intValue(),
                                    document.getString(Constants.KEY_SERVICE_UNIT),
                                    Objects.requireNonNull(document.getLong(Constants.KEY_SERVICE_FEE_BASE)).intValue(),
                                    document.getString(Constants.KEY_SERVICE_NOTE),
                                    document.getBoolean(Constants.KEY_SERVICE_ISDELETABLE),
                                    document.getBoolean(Constants.KEY_SERVICE_ISAPPLY)
                            );
                            Log.e("idService",service.getIdService() + " " + service.getIdHomeParent());
                            services.add(service);
                        }
                        services.sort(Comparator.comparing(Service::getName, Collator.getInstance(new Locale("vi", "VN"))));

                    }
                    Log.e("ServicesCountInUtils", String.valueOf((long) services.size()));
                    Log.e("homeidInUtils", idHomeParent);
                    listener.onServicesLoaded(services);
                });


    }
    public interface OnServicesLoadedListener {
        void onServicesLoaded(List<Service> services);
    }

    public static List<Bitmap> getImageLibraryData (Context context){
        List<Bitmap> images = new ArrayList<>();
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_electricity));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_water));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_wifi));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_motor));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_service));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_guard));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_sweep));
        images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.image_waste));

        for(Bitmap image : images) {
            image = customSizeImage(image);
        }
        return images;
    }

    public static Bitmap customSizeImage(Bitmap bitmap){
        String encodeImage = encodedImage(bitmap);
        return getConversionImageAndSize(encodeImage, 60, 60);
    }


}
