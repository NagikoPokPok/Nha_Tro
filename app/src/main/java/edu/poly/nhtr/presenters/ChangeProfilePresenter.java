package edu.poly.nhtr.presenters;

import android.content.Intent;
import android.provider.MediaStore;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;

import edu.poly.nhtr.Activity.ChangeProfileActivity;
import edu.poly.nhtr.interfaces.ChangeProfileInterface;
import edu.poly.nhtr.utilities.Constants;

public class ChangeProfilePresenter {
    ChangeProfileInterface view;
    ChangeProfileActivity activity;

    public ChangeProfilePresenter(ChangeProfileInterface view, ChangeProfileActivity activity) {
        this.view = view;
        this.activity = activity;
    }

    public void clickSave() {
        if(isValidChangeDetails()){
            updateProfile();
        }
    }
    private Boolean isValidChangeDetails() {
        String ten = activity.name.getText().toString().trim();
        String phoneNumber = activity.phoneNum.getText().toString().trim();


        if (!ten.matches("^[\\p{L}\\s]+$")) {
            //warning.setText("Tên chỉ được xuất hiện các kí tự là chữ và số");
            view.showErrorMessage("Tên chỉ được điền các kí tự chữ cái");
            return false;
        } else if (activity.phoneNum.getText().toString().trim().isEmpty()) {
            //warning.setText("Số điện thoại không được để trống");
            view.showErrorMessage("Số điện thoai không được để trống");
            return false;
        } else if (!phoneNumber.matches("^0[0-9]{9}$")) {
            //warning.setText("Số điện thoại chỉ gồm những kí tự là số từ 0-9");
            view.showErrorMessage("Số điện thoại có 10 kí tự và chỉ gồm những kí tự là số từ 0-9");
            return false;
        } else {
            return true;
        }
    }

    private void updateProfile() {
        view.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_EMAIL, activity.preferenceManager.getString(Constants.KEY_EMAIL));
        user.put(Constants.KEY_PASSWORD, activity.preferenceManager.getString(Constants.KEY_PASSWORD));
        user.put(Constants.KEY_NAME, activity.name.getText().toString());
        user.put(Constants.KEY_PHONE_NUMBER, activity.phoneNum.getText().toString());
        user.put(Constants.KEY_ADDRESS, activity.diachi.getText().toString());
        user.put(Constants.KEY_IMAGE, activity.encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS).document(activity.preferenceManager.getString(Constants.KEY_USER_ID))
                .set(user)
                .addOnSuccessListener(unused -> {
                    view.hideLoading();
                    view.changeSuccess();
                    RefreshPrefernceManager();
                })
                .addOnFailureListener(e -> {
                    view.hideLoading();
                    view.showErrorMessage("Cập nhật tông tin thất bại");
                });
    }

    private  void RefreshPrefernceManager(){
        activity.preferenceManager.putString(Constants.KEY_NAME, activity.name.getText().toString());
        activity.preferenceManager.putString(Constants.KEY_PHONE_NUMBER, activity.phoneNum.getText().toString());
        activity.preferenceManager.putString(Constants.KEY_ADDRESS, activity.diachi.getText().toString());
        activity.preferenceManager.putString(Constants.KEY_IMAGE, activity.encodedImage);
    }


    public void clickImageProfile() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.pickImage.launch(intent);
    }


}
