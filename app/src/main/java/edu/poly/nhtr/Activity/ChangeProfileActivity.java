package edu.poly.nhtr.Activity;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityChangeProfileBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class ChangeProfileActivity extends AppCompatActivity {
    private ActivityChangeProfileBinding binding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    Button btn_back, btn_change;
    TextView warning, txt_move_changePassword;
    EditText name, phoneNum;
    ImageView imageProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);
        //btn_back = findViewById(R.id.btn_back);
        name = findViewById(R.id.edt_name1);
        phoneNum = findViewById(R.id.edt_phoneNumber1);
        imageProfile = findViewById(R.id.img_profile);
        btn_change = findViewById(R.id.btn_save);
        //warning = findViewById(R.id.txt_warning1);
        txt_move_changePassword = findViewById(R.id.txt_move_change_password);

        binding = ActivityChangeProfileBinding.inflate(getLayoutInflater());
        binding.getRoot();
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetail();
        setListener();
    }
    private void loadUserDetail(){
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        name.setText(preferenceManager.getString(Constants.KEY_NAME));
        phoneNum.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        imageProfile.setImageBitmap(bitmap);
    }
    private void setListener(){

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValidChangeDetails()){
                    updateProfile();
                }
            }
        });
        txt_move_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
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
    private Boolean isValidChangeDetails() {
        String ten = name.getText().toString().trim();
        String phoneNumber = phoneNum.getText().toString().trim();


        if (!ten.matches("^[\\p{L}\\s]+$")) {
            //warning.setText("Tên chỉ được xuất hiện các kí tự là chữ và số");
            showToast("Please enter only alphabetical characters");
            return false;
        } else if (phoneNum.getText().toString().trim().isEmpty()) {
            //warning.setText("Số điện thoại không được để trống");
            showToast("Số điện thoai khôn được để trống");
            return false;
        } else if (!phoneNumber.matches("^0[0-9]{9}$")) {
            //warning.setText("Số điện thoại chỉ gồm những kí tự là số từ 0-9");
            showToast("Enter a valid 10-digit phone number starting with 0");
            return false;
        } else {
            return true;
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void updateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //warning.setText("Thành công");
    }
}