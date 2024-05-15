package edu.poly.nhtr.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import edu.poly.nhtr.databinding.ActivitySignUpBinding;
import edu.poly.nhtr.interfaces.SignUpInterface;
import edu.poly.nhtr.models.User;
import edu.poly.nhtr.presenters.SignUpPresenter;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SignUpActivity extends AppCompatActivity implements SignUpInterface {

    private ActivitySignUpBinding binding;
    private SignUpPresenter presenter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        presenter = new SignUpPresenter(this);

        setListeners();

        customiseEditText();
    }

    private void customiseEditText() {

        binding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String confirmPassword = Objects.requireNonNull(binding.edtConfirmPassword.getText()).toString();
                presenter.handlePasswordChanged(s.toString(), confirmPassword);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = Objects.requireNonNull(binding.edtPassword.getText()).toString().trim();
                presenter.handleConfirmPasswordChanged(password, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = Objects.requireNonNull(binding.edtName.getText()).toString().trim();
                presenter.handleNameChanged(name);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = Objects.requireNonNull(binding.edtEmail.getText()).toString().trim();
                presenter.handleEmailChanged(email);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.googleIcon.setOnClickListener(v -> presenter.googleSignIn());
    }

    private void setListeners() {
        binding.txtSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
        });
        binding.btnSignUp.setOnClickListener(v -> {
            String name = Objects.requireNonNull(binding.edtName.getText()).toString().trim();
            String email = Objects.requireNonNull(binding.edtEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.edtPassword.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(binding.edtConfirmPassword.getText()).toString().trim();

            // Khởi tạo đối tượng cho User
            User user = new User(name, email, password, confirmPassword);
            // Thực hiện công việc sign up
            presenter.signUp(user);
        });
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void showSuccessMessage(String email, String name, String password){
    // Nếu email chưa có thì mới có thể SignUp()
        Intent intent = new Intent(SignUpActivity.this, VerifyOTPActivity.class);
        intent.putExtra("email",email);
        // Mã hóa mật khẩu trước khi đưa lên Firebase
        intent.putExtra("password",password);
        intent.putExtra("name",name);
        startActivity(intent);
    }

    @Override
    public void showLoading() {
        binding.btnSignUp.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.btnSignUp.setVisibility(View.VISIBLE);
    }

    @Override
    public void setNameErrorMessage(String message) {
        binding.layoutName.setError(message);
    }

    @Override
    public void setEmailErrorMessage(String message) {
        binding.layoutEmail.setError(message);
    }

    @Override
    public void setPasswordErrorMessage(String message) {
        binding.layoutPassword.setError(message);
    }

    @Override
    public void setConfirmPasswordErrorMessage(String message) {
        binding.layoutConfirmPassword.setError(message);
    }

    @Override
    public void setPasswordHelperText(String helperText) {
        binding.layoutPassword.setHelperText(helperText);
    }

    @Override
    public void setPasswordError(String error) {
        binding.layoutPassword.setError(error);
    }

    @Override
    public void setConfirmPasswordHelperText(String helperText) {
        binding.layoutConfirmPassword.setHelperText(helperText);
    }

    @Override
    public void setConfirmPasswordError(String error) {
        binding.layoutConfirmPassword.setError(error);
    }

    @Override
    public void setNameErrorEnabled(Boolean isEmpty) {
        binding.layoutName.setErrorEnabled(isEmpty);
    }

    @Override
    public void setEmailErrorEnabled(Boolean isEmpty) {
        binding.layoutEmail.setErrorEnabled(isEmpty);
    }

    @Override
    public void setConfirmPasswordText(String message) {
        binding.edtConfirmPassword.setText(message);
    }

        @Override
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (presenter != null) {
            presenter.onActivityResult(requestCode, resultCode, data);
        } else {
            // Ví dụ: Hiển thị thông báo lỗi
            Toast.makeText(this, "Lỗi: signInPresenter không được khởi tạo", Toast.LENGTH_SHORT).show();
        }
    }

    public void firebaseAuth(String idToken) {
        presenter.firebaseAuth(idToken);
    }

    public void notifySignInSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public String getStringFromResources(int defaultWebClientId) {
        return getResources().getString(defaultWebClientId);
    }

    @Override
    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return this;
    }
}
