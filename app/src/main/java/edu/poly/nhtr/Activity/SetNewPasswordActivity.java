package edu.poly.nhtr.Activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SetNewPasswordActivity extends AppCompatActivity {
    ImageView back;
    EditText newPass, newPassConf;
    Button updatePassword;
    PreferenceManager preferenceManager;
    String email, password, id, hashPassword;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);
        preferenceManager = new PreferenceManager(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();

        email = getIntent().getStringExtra("gmail");

        init();
        setOnclickListener();
    }

    private void init(){
        back = findViewById(R.id.img_back);
        newPass = findViewById(R.id.edt_NewPassword);
        newPassConf = findViewById(R.id.edt_NewPassConfirm);
        updatePassword = findViewById(R.id.btn_Change_Password);
    }
    private void setOnclickListener(){
        back.setOnClickListener(v -> {
            Intent intent = new Intent(SetNewPasswordActivity.this,ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });
        updatePassword.setOnClickListener(v -> {
            if(newPass.getText().toString().isEmpty())
                Toast.makeText(this, "Hãy Nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            else if(newPassConf.getText().toString().isEmpty())
                Toast.makeText(this, "Hãy nhập lại mật khẩu mới", Toast.LENGTH_SHORT).show();
            else if (!newPass.getText().toString().equals(newPassConf.getText().toString())) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            }else {
                update();
            }
        });
    }
    private void update(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD)); // Assuming this is the stored hashed password

                        // Xóa mật khẩu chưa mã hóa từ PreferenceManager, chỉ sử dụng mật khẩu mã hóa
                        preferenceManager.removePreference(Constants.KEY_PASSWORD);

                        id = documentSnapshot.getId();
                        password = documentSnapshot.getString(Constants.KEY_PASSWORD); // Lấy mật khẩu mã hóa từ Firestore
                        SignInAuth();
                    } else {
                        Toast.makeText(this, "Người dùng với email:  " + email + " không tìm thấy", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void SignInAuth(){
        Log.e("Account", email + " " + password);
        mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), password) // Use the hashed password retrieved from Firestore
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.e(TAG, "signInWithEmail:success");
                        Update();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(SetNewPasswordActivity.this, "Xác thực người dùng thất bại.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void Update(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String newHashedPass = PasswordHasher.hashPassword(newPass.getText().toString());
            user.updatePassword(newHashedPass)
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(SetNewPasswordActivity.this, "Cập nhật mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            UpdateFirestore(newHashedPass); // Pass the new hashed password
                        } else {
                            Toast.makeText(SetNewPasswordActivity.this, "ERROR ", Toast.LENGTH_SHORT).show();
                            Log.e("FirestoreError", "Lỗi: " + Objects.requireNonNull(task1.getException()).getMessage());
                        }
                    });
        } else {
            Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdateFirestore(String newHashedPassword) {
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(id);
        if(documentReference != null) {
            documentReference.update(Constants.KEY_PASSWORD, newHashedPassword) // Update with the new hashed password
                    .addOnSuccessListener(unused -> {
//                        Toast.makeText(SetNewPasswordActivity.this, "Cập nhật mk thành công", Toast.LENGTH_SHORT).show();
                        ClearAll();
                        ChangeActivity("Cập nhật mật khẩu thành công");
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        ChangeActivity("Cập nhật mật kẩu thất bại");
                    });
        } else {
            Toast.makeText(this, "Document reference is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void ClearAll(){
        newPass.setText("");
        newPassConf.setText("");
    }
    private void ChangeActivity(String message){
        Intent intent = new Intent(SetNewPasswordActivity.this, SignInActivity.class);
        intent.putExtra("message", message);
        startActivity(intent);
        finish();
    }
}