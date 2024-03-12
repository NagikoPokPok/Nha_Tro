package edu.poly.nhtr.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivitySignUpBinding;
import edu.poly.nhtr.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    Animation topAnim, bottomAnim, leftAnim, rightAnim;

    ActivitySplashBinding binding;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        binding.txtNameApp.setAnimation(topAnim);
        binding.txtExplore.setAnimation(topAnim);
        binding.txtFind.setAnimation(topAnim);
        binding.btnStart.setAnimation(bottomAnim);

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}