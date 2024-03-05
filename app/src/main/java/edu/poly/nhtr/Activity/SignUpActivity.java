package edu.poly.nhtr.Activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import edu.poly.nhtr.databinding.ActivitySignUpBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;


public class SignUpActivity extends AppCompatActivity {

     ActivitySignUpBinding binding;


    String encodedImage;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());


        setListeners();
    }

    private void setListeners() {
        binding.txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidSignUpDetails()) {
                    checkExisted(binding.edtEmail.getText().toString().trim());
                }
            }
        });
        binding.layoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void checkExisted(String email) {

       loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int documentCount = task.getResult().getDocuments().size();
                        if (documentCount > 0) {
                            // Email đã tồn tại
                            Toast.makeText(this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                            loading(false);

                        } else {
                            // Nếu email chưa có thì mới có thể SignUp()
                            signUp();
                        }
                    } else {
                        // Xử lý lỗi truy vấn
                        Toast.makeText(this, "Lỗi truy vấn, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.edtName.getText().toString());
        user.put(Constants.KEY_PHONE_NUMBER, binding.edtPhoneNumber.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.edtEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.edtPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        loading(false);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                        preferenceManager.putString(Constants.KEY_NAME, binding.edtName.getText().toString());
                        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                        FirebaseAuth auth = FirebaseAuth.getInstance();

                        // Tạo tài khoản mới
                        auth.createUserWithEmailAndPassword(binding.edtEmail.getText().toString(), binding.edtPassword.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser user = task.getResult().getUser();
                                            // Bây giờ bạn có thể sử dụng đối tượng 'user'

                                        } else {
                                            // Xử lý lỗi tạo
                                        }
                                    }
                                });
//
                        sendOTP();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loading(false);
                        showToast(e.getMessage());

                    }
                });
    }

    private void sendOTP()
    {

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSignUp.setVisibility(View.INVISIBLE);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + binding.edtPhoneNumber.getText().toString(),
                60, TimeUnit.SECONDS,
                SignUpActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
                {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignUp.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignUp.setVisibility(View.VISIBLE);
                        Toast.makeText(SignUpActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignUp.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(getApplicationContext(), VerifyOTPActivity.class);
                        intent.putExtra("phoneNumber",binding.edtPhoneNumber.getText().toString());
                        intent.putExtra("verificationId",verificationId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
        );
    }

    private String encodedImage(Bitmap bitmap) // Hàm mã hoá ảnh thành chuỗi Base64
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Hàm truy cập thư viện để lấy ảnh
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imgProfile.setImageBitmap(bitmap);
                            binding.txtAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    private Boolean isValidSignUpDetails() {
        String name = binding.edtName.getText().toString().trim();
        String phoneNumber = binding.edtPhoneNumber.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        if (encodedImage == null) {
            showToast("Select profile image");
            return false;
        } else if (name.isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (!name.matches("^[\\p{L}\\s]+$")) {
            showToast("Please enter only alphabetical characters");
            return false;
        } else if (binding.edtPhoneNumber.getText().toString().trim().isEmpty()) {
            showToast("Enter phone number");
            return false;
        } else if (!phoneNumber.matches("^0[0-9]{9}$")) {
            showToast("Enter a valid 10-digit phone number starting with 0");
            return false;
        } else if (binding.edtEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.getText().toString()).matches()) {
            showToast("Enter valid email address");
            return false;
        } else if (binding.edtPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.edtConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!binding.edtPassword.getText().toString().trim().equals(binding.edtConfirmPassword.getText().toString().trim())) {
            showToast("Password and confirm must be the same");
            return false;
        } else {
            return true;
        }


    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.btnSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignUp.setVisibility(View.VISIBLE);
        }
    }
}