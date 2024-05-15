package edu.poly.nhtr.Activity;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.ChangePasswordInterface;
import edu.poly.nhtr.presenters.ChangePasswordPresenter;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangePasswordActivity extends AppCompatActivity implements ChangePasswordInterface {
    public EditText edt_pass, edt_newPass, edt_newPassConf;
    View layoutPassword, layoutNewPassword, layoutNewPasswordConfirm;
    ImageView back;
    Button btn_changePassword;
    public FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;
    ChangePasswordPresenter presenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edt_pass = findViewById(R.id.edt_Password);
        edt_newPass = findViewById(R.id.edt_NewPassword);
        edt_newPassConf = findViewById(R.id.edt_NewPassConfirm);
        back = findViewById(R.id.img_back);
        btn_changePassword = findViewById(R.id.btn_change_pass);

        layoutPassword = findViewById(R.id.txt_layout_old_pass);
        layoutNewPassword = findViewById(R.id.txt_layout_new_password);
        layoutNewPasswordConfirm = findViewById(R.id.txt_layout_new_passConf);

        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
//        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        setListener();

        presenter = new ChangePasswordPresenter(this,this);
    }

    private void setListener() {
        btn_changePassword.setOnClickListener(v -> {
            presenter.ChangePassword(preferenceManager);
//            String oldInputPassword = PasswordHasher.hashPassword(edt_pass.getText().toString());
//            String oldPassword = preferenceManager.getString(Constants.KEY_PASSWORD);
//            String newPassword = PasswordHasher.hashPassword(edt_newPass.getText().toString());
//            String reNewPassword = PasswordHasher.hashPassword(edt_newPassConf.getText().toString());
//            if (!oldInputPassword.equals(oldPassword)) {
//                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
//            } else if (edt_newPass.getText().toString().isEmpty()) {
//                Toast.makeText(ChangePasswordActivity.this, "Hãy nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
//            } else if (edt_newPass.getText().toString().length()<6) {
//                Toast.makeText(this, "Mật khẩu mới phải đảm bảo lớn hơn hoặc bằng 6 kí tự", Toast.LENGTH_SHORT).show();
//            } else if (newPassword.equals(oldPassword)) {
//                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu mới trùng với mật khẩu cũ", Toast.LENGTH_SHORT).show();
//            } else if (!newPassword.equals(reNewPassword)) {
//                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
//            } else {
//                if (mAuth.getCurrentUser() == null) SignInAgain();
//
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                user.updatePassword(newPassword)
//                        .addOnCompleteListener(task -> {
//                            if (task.isSuccessful()) {
//                                //Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
//                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
//                                documentReference.update(Constants.KEY_PASSWORD, newPassword)
//                                        .addOnSuccessListener(unused -> {
//                                            preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
//                                            ClearAllData();
//                                            ChangeActivity("Đổi mật khẩu thành công");
//                                        })
//                                        .addOnFailureListener(e -> {
//                                            user.updatePassword(preferenceManager.getString(Constants.KEY_PASSWORD))
//                                                    .addOnCompleteListener(task1 -> {
//                                                        if (task1.isSuccessful()) {
//                                                        }
//                                                    });
//                                            ChangeActivity("Đổi mật khẩu thất bại");
//                                        });
//
//                            } else {
//                                Toast.makeText(ChangePasswordActivity.this, "Lỗi ", Toast.LENGTH_SHORT).show();
//                                Log.e("FirestoreError", "Lỗi: " + task.getException().getMessage());
//                                SignInAgain();
//                            }
//                        });
//            }
        });

        back.setOnClickListener(v -> {
            Intent intent = new Intent(ChangePasswordActivity.this, ChangeProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

//    private void ChangeActivity(String message){
//        Intent intent = new Intent(ChangePasswordActivity.this, ChangeProfileActivity.class);
//        intent.putExtra("message", message);
//        Log.e("Message", message);
//        startActivity(intent);
//        finish();
//    }
//
//    private void SignInAgain() {
//        mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), Objects.requireNonNull(PasswordHasher.hashPassword(preferenceManager.getString(Constants.KEY_PASSWORD))))
//                .addOnCompleteListener(this, task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w(TAG, "signInWithEmail:failure", task.getException());
//                        Toast.makeText(ChangePasswordActivity.this, "Xác thực người dùng thất bại.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    public void changePasswordSuccess(String message) {
        Intent intent = new Intent(ChangePasswordActivity.this, ChangeProfileActivity.class);
        intent.putExtra("message", message);
        Log.e("Message", message);
        startActivity(intent);
        finish();
    }

    @Override
    public void changePasswordError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPasswordErrorMessage(String message) {

    }

    @Override
    public void setNewPasswordErrorMessage(String message) {

    }

    @Override
    public void setConfirmNewPasswordErrorMessage(String message) {

    }

    @Override
    public void setNewPasswordHyperTextMessage(String message) {

    }

    @Override
    public void setNewPasswordConfirmHyperTextMessage(String message) {

    }

    @Override
    public void ClearAllData() {
        edt_pass.setText("");
        edt_newPass.setText("");
        edt_newPassConf.setText("");
    }


    @Override
    public void showLoading() {
        btn_changePassword.setVisibility(View.INVISIBLE);

    }

    @Override
    public void hideLoading() {
        btn_changePassword.setVisibility(View.VISIBLE);
    }

    @Override
    public void setCheckFeedback() {

    }
}