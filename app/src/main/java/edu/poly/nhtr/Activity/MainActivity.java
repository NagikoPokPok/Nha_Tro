package edu.poly.nhtr.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    // Other code...

    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Other initialization code...

        //Button buttonLogout = findViewById(R.id.buttonLogout);
        binding.buttonLogout.setOnClickListener(v -> logout());
    }

    // Other methods...

    public void logout() {
        // Add code to perform logout actions, such as clearing session data, signing out, etc.
        // For example, if you're using Firebase:
        FirebaseAuth.getInstance().signOut();

        // Start the SignInActivity after logout
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish(); // Optionally, you can finish the MainActivity to prevent going back to it.
    }
}
