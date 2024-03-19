package edu.poly.nhtr.Activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
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

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivitySignUpBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;


public class SignUpActivity extends AppCompatActivity {

     ActivitySignUpBinding binding;


    String encodedImage;
    ImageView passwordIcon, confirmPassword;
    private boolean passwordShowing = false;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        passwordIcon = binding.iconPassword;
        confirmPassword = binding.iconConfirmPassword;


        setListeners();
    }

    private void setListeners() {

        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking if password is showing or not
                if(passwordShowing)
                {
                    passwordShowing = false;
                    binding.edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordIcon.setImageResource(edu.poly.nhtr.R.drawable.password_show_2);
                }
                else {
                    passwordShowing = true;
                    binding.edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordIcon.setImageResource(R.drawable.password_hide);
                }

                binding.edtPassword.setSelection(binding.edtPassword.length());
            }
        });

        confirmPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking if password is showing or not
                if(passwordShowing)
                {
                    passwordShowing = false;
                    binding.edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPassword.setImageResource(edu.poly.nhtr.R.drawable.password_show_2);
                }
                else {
                    passwordShowing = true;
                    binding.edtConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPassword.setImageResource(R.drawable.password_hide);
                }

                binding.edtConfirmPassword.setSelection(binding.edtConfirmPassword.length());

            }
        });
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
                            binding.edtEmail.setText("");
                            binding.edtName.setText("");
                            loading(false);
                            return;

                        } else {
                            // Nếu email chưa có thì mới có thể SignUp()
                            Intent intent = new Intent(SignUpActivity.this, VerifyOTPActivity.class);
                            intent.putExtra("email",binding.edtEmail.getText().toString());
                            intent.putExtra("password",binding.edtPassword.getText().toString());
                            intent.putExtra("name",binding.edtName.getText().toString());
                            intent.putExtra("image",encodedImage);
                            startActivity(intent);
                        }
                    } else {
                        // Xử lý lỗi truy vấn
                        Toast.makeText(this, "Lỗi truy vấn, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                });
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
        //String phoneNumber = binding.edtPhoneNumber.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        if (name.isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (!name.matches("^[\\p{L}\\s]+$")) {
            showToast("Please enter only alphabetical characters");
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