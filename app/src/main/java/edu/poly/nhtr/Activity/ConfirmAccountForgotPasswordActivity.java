package edu.poly.nhtr.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Random;

import edu.poly.nhtr.R;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import papaya.in.sendmail.SendMail;

public class ConfirmAccountForgotPasswordActivity extends AppCompatActivity {
    PreferenceManager preferenceManager;
    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private TextView resend;
    private ImageView back;
    private String verificationId;
    private int selectedETPosition = 0;
    private String email="";
    private int random = 0;
    private boolean resendEnable = false;

    private int resendTime = 60;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_account_forgot_password);
        preferenceManager = new PreferenceManager(getApplicationContext());
        back = findViewById(R.id.img_back);

        email = getIntent().getStringExtra("gmail");

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);
        resend = findViewById(R.id.textResendOTP);

        setupOTPInputs();
        inputCode1.addTextChangedListener(textWatcher);
        inputCode2.addTextChangedListener(textWatcher);
        inputCode3.addTextChangedListener(textWatcher);
        inputCode4.addTextChangedListener(textWatcher);
        inputCode5.addTextChangedListener(textWatcher);
        inputCode6.addTextChangedListener(textWatcher);

        showKeyboard(inputCode1);
        //Start resend count down timer
        startCountDownTimer();

        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final Button btnVerify = findViewById(R.id.btnVerify);

        random();
        resend.setOnClickListener(v -> {
            startCountDownTimer();
            random();
        });

        btnVerify.setOnClickListener(v -> {
            if (checkEmpty()) {
                Toast.makeText(ConfirmAccountForgotPasswordActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();

            } else {
                String code = inputCode1.getText().toString() +
                        inputCode2.getText().toString() +
                        inputCode3.getText().toString() +
                        inputCode4.getText().toString() +
                        inputCode5.getText().toString() +
                        inputCode6.getText().toString();

                if (!code.equals(String.valueOf(random))) {
                    Toast.makeText(ConfirmAccountForgotPasswordActivity.this, "Wrong OTP", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(ConfirmAccountForgotPasswordActivity.this, SetNewPasswordActivity.class);
                    intent.putExtra("gmail", email);
                    startActivity(intent);
                    finish();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(ConfirmAccountForgotPasswordActivity.this, ForgotPasswordActivity.class);
                startActivity(intent2);
                finish();
            }
        });
    }

    void random() {
        Random randomOtp = new Random();
        random = randomOtp.nextInt(900000) + 100000; // Generate a random 6-digit OTP

        SendMail mail = new SendMail("nhatrohomemate@gmail.com", "bpvd hqxd xbho gdyl", email, "Login Signup app's OTP",
                "Your OTP is -> " + random);
        mail.execute();
        //"nhatrohomemate@gmail.com", "orjz scow qdli loqh"
        //"iuxq ggco nwld zvyx
    }
    private void setupOTPInputs()
    {
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty())
                {
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty())
                {
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty())
                {
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty())
                {
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty())
                {
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
    }
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length()>0)
            {
                if(selectedETPosition==0)
                {
                    selectedETPosition=1;
                    showKeyboard(inputCode2);
                }
                else if(selectedETPosition==1)
                {
                    selectedETPosition=2;
                    showKeyboard(inputCode3);
                }
                else if(selectedETPosition==2)
                {
                    selectedETPosition=3;
                    showKeyboard(inputCode4);
                }
                else if(selectedETPosition==3)
                {
                    selectedETPosition=4;
                    showKeyboard(inputCode5);
                }
                else if(selectedETPosition==4)
                {
                    selectedETPosition=5;
                    showKeyboard(inputCode6);
                }
            }

        }
    };
    private void showKeyboard(EditText otpET)
    {
        otpET.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(otpET, InputMethodManager.SHOW_IMPLICIT);
    }
    private void startCountDownTimer()
    {
        resendEnable = false;
        resend.setTextColor(Color.parseColor("#99000000"));

        new CountDownTimer(resendTime*1000, 100){

            @Override
            public void onTick(long millisUntilFinished) {
                resend.setText("Resend Code ("+(millisUntilFinished/1000)+")");

            }

            @Override
            public void onFinish() {
                resendEnable = true;
                resend.setText("Resend Code");
                resend.setTextColor(getResources().getColor(R.color.primary));

            }
        }.start();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DEL)
        {
            if(selectedETPosition == 5)
            {
                selectedETPosition = 4;
                showKeyboard(inputCode5);
            }
            else if(selectedETPosition == 4)
            {
                selectedETPosition = 3;
                showKeyboard(inputCode4);
            }
            else if(selectedETPosition == 3)
            {
                selectedETPosition = 2;
                showKeyboard(inputCode3);
            }
            else if(selectedETPosition == 2)
            {
                selectedETPosition = 1;
                showKeyboard(inputCode2);
            }
            else if(selectedETPosition == 1)
            {
                selectedETPosition = 0;
                showKeyboard(inputCode1);
            }
            return true;

        }
        else {
            return super.onKeyUp(keyCode, event);
        }
    }
    private boolean checkEmpty() {
        if (inputCode1.getText().toString().trim().isEmpty()
                || inputCode2.getText().toString().trim().isEmpty()
                || inputCode3.getText().toString().trim().isEmpty()
                || inputCode4.getText().toString().trim().isEmpty()
                || inputCode5.getText().toString().trim().isEmpty()
                || inputCode6.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }
}