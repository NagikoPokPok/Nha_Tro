package edu.poly.nhtr.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

    private ActivityMainBinding binding;
    private FirebaseAuth user;
    private FirebaseFirestore firestore;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Other initialization code...

        //Button buttonLogout = findViewById(R.id.buttonLogout);
        binding.buttonLogout.setOnClickListener(v -> logout());
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> logout());


        binding.btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChangeProfileActivity.class);
                startActivity(intent);
                //finish();
            }
        });
    }

    // Other methods...

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Đăng xuất khỏi tài khoản
    public void logout() {
        showToast("Signing out ...");
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (googleSignInAccount == null) {
//            // Đăng xuất khỏi Firebase Authentication
//            FirebaseAuth.getInstance().signOut();
//
//            // Xóa dữ liệu từ Firestore
//            FirebaseFirestore database = FirebaseFirestore.getInstance();
//            DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
//                    .document(preferenceManager.getString(Constants.KEY_USER_ID));
//
//            // Cập nhật thông tin cần xóa (ví dụ: FCM token)
//            HashMap<String, Object> updates = new HashMap<>();
//            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
//
//            // Thực hiện cập nhật và xóa thông tin khỏi Firestore
//            documentReference.update(updates)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            preferenceManager.clear(); // Xóa thông tin trạng thái đăng nhập
//                            navigateToSignIn(); // Chuyển hướng đến trang đăng nhập
//                        } else {
//                            showToast("Không thể đăng xuất từ Firestore");
//                        }
//                    });
        } else {
            // TH đăng nhập bằng tài khoản Google
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }


}
