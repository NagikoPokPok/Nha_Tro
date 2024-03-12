package edu.poly.nhtr.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

import edu.poly.nhtr.databinding.ActivitySettingsBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }

    private void setListeners() {
        binding.btnLogout.setOnClickListener(v -> logout());
        binding.ChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ChangeProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void logout() {
        showToast("Signing out ...");

        // Kiểm tra người dùng có đăng nhập bằng tài khoản google hay không
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (googleSignInAccount == null) {
            preferenceManager = new PreferenceManager(getApplicationContext());
            // Người dùng không đăng nhập bằng tài khoản Google thì sẽ xử lý đăng xuất khỏi tài khoản được đăng ký trên app
            FirebaseAuth.getInstance().signOut();

            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
            preferenceManager.removePreference(Constants.KEY_USER_ID);
            preferenceManager.removePreference(Constants.KEY_NAME);

            Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
//                // Người dùng đăng nhập bằng tài khoản Google thì xử lý đăng xuất khỏi tài khoản Google
//                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken(getString(R.string.default_web_client_id))
//                        .requestEmail()
//                        .build();
//                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
//                googleSignInClient.signOut().addOnCompleteListener(this, task -> {
//                    FirebaseAuth.getInstance().signOut();
//
//                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();

            // Đăng xuất tài khoản Google
            FirebaseAuth.getInstance().signOut();
            // Mở trang đăng nhập
            Intent intent = new Intent(SettingsActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}