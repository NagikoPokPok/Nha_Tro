package edu.poly.nhtr.presenters;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.SignUpInterface;
import edu.poly.nhtr.models.User;
import edu.poly.nhtr.utilities.Constants;

public class SignUpPresenter {

    private SignUpInterface view;
    private GoogleSignInClient googleSignInClient;
    private static final String TAG = "SignUpActivity";
    private final int RC_SIGN_IN = 20;

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

    // Đăng nhập bằng Google
    public void googleSignIn() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(view.getStringFromResources(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(view.getContext(), googleSignInOptions);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            view.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                    if (googleSignInAccount != null) {
                        firebaseAuth(googleSignInAccount.getIdToken());
                    } else {
                        view.showToast("Đăng nhập thất bại");
                    }
                } catch (ApiException e) {
                    view.showToast("Đăng nhập thất bại: " + e.getMessage());
                }
            } else {
                view.showToast("Có sự cố xảy ra khi đăng nhập");
            }
        }
    }

    public void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        // Lưu thông tin người dùng vào Firestore
                        HashMap<String, Object> userData = new HashMap<>();
                        userData.put(Constants.KEY_NAME, user.getDisplayName());
                        userData.put(Constants.KEY_EMAIL, user.getEmail());
                        userData.put(Constants.KEY_IMAGE, user.getPhotoUrl().toString());
                        // Thêm các thông tin khác nếu cần

                        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                                .document(user.getUid())
                                .set(userData)
                                .addOnSuccessListener(aVoid -> view.notifySignInSuccess())
                                .addOnFailureListener(e -> view.showToast("Có sự cố xảy ra khi lưu thông tin người dùng"));
                    } else {
                        // Đăng nhập thất bại
                        view.showToast("Có sự cố xảy ra khi đăng nhập");
                    }
                });
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
