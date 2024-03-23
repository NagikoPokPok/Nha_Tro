package edu.poly.nhtr.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivitySignUpBinding;
import edu.poly.nhtr.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

//    Animation topAnim, bottomAnim, leftAnim, rightAnim;
//
//    ActivitySplashBinding binding;
//
//
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
//        binding = ActivitySplashBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
//        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
//        binding.txtNameApp.setAnimation(topAnim);
//        binding.txtExplore.setAnimation(topAnim);
//        binding.txtFind.setAnimation(topAnim);
//        binding.btnStart.setAnimation(bottomAnim);
//
//        binding.btnStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                if(user == null)
//                {
//                    // Chua log in
//                    Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
//                    startActivity(intent);
//                }
//                else{
//                    // Da log in
//                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//                    startActivity(intent);
//                }
//                finish();
//            }
//        });
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra người dùng đã đăng nhập hay chưa
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Nếu đã đăng nhập, chuyển hướng sang MainActivity và kết thúc SplashActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Nếu chưa đăng nhập, hiển thị màn hình Splash
            setContentView(R.layout.activity_splash);
            Button start = findViewById(R.id.btn_Start);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                }
            });
        }
    }
}