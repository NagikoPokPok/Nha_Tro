package edu.poly.nhtr.presenters;

import android.text.TextUtils;
import android.util.Patterns;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.interfaces.SignUpInterface;
import edu.poly.nhtr.models.User;
import edu.poly.nhtr.utilities.Constants;

public class SignUpPresenter {

    private SignUpInterface view;

    public SignUpPresenter(SignUpInterface view) {
        this.view = view;
    }

    public void signUp(User user) {
        if (isValidSignUpDetails(user.getName(), user.getEmail(), user.getPassword(), user.getConfirmPassword())) {
            checkExisted(user);
        }
    }

    private void checkExisted(User user) {
        view.showLoading();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int documentCount = task.getResult().getDocuments().size();
                        if (documentCount > 0) {
                            view.showErrorMessage("Email đã được sử dụng");
                            view.setEmailErrorMessage("Email đã được sử dụng");
                            view.hideLoading();
                        } else {
                            String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
                            view.showSuccessMessage(user.getEmail(), user.getName(), hashedPassword);
                            view.hideLoading();
                        }
                    } else {
                        view.showErrorMessage("Lỗi truy vấn, vui lòng thử lại");
                        view.hideLoading();
                    }
                });
    }

    private boolean isValidSignUpDetails(String name, String email, String password, String confirmPassword) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher matcher = pattern.matcher(password);
        boolean isPwdContainsSpeChar = matcher.find();
        if (TextUtils.isEmpty(name)) {
            view.showErrorMessage("Hãy nhập tên");
            view.setNameErrorMessage("Không được bỏ trống");
            return false;
        } else if (TextUtils.isEmpty(email)) {
            view.showErrorMessage("Hãy nhập email");
            view.setEmailErrorMessage("Không được bỏ trống");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showErrorMessage("Nhập email hợp lệ");
            view.setEmailErrorMessage("Email không hợp lệ");
            return false;
        } else if (TextUtils.isEmpty(password)) {
            view.showErrorMessage("Hãy nhập mật khẩu");
            view.setPasswordErrorMessage("Không được bỏ trống");
            return false;
        } else if (password.length() < 8 || password.length() > 32 || !isPwdContainsSpeChar ) {
            view.showErrorMessage("Hãy nhập mật khẩu");
            view.setPasswordErrorMessage("Mật khẩu không hợp lệ");
            return false;
        }else if (TextUtils.isEmpty(confirmPassword)) {
            view.showErrorMessage("Xác nhận mật khẩu");
            view.setConfirmPasswordErrorMessage("Không được bỏ trống");
            return false;
        } else if (!password.equals(confirmPassword)) {
            view.showErrorMessage("Password and confirm must be the same");
            view.setConfirmPasswordErrorMessage("Mật khẩu chưa trùng nhau");
            return false;
        }
        return true;
    }

    public void handlePasswordChanged(String password, String confirmPassword) {
        if(!confirmPassword.isEmpty() && !confirmPassword.equals(password))
        {
            view.setConfirmPasswordText("");
        }
        if (password.length() < 8) {
            view.setPasswordHelperText("Nhập ít nhất 8 ký tự");
            view.setPasswordError("");
        } else if (password.length() >= 8 && password.length() <= 32) {
            Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
            Matcher matcher = pattern.matcher(password);
            boolean isPwdContainsSpeChar = matcher.find();
            if (isPwdContainsSpeChar) {
                view.setPasswordHelperText("Mật khẩu mạnh");
                view.setPasswordError("");
            } else {
                view.setPasswordHelperText("");
                view.setPasswordError("Mật khẩu yếu, nhập ít nhất 1 ký tự đặc biệt");
            }
        } else {
            view.setPasswordHelperText("");
            view.setPasswordError("Tối đa 32 ký tự");
        }
    }

    public void handleConfirmPasswordChanged(String password, String confirmPassword) {

        if (confirmPassword.isEmpty()) {
            // Xóa thông báo lỗi và trợ giúp
            view.setConfirmPasswordHelperText("");
            view.setConfirmPasswordError("");
        } else if (confirmPassword.equals(password)) {
            // Hiển thị thông báo xác nhận thành công
            view.setConfirmPasswordHelperText("Xác minh mật khẩu chính xác");
            view.setConfirmPasswordError("");
        } else {
            // Hiển thị thông báo lỗi xác nhận mật khẩu
            view.setConfirmPasswordHelperText("");
            view.setConfirmPasswordError("Mật khẩu không trùng khớp");
        }
    }

    public void handleNameChanged(String name)
    {
        if(name.length()>=0) {
            view.setNameErrorEnabled(false);
        }
    }

    public void handleEmailChanged(String email)
    {
        if(email.length()>=0) {
            view.setEmailErrorEnabled(false);
        }
    }




}
