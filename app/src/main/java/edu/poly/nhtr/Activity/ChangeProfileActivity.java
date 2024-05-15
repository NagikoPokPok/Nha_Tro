package edu.poly.nhtr.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


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

import edu.poly.nhtr.databinding.ActivityChangeProfileBinding;

import edu.poly.nhtr.interfaces.ChangeProfileInterface;
import edu.poly.nhtr.presenters.ChangeProfilePresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangeProfileActivity extends AppCompatActivity implements ChangeProfileInterface {
    private ActivityChangeProfileBinding binding;
    public PreferenceManager preferenceManager;
    public String encodedImage;
    public ChangeProfilePresenter presenter;
    Button btn_save;
    public TextView warning, txt_move_changePassword, txt_add_image;
    public EditText name, phoneNum, diachi;
    public ImageView imageProfile,imgBack;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);

        init();

        preferenceManager = new PreferenceManager(getApplicationContext());
        presenter = new ChangeProfilePresenter(this, this);

        setListener();

        //warning = findViewById(R.id.txt_warning1);
//        binding = ActivityChangeProfileBinding.inflate(getLayoutInflater());
//        binding.getRoot();


        //Message Toast from Change Password
        Intent intent = getIntent();
        if(intent.hasExtra("message")){
            String mes = intent.getStringExtra("message");
            showToast(mes);
        }
    }

    public void init(){
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

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        //return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return resizedBitmap;
    }
    private void loadUserDetails(){

        try {
            imageProfile.setImageBitmap(getConversionImage(preferenceManager.getString(Constants.KEY_IMAGE)));
            txt_add_image.setVisibility(View.INVISIBLE);
        }catch (Exception e){
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }

        name.setText(preferenceManager.getString(Constants.KEY_NAME));
        phoneNum.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        diachi.setText(preferenceManager.getString(Constants.KEY_ADDRESS));

    }
    private void setListener(){
        //Nút back
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            finish();
        });
        //Nút lưu thay đổi
        btn_save.setOnClickListener(v -> {
            presenter.clickSave();
//            if(isValidChangeDetails()){
//                updateProfile();
//            }
        });
        //Nút chuyển page qua đổi mật khẩu
        txt_move_changePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ChangeProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        //Nút chỉnh ảnh
        imageProfile.setOnClickListener(v -> {
            presenter.clickImageProfile();
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            pickImage.launch(intent);
            //preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
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
    public final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
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

//    private Boolean isValidChangeDetails() {
//        String ten = name.getText().toString().trim();
//        String phoneNumber = phoneNum.getText().toString().trim();
//
//
//        if (!ten.matches("^[\\p{L}\\s]+$")) {
//            //warning.setText("Tên chỉ được xuất hiện các kí tự là chữ và số");
//            showToast("Tên chỉ được xuất hiện các kí tự là chữ và số");
//            return false;
//        } else if (phoneNum.getText().toString().trim().isEmpty()) {
//            //warning.setText("Số điện thoại không được để trống");
//            showToast("Số điện thoai khôn được để trống");
//            return false;
//        } else if (!phoneNumber.matches("^0[0-9]{9}$")) {
//            //warning.setText("Số điện thoại chỉ gồm những kí tự là số từ 0-9");
//            showToast("Số điện thoại có 10 kí tự và chỉ gồm những kí tự là số từ 0-9");
//            return false;
//        } else {
//            return true;
//        }
//    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
//    private void updateProfile() {
//        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> user = new HashMap<>();
//        user.put(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
//        user.put(Constants.KEY_PASSWORD, preferenceManager.getString(Constants.KEY_PASSWORD));
//        user.put(Constants.KEY_NAME, name.getText().toString());
//        user.put(Constants.KEY_PHONE_NUMBER, phoneNum.getText().toString());
//        user.put(Constants.KEY_ADDRESS, diachi.getText().toString());
//        user.put(Constants.KEY_IMAGE, encodedImage);
//        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID))
//                .set(user)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        loading(false);
//                        Toast.makeText(ChangeProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
//                        RefreshPrefernceManager();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        loading(false);
//                        Toast.makeText(ChangeProfileActivity.this, "Cập nhật tông tin thất bại", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private  void RefreshPrefernceManager(){
//        preferenceManager.putString(Constants.KEY_NAME, name.getText().toString());
//        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, phoneNum.getText().toString());
//        preferenceManager.putString(Constants.KEY_ADDRESS, diachi.getText().toString());
//        preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
//    }

    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            btn_save.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }else {
            progressBar.setVisibility(View.INVISIBLE);
            btn_save.setVisibility(View.VISIBLE);
        }
    }
}

//           byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
//           Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//           encodedImage = encodedImage(bitmap);
//
//           byte[] encodedBytes = Base64.decode(encodedImage(bitmap), Base64.DEFAULT);
//           Bitmap encodedBitmap = BitmapFactory.decodeByteArray(encodedBytes, 0, encodedBytes.length);
//           imageProfile.setImageBitmap(encodedBitmap);
