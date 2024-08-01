package edu.poly.nhtr.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private String currentFragmentTag;
    private Bundle bundle;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());


        // Receive Home object from Intent
        Home home = (Home) getIntent().getSerializableExtra("home");
        if (home != null) {
            String homeId = home.getIdHome();
            String nameHome = home.getNameHome();
            preferenceManager.putString(Constants.KEY_HOME_ID, homeId);
            preferenceManager.putString(Constants.KEY_NAME_HOME, nameHome);

            // Create Bundle to pass Home object to fragments
            bundle = new Bundle();
            bundle.putSerializable("home", home);
        }

        // Kiểm tra xem có thông báo được truyền từ Intent không
        String documentId = getIntent().getStringExtra("notification_document_id");
        if (documentId != null) {
            // Cập nhật trạng thái đã đọc của thông báo trong Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                    .document(documentId)
                    .update(Constants.KEY_NOTIFICATION_IS_READ, true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xử lý thành công
                            // Ví dụ: hiển thị thông báo hoặc cập nhật UI
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý khi thất bại
                        }
                    });
        }

        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                replaceFragment(currentFragment, currentFragmentTag);
            }
        } else {
            String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
            if (fragmentToLoad != null && fragmentToLoad.equals("IndexFragment")) {
                replaceFragment(new IndexFragment(), "IndexFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_index);
            } else if (fragmentToLoad != null && fragmentToLoad.equals("ServiceFragment")) {
                replaceFragment(new ServiceFragment(), "ServiceFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_index);
            } else if (fragmentToLoad != null && fragmentToLoad.equals("StatisticFragment")) {
                replaceFragment(new StatisticFragment(), "StatisticFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_index);
            } else {
                // Load RoomFragment (mặc định)
                replaceFragment(new RoomFragment(), "RoomFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_room);
            }
        }

        setClickNavigationBottomMenu();
        setListeners();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_FRAGMENT_TAG, currentFragmentTag);
    }

    public void setClickNavigationBottomMenu() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String tag = null;
            if (item.getItemId() == R.id.menu_room) {
                fragment = new RoomFragment();
                tag = "RoomFragment";
            } else if (item.getItemId() == R.id.menu_services) {
                fragment = new ServiceFragment();
                tag = "ServiceFragment";
            } else if (item.getItemId() == R.id.menu_statistic) {
                fragment = new StatisticFragment();
                tag = "StatisticFragment";
            } else if (item.getItemId() == R.id.menu_index) {
                fragment = new IndexFragment();
                tag = "IndexFragment";
            }
            if (fragment != null) {
                replaceFragment(fragment, tag);
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
        currentFragmentTag = tag;
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
