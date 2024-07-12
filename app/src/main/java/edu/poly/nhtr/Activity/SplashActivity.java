package edu.poly.nhtr.Activity;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.poly.nhtr.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // Splash screen duration in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent;
                if (user != null) {
                    // Đã đăng nhập
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {
                    // Chưa đăng nhập
                    intent = new Intent(SplashActivity.this, SignInActivity.class);
                }


                startActivity(intent);
                finish(); // Finish SplashActivity after starting the next activity
            }
        }, SPLASH_DURATION);
    }
}