package edu.poly.nhtr.Activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangePasswordActivity extends AppCompatActivity {
    EditText edt_pass,edt_newPass, edt_newPassConf;
    Button btn_changePass;
    ImageView back;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        edt_newPass = findViewById(R.id.edt_Password);
        edt_newPass = findViewById(R.id.edt_NewPassword);
        edt_newPassConf = findViewById(R.id.edt_NewPassConfirm);
        back = findViewById(R.id.img_back);
        btn_changePass = findViewById(R.id.btn_Change_Password);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListener();
    }
    private void setListener(){
        btn_changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edt_pass.getText().toString().equals(preferenceManager.getString(Constants.KEY_PASSWORD))){
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                }else if(edt_newPass.getText().toString().isEmpty())
                    Toast.makeText(ChangePasswordActivity.this, "Hãy nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                else if(edt_newPass.getText().toString().equals(edt_pass.getText().toString()))
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu mới trùng với mật khẩu cũ", Toast.LENGTH_SHORT).show();
                else if(!edt_newPass.getText().toString().equals(edt_newPassConf.getText().toString()))
                    Toast.makeText(ChangePasswordActivity.this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
                else{
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    user.updatePassword(edt_pass.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                    }else
                                        Toast.makeText(ChangePasswordActivity.this, "ERROR ", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}