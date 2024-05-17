package edu.poly.nhtr.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainRoomBinding;
import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.fragment.IndexFragment;
import edu.poly.nhtr.fragment.RoomFragment;
import edu.poly.nhtr.fragment.ServiceFragment;
import edu.poly.nhtr.fragment.SettingFragment;
import edu.poly.nhtr.fragment.StatisticFragment;

public class MainRoomActivity extends AppCompatActivity {


    ActivityMainRoomBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new RoomFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item ->{
            if (item.getItemId() == R.id.menu_room) {
                replaceFragment(new RoomFragment());
            } else if (item.getItemId() == R.id.menu_index) {
                replaceFragment(new IndexFragment());
            } else if (item.getItemId() == R.id.menu_services) {
                replaceFragment(new ServiceFragment());
            } else if (item.getItemId() == R.id.menu_statistic) {
                replaceFragment(new StatisticFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
