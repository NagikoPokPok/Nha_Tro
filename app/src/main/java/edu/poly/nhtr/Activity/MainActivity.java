package edu.poly.nhtr.Activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;
import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.fragment.NotificationFragment;
import edu.poly.nhtr.fragment.SettingFragment;


public class MainActivity extends AppCompatActivity {

    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    ActivityMainBinding binding;
    private String currentFragmentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cài đặt binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                replaceFragment(currentFragment, currentFragmentTag);
            }
        } else {
            String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
            if (fragmentToLoad != null && fragmentToLoad.equals("NotificationFragment")) {
                replaceFragment(new NotificationFragment(), "NotificationFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_notification);
            } else if (fragmentToLoad != null && fragmentToLoad.equals("SettingFragment")) {
                replaceFragment(new SettingFragment(), "SettingFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_settings);
            } else {
                // Load HomeFragment (mặc định)
                replaceFragment(new HomeFragment(), "HomeFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_home);
            }
        }

        // Load thanh menu dưới
        setClickNavigationBottomMenu();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag);
    }

    public void setClickNavigationBottomMenu() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String tag = null;
            if (item.getItemId() == R.id.menu_home) {
                fragment = new HomeFragment();
                tag = "HomeFragment";
            } else if (item.getItemId() == R.id.menu_settings) {
                fragment = new SettingFragment();
                tag = "SettingFragment";
            } else if (item.getItemId() == R.id.menu_notification) {
                fragment = new NotificationFragment();
                tag = "NotificationFragment";
            }
            if (fragment != null) {
                replaceFragment(fragment, tag);
            }
            return true;
        });
    }

    // Hàm thay thế Fragment
    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
        currentFragmentTag = tag;
    }

}
