package edu.poly.nhtr.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainBinding;
import edu.poly.nhtr.fragment.HomeFragment;
import edu.poly.nhtr.fragment.NotificationFragment;
import edu.poly.nhtr.fragment.SettingFragment;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT_TAG";
    private static final String ACTION_UPDATE_BADGE = "edu.poly.nhtr.ACTION_UPDATE_BADGE";

    ActivityMainBinding binding;
    private String currentFragmentTag;
    private PreferenceManager preferenceManager;
    private int numberOfNotificationsAreNotRead;

    private final BroadcastReceiver badgeUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setNotificationBadgeCount();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);

        // Đăng ký BroadcastReceiver
        registerReceiver(badgeUpdateReceiver, new IntentFilter(ACTION_UPDATE_BADGE), Context.RECEIVER_NOT_EXPORTED);

        setNotificationBadgeCount();


        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString(CURRENT_FRAGMENT_TAG);
            Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
            if (currentFragment != null) {
                replaceFragment(currentFragment, currentFragmentTag);
            }
        } else {
            String fragmentToLoad = getIntent().getStringExtra("FRAGMENT_TO_LOAD");
            if (fragmentToLoad != null && fragmentToLoad.equals("NotificationFragment")) {
                replaceFragment(new NotificationFragment(), "NotificationFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_notification);
            } else if (fragmentToLoad != null && fragmentToLoad.equals("SettingFragment")) {
                replaceFragment(new SettingFragment(), "SettingFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_settings);
            } else {
                replaceFragment(new HomeFragment(), "HomeFragment");
                binding.bottomNavigation.setSelectedItemId(R.id.menu_home);
            }
        }


        setClickNavigationBottomMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(badgeUpdateReceiver);
    }

    public void setNotificationBadgeCount() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_NOTIFICATION)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            numberOfNotificationsAreNotRead = 0;
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Boolean isRead = document.getBoolean(Constants.KEY_NOTIFICATION_IS_READ);
                                if (isRead != null && !isRead) {
                                    numberOfNotificationsAreNotRead++;
                                }
                            }
                            BadgeDrawable badge_notification = binding.bottomNavigation.getOrCreateBadge(R.id.menu_notification);
                            if (numberOfNotificationsAreNotRead != 0) {
                                badge_notification.setNumber(numberOfNotificationsAreNotRead);
                                badge_notification.setMaxCharacterCount(3);
                                badge_notification.setVisible(true);
                            } else {
                                badge_notification.setVisible(false);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                });
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
            if (item.getItemId() == R.id.menu_home) {
                fragment = new HomeFragment();
                tag = "HomeFragment";
            } else if (item.getItemId() == R.id.menu_settings) {
                fragment = new SettingFragment();
                tag = "SettingFragment";
            } else if (item.getItemId() == R.id.menu_notification) {
                fragment = new NotificationFragment();
                tag = "NotificationFragment";
            }
            if (fragment != null) {
                replaceFragment(fragment, tag);
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
        currentFragmentTag = tag;
    }
}
