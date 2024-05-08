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

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Activity.ChangeProfileActivity;
import edu.poly.nhtr.Activity.MainActivity;
import edu.poly.nhtr.Activity.SettingsActivity;
import edu.poly.nhtr.Activity.SignInActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentSettingBinding;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    FragmentSettingBinding binding;

    private PreferenceManager preferenceManager;

    SwitchCompat switchMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
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

        loadUserDetails();
        setListeners();

        return binding.getRoot();
    }



    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        return resizedBitmap;
    }

    private void loadUserDetails() {
        binding.edtName.setText(preferenceManager.getString(Constants.KEY_NAME));
        if(preferenceManager.getString(Constants.KEY_PHONE_NUMBER)!=null)
            binding.phoneNum.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            try {
                Bitmap profileImage = getConversionImage(encodedImage);
                binding.imgProfile.setImageBitmap(profileImage);
                binding.imgAva.setVisibility(View.INVISIBLE); // Ẩn ảnh mặc định nếu có ảnh người dùng
            } catch (Exception e) {
                // Xử lý ngoại lệ khi không thể tải ảnh
                binding.imgAva.setVisibility(View.VISIBLE); // Hiển thị ảnh mặc định nếu xảy ra ngoại lệ
                Toast.makeText(requireActivity().getApplicationContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định và ẩn ảnh người dùng
            binding.imgAva.setVisibility(View.VISIBLE);
        }
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

        // Kiểm tra tài khoản đăng nhập là tài khoản Email hay Google
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<? extends UserInfo> providerData = currentUser.getProviderData();
            // Lặp qua danh sách các tài khoản cấp thông tin xác thực
            for (UserInfo userInfo : providerData) {
                String providerId = userInfo.getProviderId();
                if (providerId.equals("google.com")) {
                    // TH đăng nhập bằng tài khoản Google
                    getInfoFromGoogle();
                    return; // Thoát khỏi vòng lặp khi thấy đúng tài khoản Google
                }
            }
            // Nếu là tài khoản Email thì tải thông tin người dùng từ SharedPreferences
            loadUserDetails();
        } else {
            // Không có người dùng nào đang đăng nhập, tải thông tin từ SharedPreferences
            loadUserDetails();
        }
    }


    public void back() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }


    // Lấy ảnh đại diện và tên từ Google
    private void getInfoFromGoogle() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity().getApplicationContext());
        if (account != null) {
            String userName = account.getDisplayName();
            binding.edtName.setText(userName);

            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            new DownloadImageTask(binding.imgProfile).execute(photoUrl);

            binding.imgAva.setVisibility(View.INVISIBLE);
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(requireActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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

        private void loading(Boolean isLoading) {
            if (isLoading) {
                binding.btnLogout.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.INVISIBLE);
                binding.btnLogout.setVisibility(View.VISIBLE);
            }
        }

        private void switchModeTheme() {
            sharedPreferences = requireContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
            nightMode = sharedPreferences.getBoolean("nightMode", false);

            if(nightMode) {
                switchMode.setChecked(true);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            switchMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nightMode) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        editor = sharedPreferences.edit();
                        editor.putBoolean("nightMode", false);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        editor = sharedPreferences.edit();
                        editor.putBoolean("nightMode", true);
                    }
                    editor.apply();
                }
            });
        }
}