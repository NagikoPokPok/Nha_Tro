package edu.poly.nhtr.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;


import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.VerifyOTPInterface;
import edu.poly.nhtr.models.OTP;
import edu.poly.nhtr.presenters.VerifyOTPPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import papaya.in.sendmail.SendMail;


public class VerifyOTPActivity extends AppCompatActivity implements VerifyOTPInterface {
    PreferenceManager preferenceManager;
    VerifyOTPPresenter presenter;
    private String email = "", name = "", password = "";

    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private TextView resend;

    private int selectedETPosition = 0;


    private ProgressBar progressBar;
    private Button btnVerify ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceManager = new PreferenceManager(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);
        anhxaID();
        getIntentSignup(); // Nhận intent (name, email, password) từ Sign Up
        presenter = new VerifyOTPPresenter(this);
        presenter.startCountDownTimer();
        presenter.random();


        setupOTPInputs();
        inputCode1.addTextChangedListener(textWatcher);
        inputCode2.addTextChangedListener(textWatcher);
        inputCode3.addTextChangedListener(textWatcher);
        inputCode4.addTextChangedListener(textWatcher);
        inputCode5.addTextChangedListener(textWatcher);
        inputCode6.addTextChangedListener(textWatcher);

        showKeyboard(inputCode1);

        setListeners();



    }

    private void setListeners() {
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startCountDownTimer();
                presenter.random();
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num1 = inputCode1.getText().toString();
                String num2 = inputCode2.getText().toString();
                String num3 = inputCode3.getText().toString();
                String num4 = inputCode4.getText().toString();
                String num5 = inputCode5.getText().toString();
                String num6 = inputCode6.getText().toString();

                OTP otp = new OTP(num1, num2, num3, num4, num5, num6);
                presenter.verifyOTP(otp);
            }
        });
    }

    private void anhxaID() {
        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);
        resend = findViewById(R.id.textResendOTP);

        progressBar = findViewById(R.id.progressBar);
        btnVerify = findViewById(R.id.btnVerify);
    }


    private void getIntentSignup() {
        email = getIntent().getStringExtra("email");
        name = getIntent().getStringExtra("name");
        password = getIntent().getStringExtra("password");
        //encodedImage = getIntent().getStringExtra("image");

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


    @Override
    public void showToast(String message) {
        Toast.makeText(VerifyOTPActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.INVISIBLE);
        btnVerify.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnVerify.setVisibility(View.INVISIBLE);
    }

    @Override
    public void verifySuccess() {

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, name);
        user.put(Constants.KEY_EMAIL, email);
        user.put(Constants.KEY_PASSWORD, password);
        //user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_NAME, name);
                        preferenceManager.putString(Constants.KEY_PASSWORD, password);
                        //preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                        FirebaseAuth auth = FirebaseAuth.getInstance();

                        // Tạo tài khoản mới
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent(VerifyOTPActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            showToast("Authentication failed");
                                        }
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Thêm thất bại");
                        hideLoading();

                    }
                });

    }

    @Override
    public void setBeforeTextColor() {
        resend.setTextColor(getResources().getColor(R.color.colorGray));

    }

    @Override
    public void setAfterTextColor() {
        resend.setTextColor(getResources().getColor(R.color.colorPrimary));

    }

    @Override
    public void setText(String message) {
        resend.setText(message);
    }

    @Override
    public void sendEmail(int code) {
        SendMail mail = new SendMail("nhatrohomemate@gmail.com", "bpvd hqxd xbho gdyl", email, "HOMEMATE's OTP VERIFICATION",
                "Mã xác thực của bạn là:  \n" + code);
        mail.execute();
    }

    @Override
    public void setTextEnabled(boolean enabled) {
        resend.setEnabled(enabled);
    }

    @Override
    public void clearOTP() {
        inputCode1.setText("");
        inputCode2.setText("");
        inputCode3.setText("");
        inputCode4.setText("");
        inputCode5.setText("");
        inputCode6.setText("");
        showKeyboard(inputCode1);
    }

    @Override
    protected void onPause() { // Hàm này dùng để dừng việc đếm thời gian khi chuyển sang acyivity mới
        super.onPause();
        if (presenter != null) {
            presenter.stopCountDownTimer();
        }
    }


}