package edu.poly.nhtr.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainRoomBinding;
import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.fragment.SettingFragment;

public class MainRoomActivity extends AppCompatActivity {


    ActivityMainRoomBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item ->{
            if (item.getItemId() == R.id.menu_home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.menu_settings) {
                replaceFragment(new SettingFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.scroll_layout, fragment);
        fragmentTransaction.commit();
    }
}
