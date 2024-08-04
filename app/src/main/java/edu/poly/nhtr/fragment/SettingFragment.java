package edu.poly.nhtr.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;

import edu.poly.nhtr.Activity.ChangeProfileActivity;
import edu.poly.nhtr.Activity.MainActivity;
import edu.poly.nhtr.Activity.SignInActivity;
import edu.poly.nhtr.databinding.FragmentSettingBinding;
import edu.poly.nhtr.interfaces.SettingsInterface;
import edu.poly.nhtr.presenters.SettingsPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class SettingFragment extends Fragment implements SettingsInterface {

    FragmentSettingBinding binding;
    private SettingsPresenter settingsPresenter;
    SwitchCompat switchMode;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        switchMode = binding.nightMode;


        settingsPresenter = new SettingsPresenter(this, requireContext(), new PreferenceManager(requireContext()));
        settingsPresenter.loadUserDetails();
        setListeners();

        return binding.getRoot();
    }

    @Override
    public void loadUserDetails(String name, String phoneNumber, Bitmap profileImage) {
        binding.edtName.setText(name);
        if (phoneNumber != null) {
            binding.phoneNum.setText(phoneNumber);
        }
        if (profileImage != null) {
            binding.imgProfile.setImageBitmap(profileImage);
            binding.imgAva.setVisibility(View.INVISIBLE);
        } else {
            binding.imgAva.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Bitmap getConversionImage(String encodedImage) {
        return null;
    }

    private void setListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            try {
                logout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        binding.ChangeProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangeProfileActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        binding.btnBack.setOnClickListener(v -> back());

        switchModeTheme();

        settingsPresenter.checkAccount();
    }

    public void back() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void getInfoFromGoogle() {
        settingsPresenter.getInfoFromGoogle();
    }

    @Override
    public void setUserName(String userName) {
        binding.edtName.setText(userName);
    }

    @Override
    public ImageView getProfileImageView() {
        return binding.imgProfile;
    }

    @Override
    public void hideAvatar() {
        binding.imgAva.setVisibility(View.INVISIBLE);
    }

    public void showToast(String message) {
        Toast.makeText(requireActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void logout() throws InterruptedException {
        showToast("Signing out ...");
        loading(true);

        // Kiểm tra nếu người dùng đăng nhập bằng tài khoản Google
        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                FirebaseAuth.getInstance().getCurrentUser().getProviderData().stream()
                        .anyMatch(profile -> "google.com".equals(profile.getProviderId()))) {

            // Đăng xuất tài khoản Google
            GoogleSignIn.getClient(requireActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    .addOnCompleteListener(requireActivity(), task -> {
                        // Đợi 2 giây trước khi thực hiện đăng xuất Firebase
                        new android.os.Handler().postDelayed(() -> {
                            FirebaseAuth.getInstance().signOut();
                            completeLogout();
                        }, 1000);
                    })
                    .addOnFailureListener(e -> {
                        showToast("Unable to sign out");
                        loading(false);
                    });

        } else {
            // Đăng xuất tài khoản thường
            // Đợi 2 giây trước khi thực hiện đăng xuất
            new android.os.Handler().postDelayed(this::completeLogout, 1000);
        }
    }

    private void completeLogout() {
        PreferenceManager preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
        preferenceManager.removePreference(Constants.KEY_USER_ID);
        preferenceManager.removePreference(Constants.KEY_NAME);
        preferenceManager.removePreference(Constants.KEY_PHONE_NUMBER);
        preferenceManager.removePreference(Constants.KEY_ADDRESS);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("nightMode", false);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Intent intent = new Intent(requireContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        loading(false);
    }



    @Override
    public void switchModeTheme() {
        settingsPresenter.switchModeTheme();
    }


    @Override
    public void setNightMode(boolean nightMode) {
        switchMode.setChecked(nightMode);
        AppCompatDelegate.setDefaultNightMode(nightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void setSwitchClickListener(View.OnClickListener listener) {
        switchMode.setOnClickListener(listener);
    }

    @Override
    public void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnLogout.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnLogout.setVisibility(View.VISIBLE);
        }
    }
}
