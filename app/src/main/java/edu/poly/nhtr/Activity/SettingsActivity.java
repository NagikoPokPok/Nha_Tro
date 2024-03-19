package edu.poly.nhtr.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.util.Objects;

import edu.poly.nhtr.databinding.ActivitySettingsBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }

    private void setListeners() {
        binding.btnlogout.setOnClickListener(v -> {loading(true);logout();});
        binding.ChangeProfile.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangeProfileActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnBack.setOnClickListener(v -> back());

        getInfoFromGoogle();
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
    public void logout() {
        loading(true);
        showToast("Signing out ...");

        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();
        loading(false);

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

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.btnlogout.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnlogout.setVisibility(View.VISIBLE);
        }
    }

}