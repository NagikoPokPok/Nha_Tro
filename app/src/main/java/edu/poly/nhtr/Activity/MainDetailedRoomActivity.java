package edu.poly.nhtr.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import edu.poly.nhtr.Adapter.TabLayoutAdapter;
import edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding;
import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;

public class MainDetailedRoomActivity extends AppCompatActivity {

    ActivityMainDetailedRoomBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainDetailedRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean hasGuest = checkRoomHasGuest();

        if (!hasGuest) {
            showRoomContractFragment();
        } else {
            showTabLayout();
        }
    }

    private void showTabLayout() {
        binding.fragmentContainer.setVisibility(View.GONE);
        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.VISIBLE);

        TabLayout tabLayout = binding.tabLayout;
        ViewPager viewPager = binding.viewPager;

        TabLayoutAdapter tabLayoutAdapter = new TabLayoutAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        tabLayoutAdapter.addFragment(new RoomGuestFragment(), "Khách");
        tabLayoutAdapter.addFragment(new RoomServiceFragment(), "Dịch vụ");
        tabLayoutAdapter.addFragment(new RoomBillFragment(), "Hóa đơn");
        tabLayoutAdapter.addFragment(new RoomContractFragment(), "Hợp đồng");

        viewPager.setAdapter(tabLayoutAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void showRoomContractFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(edu.poly.nhtr.R.id.fragment_container, new RoomGuestContractFragment());
        fragmentTransaction.commit();
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.GONE);
        binding.tabLayout.setVisibility(View.GONE);
    }


    private boolean checkRoomHasGuest() {
        return false;
    }
}