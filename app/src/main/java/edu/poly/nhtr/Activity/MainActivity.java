package edu.poly.nhtr.Activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.IconCompat;


import java.io.ByteArrayOutputStream;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;

import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    boolean isBlueBackground = false;
    // Other code...
    Button changeProfile;
    private String encodedImage;
    PreferenceManager preferenceManager;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadUserDetails();

    }

//    private String encodedImage(Bitmap bitmap) // Hàm mã hoá ảnh thành chuỗi Base64
//    {
//        int previewWidth = 150;
//        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
//        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(bytes, Base64.DEFAULT);
//    }

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
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        try {

//            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            encodedImage = encodedImage(bitmap);
//            byte[] encodedBytes = Base64.decode(encodedImage(bitmap), Base64.DEFAULT);
//            Bitmap encodedBitmap = BitmapFactory.decodeByteArray(encodedBytes, 0, encodedBytes.length);
            binding.imageProfile.setImageBitmap(getConversionImage(preferenceManager.getString(Constants.KEY_IMAGE)));
            binding.txtAddImage.setVisibility(View.INVISIBLE);
        }catch (Exception e){
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListeners() {

        // Khai báo một biến để theo dõi trạng thái của background


// Thiết lập sự kiện onClickListener cho view
        binding.frmQlphong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kiểm tra trạng thái hiện tại của background và thay đổi nó
                if (!isBlueBackground) {
                    binding.frmQlphong.setBackgroundResource(R.drawable.background_feature_blue);
                } else {
                    binding.frmQlphong.setBackgroundResource(R.drawable.backgroundfeature);
                }
                // Cập nhật trạng thái của background
                isBlueBackground = !isBlueBackground;
            }
        });


        binding.btnSetting.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));
        binding.QuanLyHoaDon.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), BillManagement.class)));
    }

    // Other methods...

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
