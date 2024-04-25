package edu.poly.nhtr.Activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.poly.nhtr.Class.PasswordHasher;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivitySignUpBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;


public class SignUpActivity extends AppCompatActivity {

     ActivitySignUpBinding binding;

    private boolean passwordShowing = false;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();

        binding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                // Check for minimum length first
                if (password.length() < 8) {
                    binding.layoutPassword.setHelperText("Nhập ít nhất 8 ký tự");
                    binding.layoutPassword.setError("");
                    return; // Exit early if password is less than 8 characters
                }
                else if(password.length() >=8 && password.length() <=10)
                {
                    // Improved regular expression for special characters
                    Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
                    Matcher matcher = pattern.matcher(password);
                    boolean isPwdContainsSpeChar = matcher.find();

                    if (isPwdContainsSpeChar) {
                        binding.layoutPassword.setHelperText("Mật khẩu mạnh");
                        binding.layoutPassword.setError("");
                    } else {
                        binding.layoutPassword.setHelperText("");
                        binding.layoutPassword.setError("Mật khẩu yếu, nhập ít nhất 1 ký tự đặc biệt");
                    }
                }

                else{
                    binding.layoutPassword.setHelperText("");
                    binding.layoutPassword.setError("Tối đa 10 ký tự");
                }

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
                String password = binding.edtPassword.getText().toString().trim();
                String confirmPassword = s.toString().trim();

                if(confirmPassword.equals(password)) {
                    binding.layoutConfirmPassword.setHelperText("Xác minh mật khẩu chính xác");
                    binding.layoutConfirmPassword.setError("");
                } else {
                    binding.layoutConfirmPassword.setHelperText("");
                    binding.layoutConfirmPassword.setError("Mật khẩu không trùng");
                }
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
                String name = binding.edtName.getText().toString().trim();
                if(name.length()>=0) {
                    binding.layoutName.setErrorEnabled(false);
                }
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
                String email = binding.edtEmail.getText().toString().trim();
                if(email.length()>=0) {
                    binding.layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


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
                            // Mã hóa mật khẩu trước khi đưa lên Firebase
                            String hashedPassword = PasswordHasher.hashPassword(binding.edtPassword.getText().toString());
                            intent.putExtra("password",hashedPassword);
                            intent.putExtra("name",binding.edtName.getText().toString());
                            //intent.putExtra("image",encodedImage);
                            startActivity(intent);
                        }
                    } else {
                        // Xử lý lỗi truy vấn
                        Toast.makeText(this, "Lỗi truy vấn, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        loading(false);
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
//    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == RESULT_OK) {
//                    if (result.getData() != null) {
//                        Uri imageUri = result.getData().getData();
//                        try {
//                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
//                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                            binding.imgProfile.setImageBitmap(bitmap);
//                            binding.txtAddImage.setVisibility(View.GONE);
//                            encodedImage = encodedImage(bitmap);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });

    private Boolean isValidSignUpDetails() {
        String name = binding.edtName.getText().toString().trim();
        //String phoneNumber = binding.edtPhoneNumber.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        if (name.isEmpty()) {
            showToast("Enter name");
            binding.layoutName.setError("Không được bỏ trống");
            return false;
        } else if (binding.edtEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            binding.layoutEmail.setError("Không được bỏ trống");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.getText().toString()).matches()) {
            showToast("Enter valid email address");
            binding.layoutEmail.setError("Email không hợp lệ");
            return false;
        } else if (binding.edtPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            binding.layoutPassword.setError("Không được bỏ trống");
            return false;
        } else if (binding.edtConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            binding.layoutConfirmPassword.setError("Không được bỏ trống");
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