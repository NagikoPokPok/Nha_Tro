package edu.poly.nhtr.presenters;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import edu.poly.nhtr.Activity.ChangePasswordActivity;
import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.interfaces.ChangePasswordInterface;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangePasswordPresenter {
    ChangePasswordInterface view;
    ChangePasswordActivity activity;
    PreferenceManager preferenceManager;

    public ChangePasswordPresenter(ChangePasswordActivity activity, ChangePasswordInterface view) {
        this.view = view;
        this.activity = activity;
    }
    public void ChangePassword(PreferenceManager preferenceManager){
        this.preferenceManager = preferenceManager;

        String oldInputPassword = PasswordHasher.hashPassword(activity.edt_pass.getText().toString());
        String oldPassword = preferenceManager.getString(Constants.KEY_PASSWORD);
        String newPassword = PasswordHasher.hashPassword(activity.edt_newPass.getText().toString());
        String reNewPassword = PasswordHasher.hashPassword(activity.edt_newPassConf.getText().toString());

        //Kiểm tra và thực hiện
        if (!oldInputPassword.equals(oldPassword)) {
            Toast.makeText(activity, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
        } else if (activity.edt_newPass.getText().toString().isEmpty()) {
            Toast.makeText(activity, "Hãy nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
        } else if (activity.edt_newPass.getText().toString().length()<6) {
            Toast.makeText(activity, "Mật khẩu mới phải đảm bảo lớn hơn hoặc bằng 6 kí tự", Toast.LENGTH_SHORT).show();
        } else if (newPassword.equals(oldPassword)) {
            Toast.makeText(activity, "Mật khẩu mới trùng với mật khẩu cũ", Toast.LENGTH_SHORT).show();
        } else if (!newPassword.equals(reNewPassword)) {
            Toast.makeText(activity, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
        } else {
            perform(newPassword);
        }
    }

    private void perform(String newPassword) {
        if (activity.mAuth.getCurrentUser() == null) SignInAgain();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
                        documentReference.update(Constants.KEY_PASSWORD, newPassword)
                                .addOnSuccessListener(unused -> {
                                    preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
                                    view.ClearAllData();
                                    view.changePasswordSuccess("Đổi mật khẩu thành công");
                                })
                                .addOnFailureListener(e -> {
                                    user.updatePassword(preferenceManager.getString(Constants.KEY_PASSWORD))
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                }
                                            });
                                    view.changePasswordError("Đổi mật khẩu thất bại");
                                });

                    } else {
                        view.changePasswordError("Lỗi");
                        Log.e("FirestoreError", "Lỗi: " + task.getException().getMessage());
                        SignInAgain();
                    }
                });
    }


    private void SignInAgain() {
        activity.mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), Objects.requireNonNull(PasswordHasher.hashPassword(preferenceManager.getString(Constants.KEY_PASSWORD))))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(activity, "Xác thực người dùng thất bại.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
