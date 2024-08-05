package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
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

        setupViewPagerAndTabLayout();
        customizeTabAppearance();
    }

    private void setupViewPagerAndTabLayout() {
        List<Class<? extends Fragment>> fragmentClasses = getFragmentClasses();
        List<String> titles = getTabTitles();

        Bundle bundle = createFragmentArguments();
        TabLayoutAdapter tabLayoutAdapter = new TabLayoutAdapter(this, fragmentClasses, bundle);
        binding.viewPager.setAdapter(tabLayoutAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position < titles.size()) {
                tab.setText(titles.get(position));
            }
        }).attach();

        if (targetFragmentIndex >= 0) {
            binding.viewPager.setCurrentItem(targetFragmentIndex);
        }
    }

    private List<Class<? extends Fragment>> getFragmentClasses() {
        List<Class<? extends Fragment>> fragmentClasses = new ArrayList<>();
        fragmentClasses.add(RoomGuestFragment.class);
        fragmentClasses.add(RoomServiceFragment.class);
        fragmentClasses.add(RoomBillContainerFragment.class);
        fragmentClasses.add(RoomContractFragment.class);
        return fragmentClasses;
    }

    private List<String> getTabTitles() {
        List<String> titles = new ArrayList<>();
        titles.add("Khách");
        titles.add("Dịch vụ");
        titles.add("Hóa đơn");
        titles.add("Hợp đồng");
        return titles;
    }

    private Bundle createFragmentArguments() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        bundle.putSerializable("home", home);
        return bundle;
    }

    private void customizeTabAppearance() {
        TabLayout tabLayout = binding.tabLayout;
        int colorGray = ContextCompat.getColor(this, R.color.colorGray);
        int colorWhite = ContextCompat.getColor(this, R.color.white);
        Typeface customFont = ResourcesCompat.getFont(this, R.font.inter_light);
        Typeface customFontSelected = ResourcesCompat.getFont(this, R.font.inter_bold);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView tabTextView = createTabTextView(tab.getText(), i == tabLayout.getSelectedTabPosition(), customFont, customFontSelected, colorGray, colorWhite);
                tab.setCustomView(tabTextView);
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getCustomView() instanceof TextView) {
                    updateTabTextViewAppearance((TextView) tab.getCustomView(), customFontSelected, colorWhite, 16);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() instanceof TextView) {
                    updateTabTextViewAppearance((TextView) tab.getCustomView(), customFont, colorGray, 15);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Không cần làm gì khi tab được chọn lại
            }
        });
    }

    private TextView createTabTextView(CharSequence text, boolean isSelected, Typeface customFont, Typeface customFontSelected, int colorGray, int colorWhite) {
        TextView tabTextView = new TextView(this);
        tabTextView.setText(text);
        tabTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        if (isSelected) {
            tabTextView.setTextColor(colorWhite);
            tabTextView.setTypeface(customFontSelected);
            tabTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        } else {
            tabTextView.setTextColor(colorGray);
            tabTextView.setTypeface(customFont);
            tabTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
        return tabTextView;
    }

    private void updateTabTextViewAppearance(TextView textView, Typeface typeface, int color, int textSize) {
        textView.setTextColor(color);
        textView.setTypeface(typeface);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
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
