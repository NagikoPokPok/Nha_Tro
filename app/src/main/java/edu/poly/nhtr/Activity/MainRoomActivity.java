package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainRoomBinding;
import edu.poly.nhtr.fragment.IndexFragment;
import edu.poly.nhtr.fragment.RoomFragment;
import edu.poly.nhtr.fragment.ServiceFragment;
import edu.poly.nhtr.fragment.StatisticFragment;

public class MainRoomActivity extends AppCompatActivity {

    private String home="";

    ActivityMainRoomBinding binding;
    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private String currentFragmentTag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                replaceFragment(currentFragment, currentFragmentTag);
            }
        }  else {
                replaceFragment(new RoomFragment(), "RoomFragment");
            }

        // Load bottom menu
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
            if (item.getItemId() == R.id.menu_room) {
                fragment = new RoomFragment();
                currentFragmentTag = "RoomFragment";
            } else if (item.getItemId() == R.id.menu_services) {
                fragment = new ServiceFragment();
                currentFragmentTag = "ServiceFragment";
            } else if (item.getItemId() == R.id.menu_statistic) {
                fragment = new StatisticFragment();
                currentFragmentTag = "StatisticFragment";
            } else if (item.getItemId() == R.id.menu_index) {
                fragment = new IndexFragment();
                currentFragmentTag = "IndexFragment";
            }
            if (fragment != null) {
                replaceFragment(fragment, currentFragmentTag);
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }

    private void getHomeId(){
        home = getIntent().getStringExtra("home");
    }

}
