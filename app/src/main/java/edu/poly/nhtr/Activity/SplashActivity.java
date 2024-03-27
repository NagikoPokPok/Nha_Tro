package edu.poly.nhtr.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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



    private static final int SPLASH_DURATION = 2000; // Splash screen duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Đã đăng nhập
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Chưa đăng nhập
                    Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
                finish(); // Finish SplashActivity after starting the next activity
            }
        }, SPLASH_DURATION);
    }
}