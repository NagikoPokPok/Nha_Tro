package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.PopupMenu;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;

import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Activity.MainRoomActivity;
import edu.poly.nhtr.Adapter.HomeAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentHomeBinding;
import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.presenters.HomePresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements HomeListener {
    //
    private View view;

    private PreferenceManager preferenceManager;
    private HomeAdapter.HomeViewHolder viewHolder;
    private FragmentHomeBinding binding;
    private HomePresenter homePresenter;
    private Dialog dialog;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        dialog = new Dialog(requireActivity());

        // Khai bao presenter
        homePresenter = new HomePresenter(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        binding = FragmentHomeBinding.inflate(getLayoutInflater());

        editFonts();

        //Set preference
        preferenceManager = new PreferenceManager(requireContext().getApplicationContext());

        // Set up RecyclerView layout manager
        binding.homesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        // Load user's information
        loadUserDetails();

        // Load home information
        homePresenter.getHomes();

        setListeners();

        // Xử lý Dialog Thêm nhà trọ
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding.btnAddHome.setOnClickListener(view -> openAddHomeDialog(Gravity.CENTER));

        // Xử lý nút 3 chấm menu
        binding.imgMenuEditDelete.setOnClickListener(this::openMenu);
    }

    private void openMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                // Thực hiện hành động cho mục chỉnh sửa
                showToast("Edit item");
                return true;
            } else if (itemId == R.id.menu_delete) {
                // Thực hiện hành động cho mục xóa
                showToast("Delete item");
                return true;
            }
            return false;
        });
        popupMenu.inflate(R.menu.menu_edit_delete);
        popupMenu.show();
    }

    private void editFonts() {
        //Set three fonts into one textview
        Spannable text1 = new SpannableString("Bạn chưa có nhà trọ\n Hãy nhấn nút ");
        Typeface interLightTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text1.setSpan(new TypefaceSpan(interLightTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.setText(text1);

        Spannable text2 = new SpannableString("+");
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        text2.setSpan(new TypefaceSpan(interBoldTypeface), 0, text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text2);

        Spannable text3 = new SpannableString(" để thêm nhà trọ.");
        Typeface interLightTypeface2 = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text3.setSpan(new TypefaceSpan(interLightTypeface2), 0, text3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        return binding.getRoot();
    }

    private void setListeners() {
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

    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void loadUserDetails() {
        String encodedImg = preferenceManager.getString(Constants.KEY_IMAGE);
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        if (encodedImg != null && !encodedImg.isEmpty()) {
            try {
                Bitmap profileImage = getConversionImage(encodedImg);
                binding.imgProfile.setImageBitmap(profileImage);
                binding.imgAva.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                binding.imgAva.setVisibility(View.VISIBLE); // Nếu không có ảnh thì để mặc định
                Toast.makeText(requireActivity().getApplicationContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định và ẩn ảnh người dùng
            binding.imgAva.setVisibility(View.VISIBLE);
        }
    }


    // Lấy ảnh đại diện và tên từ Google
    private void getInfoFromGoogle() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            String userName = account.getDisplayName();
            binding.name.setText(userName);

            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            new HomeFragment.DownloadImageTask(binding.imgProfile).execute(photoUrl);
        }
    }

    private Spannable customizeText(String s)  // Hàm set mau va font chu cho Text
    {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }

    private void setIDComponents()
    {

    }

    private void openAddHomeDialog(int gravity) {

        setupDialog(R.layout.layout_dialog_add_home, Gravity.CENTER);

        //Anh xa view cho dialog
        TextView nameHome = dialog.findViewById(R.id.txt_name_home);
        TextView addressHome = dialog.findViewById(R.id.txt_address_home);
        TextView title = dialog.findViewById(R.id.txt_title_dialog);
        EditText edtNameHome = dialog.findViewById(R.id.edt_name_home);
        EditText edtAddress = dialog.findViewById(R.id.edt_address_home);
        Button btnAddHome = dialog.findViewById(R.id.btn_add_home);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextInputLayout layoutNameHome = dialog.findViewById(R.id.layout_name_home);
        TextInputLayout layoutAddressHome = dialog.findViewById(R.id.layout_address_home);

        // Set dấu * đỏ cho TextView
        nameHome.append(customizeText(" *"));
        addressHome.append(customizeText(" *"));

        //Set thông tin cho dialog
        title.setText("Tạo mới nhà trọ");
        edtNameHome.setHint("Ví dụ: Nhà trọ MyHome");
        edtAddress.setHint("Ví dụ: 254 Nguyễn Văn Linh");
        btnAddHome.setText("Tạo");


        edtNameHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edtNameHome.getText().toString().trim();
                if(!name.isEmpty())
                {
                    layoutNameHome.setErrorEnabled(false);
                    layoutNameHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = edtAddress.getText().toString().trim();
                if(!address.isEmpty())
                {
                    layoutAddressHome.setErrorEnabled(false);
                    layoutAddressHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Xử lý/ hiệu chỉnh màu nút button add home
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameHome, edtAddress, btnAddHome);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edtNameHome.addTextChangedListener(textWatcher);
        edtAddress.addTextChangedListener(textWatcher);


        // Xử lý sự kiện cho button
        btnAddHome.setOnClickListener(v -> {
            String name = edtNameHome.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            Home home = new Home(name, address);
            homePresenter.addHome(home);

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    // Cập nhật màu cho button
    private void updateButtonState(EditText edtNameHome, EditText edtAddress, Button btn) {
        String name = edtNameHome.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        if (name.isEmpty() || address.isEmpty()) {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
        } else {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getInfoUserFromGoogleAccount() {
        // Lấy thông tin người dùng từ tài khoản Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        String currentUserId = "";
        if (account != null) {
            currentUserId = account.getId();
        } else {
            currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        }
        return currentUserId;
    }

    @Override
    public void putHomeInfoInPreferences(String nameHome, String address, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_HOME_ID, documentReference.getId());
        preferenceManager.putString(Constants.KEY_NAME_HOME, nameHome);
        preferenceManager.putString(Constants.KEY_ADDRESS_HOME, address);
    }

    @Override
    public void dialogClose() {
        dialog.dismiss();
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void addHome(List<Home> homes) {
        HomeAdapter homesAdapter = new HomeAdapter(homes, this);
        binding.homesRecyclerView.setAdapter(homesAdapter);

        // Sắp xếp các homes theo thứ tự từ thời gian khi theem vào
        homes.sort(Comparator.comparing(obj -> obj.dateObject));

        // hàm notifyItemInserted dùng để thông báo cho recycler view rằng có một item được thêm vào adapter
        homesAdapter.notifyDataSetChanged();

        //Sau khi nhận thông báo là có item được inserted thì cho cyclerview cuộn xuống tới item vừa được thêm
        binding.homesRecyclerView.smoothScrollToPosition(homes.size()-1);


        // Do trong activity_users.xml, usersRecycleView đang được setVisibility là Gone, nên sau
        // khi setAdapter mình phải set lại là VISIBLE
        binding.txtNotification.setVisibility(View.GONE);
        binding.imgAddHome.setVisibility(View.GONE);
        binding.homesRecyclerView.setVisibility(View.VISIBLE);
        binding.frmMenuTools.setVisibility(View.VISIBLE);
        Log.d("MainActivity", "Adapter set successfully");
    }

    @Override
    public void addHomeFailed() {
        binding.txtNotification.setVisibility(View.VISIBLE);
        binding.imgAddHome.setVisibility(View.VISIBLE);
        binding.homesRecyclerView.setVisibility(View.INVISIBLE);
        binding.frmMenuTools.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean isAdded2() {
        return isAdded();
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

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
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            }
        }
    }


    @Override
    public void onHomeClicked(Home home) {
        Intent intent = new Intent(requireContext(), MainRoomActivity.class);
        intent.putExtra("home", home);
        startActivity(intent);
    }


    @Override
    public void openPopup(View view, Home home, ItemContainerHomesBinding binding) {
        openMenuForEachHome(view, home, binding);
    }

    private void openMenuForEachHome(View view, Home home, ItemContainerHomesBinding binding) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                // Thực hiện hành động cho mục chỉnh sửa
                openUpdateHomeDialog(Gravity.CENTER, home);
                return true;
            } else if (itemId == R.id.menu_delete) {
                // Thực hiện hành động cho mục xóa
                //homePresenter.deleteHome(home);
                openDeleteHomeDialog(Gravity.CENTER, home);
                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                binding.frmImage2.setVisibility(View.INVISIBLE);
                binding.frmImage.setVisibility(View.VISIBLE);
            }
        });

        popupMenu.inflate(R.menu.menu_edit_delete);
        popupMenu.show();
    }


    private void openDeleteHomeDialog(int gravity, Home home) {

        setupDialog(R.layout.layout_dialog_delete_home, Gravity.CENTER);

        // Ánh xạ ID
        TextView txt_confirm_delete = dialog.findViewById(R.id.txt_confirm_delete);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_delete_home = dialog.findViewById(R.id.btn_delete_home);

        // Hiệu chỉnh TextView
        String text = " " + home.getNameHome() + " ?";
        txt_confirm_delete.append(text);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_delete_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.deleteHome(home);
            }
        });
    }

    @Override
    public void openDialogSuccess(int id) {
        setupDialog(id, Gravity.CENTER);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void showLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }


    @Override
    public void openConfirmUpdateHome(int gravity, String newNameHome, String newAddressHome, Home home) {
        setupDialog(R.layout.layout_dialog_confirm_update_home, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_confirm_update_home = dialog.findViewById(R.id.btn_confirm_update_home);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_confirm_update_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.updateSuccess(newNameHome, newAddressHome, home);
            }
        });


    }

    @Override
    public void showErrorMessage(String message, int id) {
        TextInputLayout layout_name_home = dialog.findViewById(id);
        layout_name_home.setError(message);

    }

    private void openUpdateHomeDialog(int gravity, Home home) {

        setupDialog(R.layout.layout_dialog_update_home, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_update_home = dialog.findViewById(R.id.btn_add_home);
        EditText edt_new_name_home = dialog.findViewById(R.id.edt_name_home);
        EditText edt_new_address_home = dialog.findViewById(R.id.edt_address_home);
        TextView title = dialog.findViewById(R.id.txt_title_dialog);
        TextView txt_name_home = dialog.findViewById(R.id.txt_name_home);
        TextView txt_address_home = dialog.findViewById(R.id.txt_address_home);
        TextInputLayout layoutNameHome = dialog.findViewById(R.id.layout_name_home);
        TextInputLayout layoutAddressHome = dialog.findViewById(R.id.layout_address_home);


        //Hiện thông tin lên edt
        edt_new_name_home.setText(home.getNameHome());
        edt_new_address_home.setText(home.getAddressHome());
        title.setText("Chỉnh sửa thông tin nhà trọ");
        btn_update_home.setText("Cập nhật");
        txt_name_home.append(customizeText(" *"));
        txt_address_home.append(customizeText(" *"));
        btn_update_home.setBackground(getResources().getDrawable(R.drawable.custom_button_add));

        edt_new_name_home.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edt_new_name_home.getText().toString().trim();
                if(!name.isEmpty())
                {
                    layoutNameHome.setErrorEnabled(false);
                    layoutNameHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_new_address_home.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = edt_new_address_home.getText().toString().trim();
                if(!address.isEmpty())
                {
                    layoutAddressHome.setErrorEnabled(false);
                    layoutAddressHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edt_new_name_home, edt_new_address_home, btn_update_home);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edt_new_name_home.addTextChangedListener(textWatcher);
        edt_new_address_home.addTextChangedListener(textWatcher);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        btn_update_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy dữ liệu
                String newNameHome = edt_new_name_home.getText().toString().trim();
                String newAddressHome = edt_new_address_home.getText().toString().trim();
                homePresenter.updateHome(newNameHome, newAddressHome, home);
            }
        });
    }

    private void setupDialog(int layoutId, int gravity) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = gravity;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(Gravity.CENTER == gravity);
            dialog.show();
        }
    }


}
