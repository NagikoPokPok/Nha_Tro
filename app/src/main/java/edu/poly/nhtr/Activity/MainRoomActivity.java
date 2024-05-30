package edu.poly.nhtr.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

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
    private String home = "";
    private Fragment currentFragment;

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
        Bundle bundle = new Bundle();
        bundle.putSerializable("home", home);

        // Tạo instance của RoomFragment
        RoomFragment roomFragment = new RoomFragment();
        ServiceFragment serviceFragment = new ServiceFragment();
        StatisticFragment statisticFragment = new StatisticFragment();
        IndexFragment indexFragment = new IndexFragment();

        // Đặt Bundle vào Fragment
        roomFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_container, roomFragment, "RoomFragment");
        fragmentTransaction.add(R.id.fragment_container, serviceFragment, "ServiceFragment");
        fragmentTransaction.add(R.id.fragment_container, statisticFragment, "StatisticFragment");
        fragmentTransaction.add(R.id.fragment_container, indexFragment, "IndexFragment");

        fragmentTransaction.hide(serviceFragment);
        fragmentTransaction.hide(statisticFragment);
        fragmentTransaction.hide(indexFragment);

        fragmentTransaction.commit();

        currentFragment = roomFragment;

        setListeners();

        // Load bottom menu
        setClickNavigationBottomMenu();
    }

    public void setClickNavigationBottomMenu() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(currentFragment);

            if (item.getItemId() == R.id.menu_room) {
                currentFragment = fragmentManager.findFragmentByTag("RoomFragment");
            } else if (item.getItemId() == R.id.menu_services) {
                currentFragment = fragmentManager.findFragmentByTag("ServiceFragment");
            } else if (item.getItemId() == R.id.menu_statistic) {
                currentFragment = fragmentManager.findFragmentByTag("StatisticFragment");
            } else if (item.getItemId() == R.id.menu_index) {
                currentFragment = fragmentManager.findFragmentByTag("IndexFragment");
            }

            if (currentFragment != null) {
                fragmentTransaction.show(currentFragment);
                fragmentTransaction.commit();
            }

            return true;
        });
    }

    private void getHomeId() {
        home = getIntent().getStringExtra("home");
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