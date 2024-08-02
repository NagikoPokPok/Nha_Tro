package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Adapter.TabLayoutAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainDetailedRoomBinding;
import edu.poly.nhtr.fragment.GuestAddContractFragment;
import edu.poly.nhtr.fragment.GuestEditContractFragment;
import edu.poly.nhtr.fragment.GuestPrintContractFragment;
import edu.poly.nhtr.fragment.GuestViewContractFragment;
import edu.poly.nhtr.fragment.RoomBillContainerFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainDetailedRoomActivity extends AppCompatActivity implements RoomContractFragment.OnFragmentInteractionListener {

    private ActivityMainDetailedRoomBinding binding;
    private PreferenceManager preferenceManager;
    private Room room;
    private boolean hasMainGuest = false;
    private Home home;
    private String roomPrice;
    private int targetFragmentIndex = -1; // Default to no target


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainDetailedRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        RoomViewModel roomViewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        room = (Room) getIntent().getSerializableExtra("room");
        home = (Home) getIntent().getSerializableExtra("home");
        roomPrice = getIntent().getStringExtra("room_price");
        targetFragmentIndex = getIntent().getIntExtra("target_fragment_index", -1); // Get target fragment index

        if (room != null && home != null) {
            setupRoomDetails();
            checkRoomHasMainGuest();
            roomViewModel.setRoom(room);
        } else {
            finish();
        }

        // Kiểm tra xem có thông báo được truyền từ Intent không
        String documentId = getIntent().getStringExtra("notification_document_id");
        if (documentId != null) {
            // Cập nhật trạng thái đã đọc của thông báo trong Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                    .document(documentId)
                    .update(Constants.KEY_NOTIFICATION_IS_READ, true)
                    .addOnSuccessListener(aVoid -> {
                        // Xử lý thành công
                        // Ví dụ: hiển thị thông báo hoặc cập nhật UI
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý khi thất bại
                    });
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

        // Tạo danh sách các lớp Fragment
        List<Class<? extends Fragment>> fragmentClasses = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        bundle.putSerializable("home", home);

        // Thêm các lớp Fragment vào danh sách
        fragmentClasses.add(RoomGuestFragment.class);
        fragmentClasses.add(RoomServiceFragment.class);
        fragmentClasses.add(RoomBillContainerFragment.class);
        fragmentClasses.add(RoomContractFragment.class);

        titles.add("Khách");
        titles.add("Dịch vụ");
        titles.add("Hóa đơn");
        titles.add("Hợp đồng");

        // Khởi tạo adapter với danh sách lớp Fragment và đối số
        TabLayoutAdapter tabLayoutAdapter = new TabLayoutAdapter(this, fragmentClasses, bundle);
        binding.viewPager.setAdapter(tabLayoutAdapter);

        // Thiết lập TabLayout với ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> tab.setText(titles.get(position))).attach();

        if (targetFragmentIndex >= 0) {
            binding.viewPager.setCurrentItem(targetFragmentIndex);
        }

//        // Refresh fragments when switching tabs
//        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                tabLayoutAdapter.notifyItemChanged(position);
//            }
//        });

//        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                if (tab != null) {
//                    int position = tab.getPosition();
//                    Toast.makeText(MainDetailedRoomActivity.this, position + "", Toast.LENGTH_SHORT).show();
//
//                    // Cập nhật ViewPager
//                    binding.viewPager.setCurrentItem(position, false);
//
//                    // Tạo mới và thay thế fragment trong container
//                    Fragment fragment = createFragmentForPosition(position);
//                    replaceFragment(fragment);
//                }
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                // Implement if needed
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//                // Implement if needed
//
//            }
//        });


    }

    private Fragment createFragmentForPosition(int position) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room); // Thay đổi tên key nếu cần
        bundle.putSerializable("home", home); // Thay đổi tên key nếu cần

        switch (position) {
            case 0:
                fragment = new RoomGuestFragment();
                break;
            case 1:
                fragment = new RoomServiceFragment();
                break;
            case 2:
                fragment = new RoomBillContainerFragment();
                break;
            case 3:
                fragment = new RoomContractFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            fragment.setArguments(bundle); // Đặt Bundle cho fragment
        }

        return fragment;
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
        }
    }

    private void showRoomContractFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RoomGuestContractFragment fragment = new RoomGuestContractFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        bundle.putSerializable("home", home);
        bundle.putString("room_price", roomPrice);
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, fragment);
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
    public void onHideTabLayoutAndViewPager() {
        binding.tabLayout.setVisibility(View.GONE);
        binding.viewPager.setVisibility(View.GONE);
    }

    @Override
    public void showTabLayoutAndViewPager() {
        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof GuestViewContractFragment ||
                currentFragment instanceof GuestEditContractFragment ||
                currentFragment instanceof GuestPrintContractFragment) {
            showTabLayoutAndViewPager();
            super.onBackPressed();
        } else if (currentFragment instanceof GuestAddContractFragment) {
            if (!hasMainGuest) {
                Intent intent = new Intent(MainDetailedRoomActivity.this, MainRoomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                showTabLayoutAndViewPager();
                super.onBackPressed();
            }
        } else if (currentFragment instanceof RoomGuestContractFragment) {
            if (!hasMainGuest) {
                Intent intent = new Intent(MainDetailedRoomActivity.this, MainRoomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                showTabLayoutAndViewPager();
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
        bundle.putSerializable("home", home);
        roomGuestFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, roomGuestFragment)
                .commit();
    }

    public void showTabLayoutEditRoomGuestFragment() {
        showTabLayout();

        RoomGuestFragment roomGuestFragment = new RoomGuestFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        bundle.putSerializable("home", home);
        roomGuestFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, roomGuestFragment)
                .commit();

//        roomGuestFragment.onResume();
    }
}
