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
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.IconCompat;


import java.io.ByteArrayOutputStream;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;

import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private boolean isBlueBackground = false;
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

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return resizedBitmap;
    }

    private void loadUserDetails(){
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        try {
            binding.imageProfile.setImageBitmap(getConversionImage(preferenceManager.getString(Constants.KEY_IMAGE)));
            binding.txtAddImage.setVisibility(View.INVISIBLE);
        }catch (Exception e){
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeBackgroundFeature(){
        // Thiết lập sự kiện onClickListener cho view
        binding.frmQlphong.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmQlphong.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmQlphong.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
        binding.frmQlDichVu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmQlDichVu.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmQlDichVu.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
        binding.frmQlHoaDon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmQlHoaDon.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmQlHoaDon.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }

        });
        binding.frmQlKhachThue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmQlKhachThue.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmQlKhachThue.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
        binding.frmQlHopDong.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmQlHopDong.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmQlHopDong.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
        binding.frmTraPhong.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmTraPhong.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmTraPhong.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
        binding.frmTaoHoaDon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmTaoHoaDon.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;

                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmTaoHoaDon.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        if (v.getId() == R.id.frm_qlHoaDon) {
                            startActivity(new Intent(getApplicationContext(), BillManagement.class));
                        }
                        break;

                }
                return true;
            }
        });
        binding.frmTaoHoaDonNhanh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmTaoHoaDonNhanh.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmTaoHoaDonNhanh.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
        binding.frmTaoHopDong.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Nhấn giữ: Thay đổi background
                        binding.frmTaoHopDong.setBackgroundResource(R.drawable.background_feature_blue);
                        isBlueBackground = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Nhả nút: Trở lại background ban đầu
                        binding.frmTaoHopDong.setBackgroundResource(R.drawable.backgroundfeature);
                        isBlueBackground = false;
                        break;
                }
                return true;
            }
        });
    }
    private void setListeners() {

        changeBackgroundFeature();
        // Khai báo một biến để theo dõi trạng thái của background
        binding.btnSetting.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));
        binding.frmQlHoaDon.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), BillManagement.class)));
    }

    // Other methods...

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
