package edu.poly.nhtr.Activity;



import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText edt_pass, edt_newPass, edt_newPassConf;
    Button btn_changePass;
    ImageView back;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edt_pass = findViewById(R.id.edt_Password);
        edt_newPass = findViewById(R.id.edt_NewPassword);
        edt_newPassConf = findViewById(R.id.edt_NewPassConfirm);
        back = findViewById(R.id.img_back);
        btn_changePass = findViewById(R.id.btn_Change_Password);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }

    private void setListener() {
        // Change password button click listener
        btn_changePass.setOnClickListener(view -> {
            String oldPassword = edt_pass.getText().toString();
            String newPassword = edt_newPass.getText().toString();
            String confirmPassword = edt_newPassConf.getText().toString();

            // Validate input fields
            if (!oldPassword.equals(preferenceManager.getString(Constants.KEY_PASSWORD))) {
                showToast("Mật khẩu không chính xác");
            } else if (newPassword.isEmpty()) {
                showToast("Hãy nhập mật khẩu mới");
            } else if (newPassword.equals(oldPassword)) {
                showToast("Mật khẩu mới trùng với mật khẩu cũ");
            } else if (!newPassword.equals(confirmPassword)) {
                showToast("Mật khẩu nhập lại không khớp");
            } else {
                // Reauthenticate user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), oldPassword);
                    user.reauthenticate(credential)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Log reauthentication success
                                    Log.d("ChangePasswordActivity", "Reauthentication successful");

                                    // Update password in Firebase Authentication
                                    user.updatePassword(newPassword)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    showToast("Đổi mật khẩu thành công");
                                                    // Update password in local storage
                                                    preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);

                                                    // Update password in Firestore
                                                    updatePasswordInFirestore(user.getUid(), newPassword);

                                                    finish();
                                                } else {
                                                    showToast("Đổi mật khẩu thất bại");
                                                }
                                            });
                                } else {
                                    showToast("Xác thực người dùng thất bại");
                                    // Log reauthentication failure
                                    Log.e("ChangePasswordActivity", "Reauthentication failed", task.getException());
                                }
                            });
                } else {
                    // Log user is null
                    Log.e("ChangePasswordActivity", "User is null");
                    showToast("Người dùng không tồn tại");
                }
            }
        });

        // Back button click listener
        back.setOnClickListener(v -> onBackPressed());
    }


        private void showToast(String message) {
            Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_SHORT).show();
        }

    private void updatePasswordInFirestore(String userId, String newPassword) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(userId);
        documentReference.update(Constants.KEY_PASSWORD, newPassword)
                .addOnSuccessListener(unused -> showToast("Cập nhật mật khẩu thành công"))
                .addOnFailureListener(e -> showToast("Không cập nhật được mật khẩu"));
    }

}