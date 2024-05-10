package edu.poly.nhtr.fragment;


import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;


import java.io.InputStream;

import java.util.Objects;

import edu.poly.nhtr.Activity.ChangeProfileActivity;
import edu.poly.nhtr.Activity.MainActivity;

import edu.poly.nhtr.Activity.SignInActivity;

import edu.poly.nhtr.databinding.FragmentSettingBinding;
import edu.poly.nhtr.interfaces.SettingsInterface;
import edu.poly.nhtr.presenters.SettingsPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment implements SettingsInterface {

    FragmentSettingBinding binding;

    SharedPreferences sharedPreferences;
    private PreferenceManager preferenceManager;
    private SettingsPresenter settingsPresenter;
    SwitchCompat switchMode;

    boolean nightMode;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        settingsPresenter = new SettingsPresenter(this, requireContext(), preferenceManager); // Truyền context vào đây

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        switchMode = binding.nightMode;

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
                logout(); // Gọi phương thức logout() của presenter
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

    // Triển khai các phương thức mới từ SettingsInterface

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

    }

    public void logout() throws InterruptedException {
        showToast("Signing out ...");
        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // Xóa cài đặt về người dùng
        PreferenceManager preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
        preferenceManager.removePreference(Constants.KEY_USER_ID);
        preferenceManager.removePreference(Constants.KEY_NAME);
        preferenceManager.removePreference(Constants.KEY_PHONE_NUMBER);
        preferenceManager.removePreference(Constants.KEY_ADDRESS);

        // Chuyển theme
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("nightMode", false);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Trở lại Settings Activity
        Intent intent = new Intent(requireContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }


    @Override
    public void switchModeTheme() {
        settingsPresenter.switchModeTheme();
    }

    @Override
    public void setNightMode(boolean nightMode) {
        switchMode.setChecked(nightMode);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
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