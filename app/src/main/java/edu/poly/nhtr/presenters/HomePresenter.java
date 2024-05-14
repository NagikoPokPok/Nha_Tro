package edu.poly.nhtr.presenters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.utilities.Constants;


public class HomePresenter {

    private HomeListener homeListener;
    private int count;

    public HomePresenter(HomeListener homeListener) {
        this.homeListener = homeListener;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addHome(Home home) {
        if (home.getNameHome().isEmpty()) {
            homeListener.showToast("Enter home name");
        } else if (home.getAddressHome().isEmpty()) {
            homeListener.showToast("Enter home address");
        } else {
            checkDuplicateData(home);
        }
    }

    private void checkDuplicateData(Home home) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                            String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                            if (nameFromFirestore != null && nameFromFirestore.toLowerCase().equals(home.getNameHome().toLowerCase())) {
                                homeListener.showToast("Tên nhà đã tồn tại");
                                return;
                            }
                            if (addressFromFirestore != null && addressFromFirestore.toLowerCase().equals(home.getAddressHome().toLowerCase())) {
                                homeListener.showToast("Địa chỉ nhà đã tồn tại");
                                return;
                            }
                        }
                        // Không có trùng lặp, thêm dữ liệu mới vào Firestore
                        addHomeToFirestore(home);
                    } else {
                        // Handle errors
                    }
                });
    }


    private void addHomeToFirestore(Home home) {
        HashMap<String, Object> homeInfo = new HashMap<>();
        homeInfo.put(Constants.KEY_NAME_HOME, home.getNameHome());
        homeInfo.put(Constants.KEY_ADDRESS_HOME, home.getAddressHome());
        homeInfo.put(Constants.KEY_TIMESTAMP, new Date());
        homeInfo.put(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount()); // Sử dụng ID người dùng Google

        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_HOMES)
                .add(homeInfo)
                .addOnSuccessListener(documentReference -> {
                    homeListener.putHomeInfoInPreferences(home.getNameHome(), home.getAddressHome(), documentReference);
                    homeListener.showToast("Thêm nhà trọ thành công");
                    getHomes();
                    homeListener.dialogClose();
                })
                .addOnFailureListener(e -> {
                    homeListener.showToast("Add failed");
                    homeListener.hideLoading();
                });
    }


    public void getHomes() {
        homeListener.showLoading();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    // Kiểm tra fragment đã được gắn kết với activity chưa
                    if (homeListener.isAdded2()) {
                        homeListener.hideLoading();

                        if (task.isSuccessful() && task.getResult() != null) {

                            List<Home> homes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Duyệt qua document và lấy danh sách các nhà trọ
                                Home home = new Home();
                                home.nameHome = document.getString(Constants.KEY_NAME_HOME);
                                home.addressHome = document.getString(Constants.KEY_ADDRESS_HOME);
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
                                homeListener.addHomeFailed();
                        }
                    }

                });
    }


    public void deleteHome(Home home)
    {
        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_HOMES)
                .document(home.getIdHome())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Xoá thành công, thông báo và cập nhật giao diện
                    homeListener.showToast("Xoá nhà trọ thành công");
                    getHomes(); // Cập nhật danh sách nhà trọ
                })
                .addOnFailureListener(e -> {
                    // Xoá thất bại, thông báo lỗi
                    homeListener.showToast("Xoá nhà trọ thất bại");
                });
    }


}



