package edu.poly.nhtr.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivitySettingsBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    PreferenceManager preferenceManager;


    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return resizedBitmap;
    }

    private void loadUserDetails() {
        binding.edtName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.phoneNum.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            try {
                Bitmap profileImage = getConversionImage(encodedImage);
                binding.imgProfile.setImageBitmap(profileImage);
                binding.imgAva.setVisibility(View.INVISIBLE); // Ẩn ảnh mặc định nếu có ảnh người dùng
            } catch (Exception e) {
                // Xử lý ngoại lệ khi không thể tải ảnh
                binding.imgAva.setVisibility(View.VISIBLE); // Hiển thị ảnh mặc định nếu xảy ra ngoại lệ
                Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định và ẩn ảnh người dùng
            binding.imgAva.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_setting);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_home) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (item.getItemId() == R.id.menu_notification) {
                    startActivity(new Intent(getApplicationContext(), NotificationActivity.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (item.getItemId() == R.id.menu_setting) {
                    return true;

                }
                return false;
            }
        });
        setListeners();


    }

    private void setListeners() {
        binding.btnlogout.setOnClickListener(v -> {
            try {
                logout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        binding.ChangeProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangeProfileActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> back());

        // Kiểm tra tài khoản đăng nhập là tài khoản Email hay Google
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<? extends UserInfo> providerData = currentUser.getProviderData();
            // Lặp qua danh sách các tài khoản cấp thông tin xác thực
            for (UserInfo userInfo : providerData) {
                String providerId = userInfo.getProviderId();
                if (providerId != null && providerId.equals("google.com")) {
                    // TH đăng nhập bằng tài khoản Google
                    getInfoFromGoogle();
                    return; // Thoát khỏi vòng lặp khi thấy đúng tài khoản Google
                }
            }
            // Nếu là tài khoản Email thì tải thông tin người dùng từ SharedPreferences
            loadUserDetails();
        } else {
            // Không có người dùng nào đang đăng nhập, tải thông tin từ SharedPreferences
            loadUserDetails();
        }
//        binding.nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
//                }
//                else {
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//                }
//            }
//        });
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void back() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Lấy ảnh đại diện và tên từ Google
    private void getInfoFromGoogle() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            String userName = account.getDisplayName();
            binding.edtName.setText(userName);

            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            new DownloadImageTask(binding.imgProfile).execute(photoUrl);

            binding.imgAva.setVisibility(View.INVISIBLE);
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }

    public void logout() throws InterruptedException {
        showToast("Signing out ...");
        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // Xóa cài đặt về người dùng
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
        preferenceManager.removePreference(Constants.KEY_USER_ID);
        preferenceManager.removePreference(Constants.KEY_NAME);

        // Trở lại Settings Activity
        Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnlogout.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnlogout.setVisibility(View.VISIBLE);
        }
    }

}