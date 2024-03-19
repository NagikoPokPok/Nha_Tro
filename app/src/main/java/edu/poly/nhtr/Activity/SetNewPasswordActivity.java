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

import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SetNewPasswordActivity extends AppCompatActivity {
    ImageView back;
    EditText newPass, newPassConf;
    Button updatePassword;
    PreferenceManager preferenceManager;
    String email, password;
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
                       password = documentSnapshot.getString(Constants.KEY_PASSWORD);

                       if(mAuth.getCurrentUser()==null) SignInAuth();
                       FirebaseUser user = mAuth.getCurrentUser();
                       user.updatePassword(newPass.getText().toString())
                               .addOnCompleteListener(task1 -> {
                                   if (task1.isSuccessful()) {
                                       Toast.makeText(SetNewPasswordActivity.this, "Cập nhật mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                       //preferenceManager.putString(Constants.KEY_PASSWORD,edt_newPass.getText().toString());
                                       DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
                                       documentReference.update(Constants.KEY_PASSWORD,newPass.getText().toString())
                                               .addOnSuccessListener(unused -> {
                                                   Toast.makeText(SetNewPasswordActivity.this, "Cập nhật mk thành công", Toast.LENGTH_SHORT).show();
                                                   //preferenceManager.putString(Constants.KEY_PASSWORD, newPass.getText().toString());
                                                   ClearAll();
                                               })
                                               .addOnFailureListener(e -> user.updatePassword(preferenceManager.getString(Constants.KEY_PASSWORD))
                                                       .addOnCompleteListener(task11 -> {
                                                           if (task11.isSuccessful()) {
                                                           }
                                                       }));
                                   }else {
                                       Toast.makeText(SetNewPasswordActivity.this, "ERROR ", Toast.LENGTH_SHORT).show();
                                       Log.e("FirestoreError", "Error: " + Objects.requireNonNull(task1.getException()).getMessage());
                                   }
                               });
                   }else Toast.makeText(this, "email không đúng " + email, Toast.LENGTH_SHORT).show();
                });
    }
    private void SignInAuth(){
        Log.e("Account",email + " " + password);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SetNewPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
    private void ClearAll(){
        newPass.setText("");
        newPassConf.setText("");
    }
}