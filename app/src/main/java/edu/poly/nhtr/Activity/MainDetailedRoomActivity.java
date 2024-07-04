package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import edu.poly.nhtr.Adapter.TabLayoutAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding;
import edu.poly.nhtr.fragment.GuestAddContractFragment;
import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainDetailedRoomActivity extends AppCompatActivity {

    private ActivityMainDetailedRoomBinding binding;
    private PreferenceManager preferenceManager;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainDetailedRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        setupView();
        setListeners();
    }

    private void setupView() {
        room = (Room) getIntent().getSerializableExtra("room");
        if (room != null) {
            String roomId = room.getRoomId();
            String roomTitle = room.getNameRoom();
            preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
            preferenceManager.putString(Constants.KEY_NAME_ROOM, roomTitle);

            Bundle bundle = new Bundle();
            bundle.putSerializable("room", room);
            setFragmentArguments(bundle);
            if (checkRoomHasGuest()) {
                showTabLayout();
            } else {
                showRoomContractFragment();
            }
        } else {
            // Handle case when room is null
            finish();
        }
    }

    private void setFragmentArguments(Bundle bundle) {
        setArgumentsForFragment(new RoomGuestFragment(), bundle);
        setArgumentsForFragment(new RoomBillFragment(), bundle);
        setArgumentsForFragment(new RoomContractFragment(), bundle);
        setArgumentsForFragment(new RoomServiceFragment(), bundle);
        setArgumentsForFragment(new RoomGuestContractFragment(), bundle);
        setArgumentsForFragment(new GuestAddContractFragment(), bundle);
    }

    private void setArgumentsForFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
    }

    private void setListeners() {
        binding.nameRoom.setText(String.format("Phòng %s", preferenceManager.getString(Constants.KEY_NAME_ROOM)));
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void showTabLayout() {
        binding.fragmentContainer.setVisibility(View.GONE);
        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.VISIBLE);

        TabLayoutAdapter tabLayoutAdapter = new TabLayoutAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        tabLayoutAdapter.addFragment(new RoomGuestFragment(), "Khách");
        tabLayoutAdapter.addFragment(new RoomServiceFragment(), "Dịch vụ");
        tabLayoutAdapter.addFragment(new RoomBillFragment(), "Hóa đơn");
        tabLayoutAdapter.addFragment(new RoomContractFragment(), "Hợp đồng");

        binding.viewPager.setAdapter(tabLayoutAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    private void showRoomContractFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RoomGuestContractFragment fragment = new RoomGuestContractFragment();
        fragment.setArguments(getIntent().getExtras());  // Pass the arguments to the fragment
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.GONE);
        binding.tabLayout.setVisibility(View.GONE);
    }

    private boolean checkRoomHasGuest() {
        // Implement your logic to check if the room has a guest
        return false;
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof GuestAddContractFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                Intent intent = new Intent(MainDetailedRoomActivity.this, MainRoomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        } else {
            super.onBackPressed();  // Handle other cases normally
        }
    }
}
