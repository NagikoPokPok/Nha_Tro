package edu.poly.nhtr.Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import edu.poly.nhtr.Adapter.TabLayoutAdapter;
import edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding;
import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;

public class MainDetailedRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding binding = ActivityMainDetailedRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
}