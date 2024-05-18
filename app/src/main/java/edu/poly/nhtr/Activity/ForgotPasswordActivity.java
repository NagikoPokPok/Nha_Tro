package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.ForgotPasswordInterface;
import edu.poly.nhtr.presenters.ForgotPasswordPresenter;

public class ForgotPasswordActivity extends AppCompatActivity implements ForgotPasswordInterface {
    Button btn_SetPassword;

    ImageView back;
    TextView email,warning;
    ProgressBar progressBar;
    private ForgotPasswordPresenter forgotPasswordPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        btn_SetPassword = findViewById(R.id.btn_DatLaiMK);
        email = findViewById(R.id.edt_emailForgotPassword);
        back = findViewById(R.id.img_back);
        warning = findViewById(R.id.txt_warning);

        forgotPasswordPresenter = new ForgotPasswordPresenter(this);

        progressBar = findViewById(R.id.progressBar);

        setListener();
    }



    private void setListener() {
        // Nút quay lại
        back.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this,SignInActivity.class);
            startActivity(intent);
            finish();
        });

        //Nút đặt lại mật khẩu
        btn_SetPassword.setOnClickListener(v -> {
            String emailAddress = email.getText().toString();
            forgotPasswordPresenter.setNewPassword(emailAddress);
        });
    }

    @Override
    public void success() {
        Intent intent = new Intent(ForgotPasswordActivity.this, ConfirmAccountForgotPasswordActivity.class);
        intent.putExtra("gmail",email.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    @Override
    public void error() {

    }

    @Override
    public void showLoading() {
        btn_SetPassword.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        btn_SetPassword.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}