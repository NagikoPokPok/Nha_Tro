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

    private final HomeListener homeListener;
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
            homeListener.showErrorMessage("Nhập tên nhà trọ", R.id.layout_name_home);
        } else if (home.getAddressHome().isEmpty()) {
            homeListener.showErrorMessage("Nhập địa chỉ nhà trọ", R.id.layout_address_home);
        } else {
            checkDuplicateData(home, () -> addHomeToFirestore(home));
        }
    }

    private Boolean isDuplicate(String fieldFromFirestore, String fieldFromHome, String userIdFromFirestore, Home home) {
        return fieldFromFirestore != null && fieldFromFirestore.equalsIgnoreCase(fieldFromHome) && userIdFromFirestore.equals(homeListener.getInfoUserFromGoogleAccount());
    }

    private void checkDuplicateData(Home home, Runnable onSuccess) {
        homeListener.showLoadingOfFunctions(R.id.btn_add_home);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                    String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                    String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                    if(isDuplicate(nameFromFirestore, home.getNameHome(), userIdFromFirestore, home) && isDuplicate(addressFromFirestore, home.getAddressHome(), userIdFromFirestore, home))
                    {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        return;
                    }
                    if (isDuplicate(nameFromFirestore, home.getNameHome(), userIdFromFirestore, home)) {
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        return;
                    }
                    if (isDuplicate(addressFromFirestore, home.getAddressHome(), userIdFromFirestore, home)) {
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        return;
                    }
                }
                onSuccess.run();
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
        homeInfo.put(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount());

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_HOMES)
                .add(homeInfo)
                .addOnSuccessListener(documentReference -> {
                    homeListener.putHomeInfoInPreferences(home.getNameHome(), home.getAddressHome(), documentReference);
                    homeListener.showToast("Thêm nhà trọ thành công");
                    getHomes();
                    homeListener.dialogClose();
                    homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                })
                .addOnFailureListener(e -> {
                    homeListener.showToast("Add failed");
                    homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                });
    }

    public void getHomes() {
        homeListener.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_USER_ID, homeListener.getInfoUserFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (homeListener.isAdded2()) {
                        homeListener.hideLoading();
                        if (task.isSuccessful() && task.getResult() != null) {
                            List<Home> homes = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
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
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_HOMES)
                .document(home.getIdHome())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    getHomes();
                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                    homeListener.dialogClose();
                    homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                })
                .addOnFailureListener(e -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_delete_home);
                    homeListener.showToast("Xoá nhà trọ thất bại");

                });
    }

    public void updateHome(String newNameHome, String newAddressHome, Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_add_home);
        if (newNameHome.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
            homeListener.showErrorMessage("Nhập tên nhà trọ", R.id.layout_name_home);
        } else if (newAddressHome.isEmpty()) {
            homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
            homeListener.showErrorMessage("Nhập địa chỉ nhà trọ", R.id.layout_address_home);
        } else {
            checkDuplicateDataForUpdate(newNameHome, newAddressHome, home);
        }
    }

    private void checkDuplicateDataForUpdate(String newNameHome, String newAddressHome, Home home) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_HOMES).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameFromFirestore = document.getString(Constants.KEY_NAME_HOME);
                    String addressFromFirestore = document.getString(Constants.KEY_ADDRESS_HOME);
                    String userIdFromFirestore = document.getString(Constants.KEY_USER_ID);
                    String homeIdFromFirestore = document.getId();

                    if(isDuplicate(nameFromFirestore, newNameHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome()) && isDuplicate(addressFromFirestore, newAddressHome, userIdFromFirestore, home))
                    {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        return;
                    }

                    if (isDuplicate(nameFromFirestore, newNameHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome())) {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Tên nhà đã tồn tại", R.id.layout_name_home);
                        return;
                    }
                    if (isDuplicate(addressFromFirestore, newAddressHome, userIdFromFirestore, home) && !homeIdFromFirestore.equals(home.getIdHome())) {
                        homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                        homeListener.showErrorMessage("Địa chỉ nhà đã tồn tại", R.id.layout_address_home);
                        return;
                    }
                }
                homeListener.hideLoadingOfFunctions(R.id.btn_add_home);
                homeListener.openConfirmUpdateHome(Gravity.CENTER, newNameHome, newAddressHome, home);

            } else {
                // Handle errors
            }
        });
    }

    public void updateSuccess(String newNameHome, String newAddressHome, Home home) {
        homeListener.showLoadingOfFunctions(R.id.btn_confirm_update_home);
        HashMap<String, Object> updateInfo = new HashMap<>();
        updateInfo.put(Constants.KEY_NAME_HOME, newNameHome);
        updateInfo.put(Constants.KEY_ADDRESS_HOME, newAddressHome);

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_HOMES)
                .document(home.getIdHome())
                .update(updateInfo)
                .addOnSuccessListener(aVoid -> {
                    getHomes();
                    homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                    homeListener.dialogClose();
                    homeListener.openDialogSuccess(R.layout.layout_dialog_delete_home_success);
                })
                .addOnFailureListener(e -> {
                    homeListener.hideLoadingOfFunctions(R.id.btn_confirm_update_home);
                    homeListener.showToast("Cập nhật thông tin nhà trọ thất bại");
                });
    }
}
