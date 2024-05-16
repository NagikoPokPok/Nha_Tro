package edu.poly.nhtr.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivitySignInBinding;
import edu.poly.nhtr.interfaces.SignInInterface;
import edu.poly.nhtr.presenters.SignInPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity implements SignInInterface {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseAuth mAuth;

    private SignInPresenter signInPresenter;
    FirebaseDatabase database;

    private GoogleSignInClient googleSignInClient;
    private static final String TAG = "SignInActivity";
    private final int RC_SIGN_IN = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        signInPresenter = new SignInPresenter(this);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        if (mAuth.getCurrentUser() != null || preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        //Show message from SetNewPasswordActivity
        Intent intent = getIntent();
        if(intent.hasExtra("message")){
            String mes = intent.getStringExtra("message");
            showToast(mes);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }

    private void reload() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String userName = documentSnapshot.getString(Constants.KEY_NAME);
                        Toast.makeText(SignInActivity.this, "Chào mừng trở lại, " + userName + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(SignInActivity.this, "Thất bại khi lấy thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(SignInActivity.this, "Tài khoản chưa được đăng nhập", Toast.LENGTH_SHORT).show();
        }
    }
    public void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void entryMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setListeners() {
        binding.buttonSignIn.setOnClickListener(v->{
            if(isValidSignInDetails()){
                signIn();
            }
        });

        binding.txtForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });

        binding.txtSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        binding.googleIcon.setOnClickListener(v -> signInPresenter.googleSignIn());
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .get()
                .addOnCompleteListener(task-> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        String storedHashedPassword = documentSnapshot.getString(Constants.KEY_PASSWORD);

                        // Harness the power of hashing to secure your passwords
                        String enteredPassword = binding.inputPassword.getText().toString();
                        String hashedPassword = PasswordHasher.hashPassword(enteredPassword);
                        if (storedHashedPassword.equals(hashedPassword)) {
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                            preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD));
                            preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                            preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                            preferenceManager.putString(Constants.KEY_PHONE_NUMBER, documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));
                            try {
                                preferenceManager.putString(Constants.KEY_ADDRESS, documentSnapshot.getString(Constants.KEY_ADDRESS));
                            } catch (Exception ex){}
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                            mAuth.signInWithEmailAndPassword(preferenceManager.getString(Constants.KEY_EMAIL), preferenceManager.getString(Constants.KEY_PASSWORD))
                                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d(TAG, "signInWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();

                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                Toast.makeText(SignInActivity.this, "Xác thực người dùng thất bại.",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                        } else {
                            // The gates remain shut, authentication denied
                            loading(false);
                            showToast("Sai tài khoản hoặc mật khẩu");
                        }
                    }else {
                        loading(false);
                        showToast("Sai tài khoản hoặc mật khẩu");
                    }
                });
    }

     public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Email không hợp lệ");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Vui lòng nhập mật khẩu");
            return false;
        } else  {
            return true;
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (signInPresenter != null) {
            signInPresenter.onActivityResult(requestCode, resultCode, data);
        } else {
            // Ví dụ: Hiển thị thông báo lỗi
            Toast.makeText(this, "Lỗi: signInPresenter không được khởi tạo", Toast.LENGTH_SHORT).show();
        }
    }

    public void firebaseAuth(String idToken) {
        signInPresenter.firebaseAuth(idToken);
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


}