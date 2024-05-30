package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.ChangeProfileInterface;
import edu.poly.nhtr.presenters.ChangeProfilePresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangeProfileActivity extends AppCompatActivity implements ChangeProfileInterface {
    public PreferenceManager preferenceManager;
    public String encodedImage;
    public ChangeProfilePresenter presenter;
    public TextView txt_move_changePassword, txt_add_image;
    public EditText name, phoneNum, diachi;
    public ImageView imageProfile, imgBack;
    // Hàm truy cập thư viện để lấy ảnh
    public final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageProfile.setImageBitmap(bitmap);
                            txt_add_image.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    Button btn_save;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);

        init();

        preferenceManager = new PreferenceManager(getApplicationContext());
        presenter = new ChangeProfilePresenter(this, this);

        //Lấy ảnh
        encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);

        setListener();

        //Message Toast from Change Password
        Intent intent = getIntent();
        if (intent.hasExtra("message")) {
            String mes = intent.getStringExtra("message");
            showToast(mes);
        }
    }

    public void init() {
        imgBack = findViewById(R.id.img_back);
        name = findViewById(R.id.edt_name);
        phoneNum = findViewById(R.id.edt_phoneNumber);
        diachi = findViewById(R.id.edt_address);
        imageProfile = findViewById(R.id.img_profile);
        btn_save = findViewById(R.id.btn_save);
        txt_add_image = findViewById(R.id.txt_add_image);
        txt_move_changePassword = findViewById(R.id.txt_move_change_password);
        progressBar = findViewById(R.id.progressBar);
    }

    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void loadUserDetails() {

        try {
            imageProfile.setImageBitmap(getConversionImage(preferenceManager.getString(Constants.KEY_IMAGE)));
            txt_add_image.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }

        name.setText(preferenceManager.getString(Constants.KEY_NAME));
        phoneNum.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        diachi.setText(preferenceManager.getString(Constants.KEY_ADDRESS));

    }

    private void setListener() {
        //Nút back
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChangeProfileActivity.this, MainActivity.class);
            intent.putExtra("FRAGMENT_TO_LOAD", "SettingFragment");
            startActivity(intent);
            finish();
        });

        //Nút lưu thay đổi
        btn_save.setOnClickListener(v -> {
            presenter.clickSave();
        });
        //Nút chuyển page qua đổi mật khẩu
        txt_move_changePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ChangeProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        //Nút chỉnh ảnh
        imageProfile.setOnClickListener(v -> {
            presenter.clickImageProfile();
        });

        // Kiểm tra tài khoản đăng nhập là tài khoản Email hay Google
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<? extends UserInfo> providerData = currentUser.getProviderData();
            // Lặp qua danh sách các tài khoản cấp thông tin xác thực
            for (UserInfo userInfo : providerData) {
                String providerId = userInfo.getProviderId();
                if (providerId.equals("google.com")) {
                    // TH đăng nhập bằng tài khoản Google
                    loadInfoGoogleAccount();
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

    // Đưa tên, ảnh đại diện từ Google
    private void loadInfoGoogleAccount() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            String userName = account.getDisplayName();
            name.setText(userName);

            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            new DownloadImageTask(imageProfile).execute(photoUrl);

            txt_add_image.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public void changeSuccess() {
        showToast("Thay đổi thành công");
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showLoading() {
        btn_save.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        btn_save.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
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


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

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
}
