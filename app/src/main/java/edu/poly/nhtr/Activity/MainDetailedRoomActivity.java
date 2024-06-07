package edu.poly.nhtr.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import edu.poly.nhtr.Adapter.TabLayoutAdapter;
import edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding;
import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainDetailedRoomActivity extends AppCompatActivity {

    ActivityMainDetailedRoomBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainDetailedRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        boolean hasGuest = checkRoomHasGuest();

        if (!hasGuest) {
            showRoomContractFragment();
        } else {
            showTabLayout();
        }

        getIntentFromRoomFragment();

        setListeners();
    }

    // Lấy dữ liệu từ Room
    private void getIntentFromRoomFragment() {

        // Nhận dữ liệu từ Intent Home
        Room room = (Room) getIntent().getSerializableExtra("room");
        String roomId = Objects.requireNonNull(room).getRoomId();
        String roomTitle = room.getNameRoom();
        preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
        preferenceManager.putString(Constants.KEY_NAME_ROOM, roomTitle);

        // Tạo Bundle chứa dữ liệu Room
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);

        // Tạo instance của các Fragment trong MainDetailedRoomActivity
        RoomGuestFragment roomGuestFragment = new RoomGuestFragment();
        RoomBillFragment roomBillFragment = new RoomBillFragment();
        RoomContractFragment roomContractFragment = new RoomContractFragment();
        RoomServiceFragment roomServiceFragment = new RoomServiceFragment();

        // Đặt Bundle vào Fragment
        roomGuestFragment.setArguments(bundle);
        roomBillFragment.setArguments(bundle);
        roomContractFragment.setArguments(bundle);
        roomServiceFragment.setArguments(bundle);

        setListeners();

    }

    // Hàm tổng hợp các tương tác binding
    private void setListeners() {
        getRoomTitle();
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

    private void getRoomTitle() {
        binding.nameRoom.setText(preferenceManager.getString(Constants.KEY_NAME_ROOM));
    }

    private boolean checkRoomHasGuest() {
        return false;
    }
}