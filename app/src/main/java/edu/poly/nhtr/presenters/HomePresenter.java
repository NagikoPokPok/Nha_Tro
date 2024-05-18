package edu.poly.nhtr.presenters;

import android.app.Dialog;
import android.view.Gravity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.utilities.Constants;


public class HomePresenter {

    private HomeListener homeListener;

    private Dialog dialog;
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
            homeListener.showErrorMessage("Nhập tên nhà trọ", R.id.layout_name_home);
        } else if (home.getAddressHome().isEmpty()) {
            homeListener.showToast("Enter home address");
            homeListener.showErrorMessage("Nhập địa chỉ nhà trọ", R.id.layout_address_home);
        } else {
            checkDuplicateData(home);
        }
    }

    private void checkDuplicateData(Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_add_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                            String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                            String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                            if (nameFromFirestore != null && nameFromFirestore.equalsIgnoreCase(home.getNameHome()) && userIdFromFirestore.equals(homeListener.getInfoUserFromGoogleAccount())) {
                                homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                                homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                                homeListener.showToast("Tên nhà đã tồn tại");
                                return;
                            }
                            if (addressFromFirestore != null && addressFromFirestore.equalsIgnoreCase(home.getAddressHome()) && userIdFromFirestore.equals(homeListener.getInfoUserFromGoogleAccount())) {
                                homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                                homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
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
        homeListener.showLoadingOfFunctions(R.id.btn_add_home);
        HashMap<String, Object> homeInfo = new HashMap<>();
        homeInfo.put(Constants.KEY_NAME_HOME, home.getNameHome());
        homeInfo.put(Constants.KEY_ADDRESS_HOME, home.getAddressHome());
        homeInfo.put(Constants.KEY_TIMESTAMP, new Date());
        homeInfo.put(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount()); // Sử dụng ID người dùng Google

        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_HOMES)
                .add(homeInfo)
                .addOnSuccessListener(documentReference -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                    homeListener.putHomeInfoInPreferences(home.getNameHome(), home.getAddressHome(), documentReference);
                    homeListener.showToast("Thêm nhà trọ thành công");
                    getHomes();
                    homeListener.dialogClose();
                })
                .addOnFailureListener(e -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
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


    public void deleteHome(Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_delete_home);
        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_HOMES)
                .document(home.getIdHome())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                    // Xoá thành công, thông báo và cập nhật giao diện
                    getHomes(); // Cập nhật danh sách nhà trọ
                    homeListener.dialogClose();
                    homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                })
                .addOnFailureListener(e -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                    // Xoá thất bại, thông báo lỗi
                    homeListener.showToast("Xoá nhà trọ thất bại");
                });
    }

    public void updateHome(String newNameHome, String newAddressHome, Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_update_home);
        if (newNameHome.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_update_home);
            homeListener.showToast("Nhập tên nhà trọ");
        } else if (newAddressHome.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_update_home);
            homeListener.showToast("Nhập địa chỉ nhà trọ");
        } else {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_HOMES)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                                String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                                String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                                String homeIdFromFirestore = document.getId();
                                if (nameFromFirestore != null && nameFromFirestore.toLowerCase().equals(newNameHome.toLowerCase()) && userIdFromFirestore.equals(homeListener.getInfoUserFromGoogleAccount()) && !homeIdFromFirestore.equals(home.getIdHome())) {
                                    homeListener.hideLoadingOfFunctions(R.id.btn_update_home);
                                    homeListener.showToast("Tên nhà đã tồn tại");
                                    return;
                                }
                                if (addressFromFirestore != null && addressFromFirestore.toLowerCase().equals(newAddressHome.toLowerCase()) && userIdFromFirestore.equals(homeListener.getInfoUserFromGoogleAccount()) && !homeIdFromFirestore.equals(home.getIdHome())) {
                                    homeListener.hideLoadingOfFunctions(R.id.btn_update_home);
                                    homeListener.showToast("Địa chỉ nhà đã tồn tại");
                                    return;
                                }
                            }
                            homeListener.hideLoadingOfFunctions(R.id.btn_update_home);
                            homeListener.openConfirmUpdateHome(Gravity.CENTER, newNameHome, newAddressHome, home);
                        } else {
                            // Handle errors
                        }
                    });
        }
    }

    public void updateSuccess(String newNameHome, String newAddressHome, Home home)
    {
        homeListener.showLoadingOfFunctions(R.id.btn_confirm_update_home);
        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put(Constants.KEY_NAME_HOME, newNameHome);
        updateInfo.put(Constants.KEY_ADDRESS_HOME, newAddressHome);

        // Thực hiện cập nhật dữ liệu trong Firestore
        FirebaseFirestore.getInstance()
                .collection(Constants.KEY_COLLECTION_HOMES)
                .document(home.getIdHome())
                .update(updateInfo)
                .addOnSuccessListener(aVoid -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                    getHomes(); // Cập nhật danh sách nhà trọ
                    homeListener.dialogClose();
                    homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);

                })
                .addOnFailureListener(e -> {
                    // Cập nhật thất bại, thông báo lỗi
                    homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                    homeListener.showToast("Cập nhật thông tin nhà trọ thất bại");
                });
    }

}



