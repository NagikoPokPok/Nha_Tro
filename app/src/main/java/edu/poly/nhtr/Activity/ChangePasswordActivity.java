package edu.poly.nhtr.Activity;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityChangePasswordBinding;
import edu.poly.nhtr.databinding.ActivityChangeProfileBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText edt_pass, edt_newPass, edt_newPassConf;
    ImageView back;
    private FirebaseAuth mAuth;
    private PreferenceManager preferenceManager;
    private ActivityChangePasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edt_pass = findViewById(R.id.edt_Password);
        edt_newPass = findViewById(R.id.edt_NewPassword);
        edt_newPassConf = findViewById(R.id.edt_NewPassConfirm);
        back = findViewById(R.id.img_back);
        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        binding.getRoot();
        setListener();
    }

    private void setListener() {
        binding.btnChangePass.setOnClickListener(v -> {
            String oldInputPassword = PasswordHasher.hashPassword(edt_pass.getText().toString());
            String oldPassword = preferenceManager.getString(Constants.KEY_PASSWORD);
            String newPassword = PasswordHasher.hashPassword(edt_newPass.getText().toString());
            String reNewPassword = PasswordHasher.hashPassword(edt_newPassConf.getText().toString());
            if (!oldInputPassword.equals(oldPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
            } else if (edt_newPass.getText().toString().isEmpty()) {
                Toast.makeText(ChangePasswordActivity.this, "Hãy nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            } else if (edt_newPass.getText().toString().length()<6) {
                Toast.makeText(this, "Mật khẩu mới phải đảm bảo lớn hơn hoặc bằng 6 kí tự", Toast.LENGTH_SHORT).show();
            } else if (newPassword.equals(oldPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu mới trùng với mật khẩu cũ", Toast.LENGTH_SHORT).show();
            } else if (!newPassword.equals(reNewPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            } else {
                if (mAuth.getCurrentUser() == null) SignInAgain();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.updatePassword(newPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                //Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
                                documentReference.update(Constants.KEY_PASSWORD, newPassword)
                                        .addOnSuccessListener(unused -> {
                                            preferenceManager.putString(Constants.KEY_PASSWORD, newPassword);
                                            ClearAll();
                                            ChangeActivity("Đổi mật khẩu thành công");
                                        })
                                        .addOnFailureListener(e -> {
                                            user.updatePassword(preferenceManager.getString(Constants.KEY_PASSWORD))
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                        }
                                                    });
                                            ChangeActivity("Đổi mật khẩu thất bại");
                                        });

                            } else {
                                Toast.makeText(ChangePasswordActivity.this, "Lỗi ", Toast.LENGTH_SHORT).show();
                                Log.e("FirestoreError", "Lỗi: " + task.getException().getMessage());
                                SignInAgain();
                            }
                        });
            }
        });

        back.setOnClickListener(v -> onBackPressed());
    }

    private void ClearAll() {
        edt_pass.setText("");
        edt_newPass.setText("");
        edt_newPassConf.setText("");
    }
    private void ChangeActivity(String message){
        Intent intent = new Intent(ChangePasswordActivity.this, ChangeProfileActivity.class);
        intent.putExtra("message", message);
        Log.e("Message", message);
        startActivity(intent);
        finish();
    }

    private void SignInAgain() {
        mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), Objects.requireNonNull(PasswordHasher.hashPassword(preferenceManager.getString(Constants.KEY_PASSWORD))))
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(ChangePasswordActivity.this, "Xác thực người dùng thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}