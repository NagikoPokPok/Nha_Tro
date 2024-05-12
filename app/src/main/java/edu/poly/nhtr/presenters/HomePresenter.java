package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.utilities.Constants;


public class HomePresenter {

    private HomeListener homeListener;
    public HomePresenter(HomeListener homeListener) {
        this.homeListener = homeListener;
    }

    public void addHome(Home home)
    {
        if (home.getNameHome().isEmpty()) {
            homeListener.showToast("Enter home name");
        } else if (home.getAddressHome().isEmpty()) {
            homeListener.showToast("Enter home address");
        } else {

            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> homeInfo = new HashMap<>();
            homeInfo.put(Constants.KEY_NAME_HOME, home.getNameHome());
            homeInfo.put(Constants.KEY_ADDRESS, home.getAddressHome());
            homeInfo.put(Constants.KEY_TIMESTAMP, new Date());
            homeInfo.put(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount()); // Sử dụng ID người dùng Google
            database.collection(Constants.KEY_COLLECTION_HOMES)
                    .add(homeInfo)
                    .addOnSuccessListener(documentReference -> {
                        homeListener.putHomeInfoInPreferences(home.getNameHome(), home.getAddressHome(), documentReference);
                        homeListener.showToast("Add Success");
                        getHomes();
                        homeListener.dialogClose();
                    })
                    .addOnFailureListener(e -> {
                        homeListener.showToast("Add failed");
                        homeListener.hideLoading();
                    });
        }

    }

    public void getHomes() {
        homeListener.showLoading();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                     // Kiểm tra fragment đã được gắn kết với activity chưa
                    if(homeListener.isAdded2()) {
                        homeListener.hideLoading();

                        if (task.isSuccessful() && task.getResult() != null) {

                            List<Home> homes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Duyệt qua document và lấy danh sách các nhà trọ
                                Home home = new Home();
                                home.nameHome = document.getString(Constants.KEY_NAME_HOME);
                                home.addressHome = document.getString(Constants.KEY_ADDRESS);
                                home.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                home.idHome = document.getId();
                                homes.add(home);
                            }
                            if (!homes.isEmpty()) {
                                homeListener.addHome(homes);
                            } else {
                                homeListener.addHomeFailed();
                            }
                        } else {

                        }
                    }

                });
    }


}
