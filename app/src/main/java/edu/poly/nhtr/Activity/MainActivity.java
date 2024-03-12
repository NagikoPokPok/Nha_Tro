package edu.poly.nhtr.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    // Other code...
    Button changeProfile;
    PreferenceManager preferenceManager;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Other initialization code...

        //Button buttonLogout = findViewById(R.id.buttonLogout);
//        binding.buttonLogout.setOnClickListener(v -> logout());
//        Button buttonLogout = findViewById(R.id.buttonLogout);
//        buttonLogout.setOnClickListener(v -> logout());


//        changeProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, ChangeProfileActivity.class);
//                startActivity(intent);
//                //finish();
//            }
//        });
    }

    // Other methods...

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

                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
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
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
                }
    }
}
