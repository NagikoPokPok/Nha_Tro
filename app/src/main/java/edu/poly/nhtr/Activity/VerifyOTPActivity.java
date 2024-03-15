package edu.poly.nhtr.Activity;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import edu.poly.nhtr.R;


public class VerifyOTPActivity extends AppCompatActivity {



    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private TextView resend;
    private String verificationId;
    private int selectedETPosition = 0;
    private boolean resendEnable = false;

    private int resendTime = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);

        TextView textMobile = findViewById(R.id.textMobile);
        textMobile.setText(String.format(
                "+84-%s", getIntent().getStringExtra("phoneNumber")
        ));

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);
        resend = findViewById(R.id.textResendOTP);

        //setupOTPInputs();
        inputCode1.addTextChangedListener(textWatcher);
        inputCode2.addTextChangedListener(textWatcher);
        inputCode3.addTextChangedListener(textWatcher);
        inputCode4.addTextChangedListener(textWatcher);
        inputCode5.addTextChangedListener(textWatcher);
        inputCode6.addTextChangedListener(textWatcher);

        showKeyboard(inputCode1);
        //Start resend count down timer
        startCountDownTimer();

        resend.setOnClickListener(v -> {
            if (resendEnable) {
                startCountDownTimer();
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        "+84" + getIntent().getStringExtra("phoneNumber"),
                        60, TimeUnit.SECONDS,
                        VerifyOTPActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                                Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onCodeSent(@NonNull String newVerificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                verificationId = newVerificationId;
                                Toast.makeText(VerifyOTPActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });



        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final Button btnVerify = findViewById(R.id.btnVerify);

        verificationId = getIntent().getStringExtra("verificationId");
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputCode1.getText().toString().trim().isEmpty()
                        || inputCode2.getText().toString().trim().isEmpty()
                        || inputCode3.getText().toString().trim().isEmpty()
                        || inputCode4.getText().toString().trim().isEmpty()
                        || inputCode5.getText().toString().trim().isEmpty()
                        || inputCode6.getText().toString().trim().isEmpty()) {
                    Toast.makeText(VerifyOTPActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = inputCode1.getText().toString() +
                        inputCode2.getText().toString() +
                        inputCode3.getText().toString() +
                        inputCode4.getText().toString() +
                        inputCode5.getText().toString() +
                        inputCode6.getText().toString();
                if (verificationId != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    btnVerify.setVisibility(View.INVISIBLE);
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationId,
                            code
                    );
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                btnVerify.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(VerifyOTPActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }
        });

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

    private void showKeyboard(EditText otpET)
    {
        otpET.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(otpET, InputMethodManager.SHOW_IMPLICIT);
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
}