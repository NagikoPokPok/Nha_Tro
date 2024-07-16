package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.poly.nhtr.Adapter.TabLayoutAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding;
import edu.poly.nhtr.fragment.GuestAddContractFragment;
import edu.poly.nhtr.fragment.RoomBillContainerFragment;
import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomMakeBillFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainDetailedRoomActivity extends AppCompatActivity {

    private ActivityMainDetailedRoomBinding binding;
    private PreferenceManager preferenceManager;
    private Room room;
    private boolean hasMainGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainDetailedRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        RoomViewModel roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        room = (Room) getIntent().getSerializableExtra("room");
        if (room != null) {
            setupRoomDetails();
            checkRoomHasMainGuest();
            roomViewModel.setRoom(room);
        } else {
            finish();
        }

        setListeners();
    }

    private void setupRoomDetails() {
        String roomId = room.getRoomId();
        String roomTitle = room.getNameRoom();
        preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
        preferenceManager.putString(Constants.KEY_NAME_ROOM, roomTitle);
    }

    private void setListeners() {
        binding.nameRoom.setText(String.format("Phòng %s", preferenceManager.getString(Constants.KEY_NAME_ROOM)));
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void showTabLayout() {
        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.VISIBLE);

        TabLayoutAdapter tabLayoutAdapter = new TabLayoutAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);

        // Create fragments and set arguments
        Fragment roomGuestFragment = new RoomGuestFragment();
        roomGuestFragment.setArguments(bundle);
        Fragment roomServiceFragment = new RoomServiceFragment();
        roomServiceFragment.setArguments(bundle);
        Fragment roomBillFragment = new RoomBillFragment();
        roomBillFragment.setArguments(bundle);
        Fragment roomContractFragment = new RoomContractFragment();
        roomContractFragment.setArguments(bundle);

        Fragment roomBillContainerFragment = new RoomBillContainerFragment();
        roomBillContainerFragment.setArguments(bundle);


        // Add fragments to adapter
        tabLayoutAdapter.addFragment(roomGuestFragment, "Khách");
        tabLayoutAdapter.addFragment(roomServiceFragment, "Dịch vụ");
        //tabLayoutAdapter.addFragment(roomBillFragment, "Hóa đơn");
        tabLayoutAdapter.addFragment(roomBillContainerFragment, "Hóa đơn");
        tabLayoutAdapter.addFragment(roomContractFragment, "Hợp đồng");

        binding.viewPager.setAdapter(tabLayoutAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    private void showRoomContractFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RoomGuestContractFragment fragment = new RoomGuestContractFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
        binding.viewPager.setVisibility(View.GONE);
        binding.tabLayout.setVisibility(View.GONE);
    }

    public void showRoomMakeBillFragment(RoomBill bill) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RoomMakeBillFragment fragment = new RoomMakeBillFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bill", bill);
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null); // Để quay lại Fragment trước đó khi cần
        fragmentTransaction.commit();
       binding.viewPager.setVisibility(View.GONE);
       binding.tabLayout.setVisibility(View.GONE);
    }

    private void checkRoomHasMainGuest() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String roomId = room.getRoomId();

        db.collection("contracts")
                .whereEqualTo(Constants.KEY_ROOM_ID, roomId)
                .whereEqualTo(Constants.KEY_CONTRACT_STATUS, true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        hasMainGuest = true;
                        showTabLayout();
                    } else {
                        hasMainGuest = false;
                        showRoomContractFragment();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof GuestAddContractFragment ||
                currentFragment instanceof RoomGuestContractFragment ||
                currentFragment instanceof RoomGuestFragment) {
            if (!hasMainGuest) {
                Intent intent = new Intent(MainDetailedRoomActivity.this, MainRoomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void showTabLayoutAndRoomGuestFragment() {
        showTabLayout();

        RoomGuestFragment roomGuestFragment = new RoomGuestFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        roomGuestFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, roomGuestFragment)
                .commit();
    }


}
