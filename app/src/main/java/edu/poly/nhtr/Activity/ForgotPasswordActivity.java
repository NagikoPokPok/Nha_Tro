package edu.poly.nhtr.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;

public class ForgotPasswordActivity extends AppCompatActivity {
    Button setPassword;


    ImageView back;
    TextView email,warning;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setPassword = findViewById(R.id.btn_DatLaiMK);
        email = findViewById(R.id.edt_emailForgotPassword);
        back = findViewById(R.id.img_back);
        warning = findViewById(R.id.txt_warning);
        progressDialog = new ProgressDialog(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this,SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                FirebaseAuth auth = FirebaseAuth.getInstance();

                String emailAddress = email.getText().toString();

                String[] strings = emailAddress.split("@");
                if(strings.length!=2 || !strings[1].equals("gmail.com")) {
                    progressDialog.dismiss();
                    warning.setText("x Email không hợp lệ");
                    Toast.makeText(ForgotPasswordActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                }else{
                    FirebaseFirestore databse = FirebaseFirestore.getInstance();
                    databse.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo(Constants.KEY_EMAIL, emailAddress)
                            .get()
                            .addOnCompleteListener(Task->{
                                if(Task.isSuccessful() && Task.getResult()!=null && Task.getResult().getDocuments().size() >0){
                                    auth.sendPasswordResetEmail(emailAddress)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    progressDialog.dismiss();
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(ForgotPasswordActivity.this, "Email đã được gửi đi", Toast.LENGTH_SHORT).show();

                                                    } else
                                                        Toast.makeText(ForgotPasswordActivity.this, "Không thể gửi Email", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                                else{
                                    progressDialog.dismiss();
                                    warning.setText("x Tài khoản chưa được đăng kí");
                                    Toast.makeText(ForgotPasswordActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                                }

                            });
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    HashMap<String, Object> user = new HashMap<>();

                }
            }

        });
    }
}