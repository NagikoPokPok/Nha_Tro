package edu.poly.nhtr.Activity;
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

        // Khởi tạo fragment mặc định là RoomFragment
        loadFragment(new RoomFragment(), "RoomFragment");

        setListeners();

        // Load bottom menu
        setClickNavigationBottomMenu();
    }

    private void loadFragment(Fragment fragment, String tag) {
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
        currentFragment = fragment;
    }

    public void setClickNavigationBottomMenu() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_room) {
                loadFragment(new RoomFragment(), "RoomFragment");
            } else if (itemId == R.id.menu_services) {
                loadFragment(new ServiceFragment(), "ServiceFragment");
            } else if (itemId == R.id.menu_statistic) {
                loadFragment(new StatisticFragment(), "StatisticFragment");
            } else if (itemId == R.id.menu_index) {
                loadFragment(new IndexFragment(), "IndexFragment");
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
