package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainRoomBinding;
import edu.poly.nhtr.fragment.IndexFragment;
import edu.poly.nhtr.fragment.RoomFragment;
import edu.poly.nhtr.fragment.ServiceFragment;
import edu.poly.nhtr.fragment.StatisticFragment;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainRoomActivity extends AppCompatActivity {

    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    ActivityMainRoomBinding binding;
    private String currentFragmentTag;
    private Bundle bundle;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        // Nhận dữ liệu từ Intent Home
        Home home = (Home) getIntent().getSerializableExtra("home");
        String homeId = Objects.requireNonNull(home).getIdHome();
        String nameHome = home.getNameHome();
        preferenceManager.putString(Constants.KEY_HOME_ID, homeId);
        preferenceManager.putString(Constants.KEY_NAME_HOME, nameHome);

        // Tạo Bundle chứa dữ liệu Home
        bundle = new Bundle();
        bundle.putSerializable("home", home);

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                replaceFragment(currentFragment, currentFragmentTag);
            }
        } else {
            String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
            if (fragmentToLoad != null && fragmentToLoad.equals("IndexFragment")) {
                replaceFragment(new IndexFragment(), "IndexFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_index);
            }else if(fragmentToLoad != null && fragmentToLoad.equals("ServiceFragment"))
            {
                replaceFragment(new IndexFragment(), "ServiceFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_index);
            }else if(fragmentToLoad != null && fragmentToLoad.equals("StatisticFragment"))
            {
                replaceFragment(new IndexFragment(), "StatisticFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_index);
            }
            else {
                // Load RoomFragment (mặc định)
                replaceFragment(new RoomFragment(), "RoomFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_room);
            }
        }

        setClickNavigationBottomMenu();
        setListeners();
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
            if (item.getItemId() == R.id.menu_room) {
                fragment = new RoomFragment();
                tag = "RoomFragment";
            } else if (item.getItemId() == R.id.menu_services) {
                fragment = new ServiceFragment();
                tag = "ServiceFragment";
            } else if (item.getItemId() == R.id.menu_statistic) {
                fragment = new StatisticFragment();
                tag = "StatisticFragment";
            } else if (item.getItemId() == R.id.menu_index) {
                fragment = new IndexFragment();
                tag = "IndexFragment";
            }
            if (fragment != null) {
                replaceFragment(fragment, tag);
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
        currentFragmentTag = tag;
    }

    private void setListeners() {
        loadHomeDetails();
        backToHome();
    }

    private void loadHomeDetails() {
        binding.nameHome.setText(preferenceManager.getString(Constants.KEY_NAME_HOME));
    }

    private void backToHome() {
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(MainRoomActivity.this, MainActivity.class);
            intent.putExtra("FRAGMENT_TO_LOAD", "HomeFragment");
            startActivity(intent);
            finish();
        });
    }
}
