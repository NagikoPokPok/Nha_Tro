package edu.poly.nhtr.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

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

import java.io.InputStream;
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

    private void loadUserDetails(){
        binding.edtName.setText(preferenceManager.getString(Constants.KEY_NAME));
        try {
            binding.imgProfile.setImageBitmap(getConversionImage(preferenceManager.getString(Constants.KEY_IMAGE)));
            binding.phoneNum.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
            binding.imgAva.setVisibility(View.INVISIBLE);
        }catch (Exception e){
            Toast.makeText(requireActivity().getApplicationContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setListeners() {
        binding.btnlogout.setOnClickListener(v -> {
            try {
                logout();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        binding.ChangeProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ChangeProfileActivity.class);
            requireActivity().startActivity(intent);
            requireActivity().finish();

        });

        binding.btnBack.setOnClickListener(v -> back());

        getInfoFromGoogle();
    }

    public void back() {


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
    public void logout() throws InterruptedException {

        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // Xóa cài đặt về người dùng
        PreferenceManager preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, false);
        preferenceManager.removePreference(Constants.KEY_USER_ID);
        preferenceManager.removePreference(Constants.KEY_NAME);

        // Trở lại Settings Activity
        Intent intent = new Intent(requireActivity(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        requireActivity().startActivity(intent);
        requireActivity().finish();

    }
}