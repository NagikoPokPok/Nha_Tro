package edu.poly.nhtr.presenters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Activity.SignInActivity;
import edu.poly.nhtr.interfaces.SettingsInterface;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SettingsPresenter {

    private final SettingsInterface view;
    private final Context context;
    private final PreferenceManager preferenceManager;
    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    public SettingsPresenter(SettingsInterface view, Context context, PreferenceManager preferenceManager) {
        this.view = view;
        this.context = context;
        this.preferenceManager = preferenceManager;
    }

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return resizedBitmap;
    }

    public void loadUserDetails() {
        // Code từ loadUserDetails() của SettingFragment
            String name = preferenceManager.getString(Constants.KEY_NAME);
            String phoneNumber = preferenceManager.getString(Constants.KEY_PHONE_NUMBER);
            Bitmap profileImage = null;
            String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
            if (encodedImage != null && !encodedImage.isEmpty()) {
                try {
                    profileImage = getConversionImage(encodedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            view.loadUserDetails(name, phoneNumber, profileImage); // Truyền dữ liệu từ Presenter tới Fragment
    }

    public void checkAccount() {
        // Kiểm tra tài khoản đăng nhập là tài khoản Email hay Google
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<? extends UserInfo> providerData = currentUser.getProviderData();
            // Lặp qua danh sách các tài khoản cấp thông tin xác thực
            for (UserInfo userInfo : providerData) {
                String providerId = userInfo.getProviderId();
                if (providerId.equals("google.com")) {
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
    }

    public void getInfoFromGoogle() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            String userName = account.getDisplayName();
            view.setUserName(userName);

            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            new DownloadImageTask(view.getProfileImageView()).execute(photoUrl);

            view.hideAvatar();
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

    public void switchModeTheme() {
        sharedPreferences = context.getSharedPreferences("MODE", Context.MODE_PRIVATE);
        boolean nightMode = sharedPreferences.getBoolean("nightMode", false);
        view.setNightMode(nightMode);

        view.setSwitchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", !nightMode);
                editor.apply();
            }
        });
    }

//    public void logout {
//            // Hiển thị thông báo đăng xuất
//            view.showToast("Signing out ...");
//
//            // Đăng xuất khỏi Firebase
//            FirebaseAuth.getInstance().signOut();
//
//            // Xóa cài đặt về người dùng
//            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
//            preferenceManager.removePreference(Constants.KEY_USER_ID);
//            preferenceManager.removePreference(Constants.KEY_NAME);
//            preferenceManager.removePreference(Constants.KEY_PHONE_NUMBER);
//            preferenceManager.removePreference(Constants.KEY_ADDRESS);
//
//            // Chuyển theme
//            switchModeTheme();
//
//            // Trở lại SignIn Activity
//            navigateToSignIn();
//    }
//    private void navigateToSignIn() {
//        Intent intent = new Intent(context, SignInActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        context.startActivity(intent);
//        ((Activity) context).finish();
//    }




}
