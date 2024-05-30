package edu.poly.nhtr.fragment;

import static android.content.Intent.getIntent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Adapter.HomeAdapter;
import edu.poly.nhtr.Adapter.RoomAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.ActivityMainRoomBinding;
import edu.poly.nhtr.databinding.FragmentRoomBinding;
import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.databinding.ItemContainerRoomBinding;
import edu.poly.nhtr.listeners.RoomListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.RoomPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomFragment extends Fragment implements RoomListener {

    private View view;
    private TextView nameTextView, addImageView;
    private ImageView profileImageView;

    private PreferenceManager preferenceManager;
    private FragmentRoomBinding binding;
    private ActivityMainRoomBinding binding1;
    private Dialog dialog;
    private RoomPresenter roomPresenter;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RoomFragment() {
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
    public static RoomFragment newInstance(String param1, String param2) {
        RoomFragment fragment = new RoomFragment();
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
        Home home = (Home) getArguments().getSerializable("home");
        String homeId = home.getIdHome();
        String nameHome = home.getNameHome();
        preferenceManager.putString(Constants.KEY_HOME_ID, homeId);
        preferenceManager.putString(Constants.KEY_NAME_HOME, nameHome);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
        roomPresenter = new RoomPresenter(this);
        binding = FragmentRoomBinding.inflate(getLayoutInflater());
        binding.rootLayoutRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.edtSearchRoom.clearFocus();
                binding.rootLayoutRoom.requestFocus();
            }
        });



        editFonts();

        //Set preference
        preferenceManager = new PreferenceManager(requireContext().getApplicationContext());

        // Set up RecyclerView layout manager
        binding.roomsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));

        // Load user's information
        loadUserDetails();

        // Load room information
        roomPresenter.getRooms("init");

        setListeners();

        // Xử lý Dialog Thêm phòng trọ
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding.btnAddRoom.setOnClickListener(view -> {
            openAddRoomDialog(Gravity.CENTER);
        });

        customizeLayoutSearch();
    }


    private void customizeLayoutSearch() {
        binding.layoutSearchRoom.setEndIconDrawable(R.drawable.ic_search_orange);
        binding.layoutSearchRoom.setEndIconVisible(true);
        binding.layoutSearchRoom.setHint("Tìm kiếm phòng ...");

        binding.edtSearchRoom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                {
                    binding.layoutSearchRoom.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });


        binding.edtSearchRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchNameHome = Objects.requireNonNull(binding.edtSearchRoom.getText()).toString().trim();
                roomPresenter.searchRoom(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.layoutSearchRoom.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchNameHome = Objects.requireNonNull(binding.edtSearchRoom.getText().toString().trim());
                roomPresenter.searchRoom(searchNameHome);
            }
        });
    }

    private void editFonts() {
        //Set three fonts into one textview
        Spannable text1 = new SpannableString("Bạn chưa có phòng\n Hãy nhấn nút ");
        Typeface interLightTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text1.setSpan(new TypefaceSpan(interLightTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.setText(text1);

        Spannable text2 = new SpannableString("+");
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        text2.setSpan(new TypefaceSpan(interBoldTypeface), 0, text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text2);

        Spannable text3 = new SpannableString(" để thêm phòng");
        Typeface interLightTypeface2 = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text3.setSpan(new TypefaceSpan(interLightTypeface2), 0, text3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text3);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_room, container, false);

        // Kiểm tra xem Bundle có tồn tại hay không
        if (getArguments() != null) {
            // Nhận dữ liệu từ Bundle
            Home home = (Home) getArguments().getSerializable("home");

            // Sử dụng dữ liệu 'home' như mong muốn
            // ...
        }

        return binding.getRoot();
    }

    private void setListeners(){
       loadUserDetails();
    }

    private void loadUserDetails() {
        binding1.nameHome.setText(preferenceManager.getString(Constants.KEY_NAME_HOME));
    }

    private void openAddRoomDialog(int gravity){
        setupDialog(R.layout.layout_dialog_add_room, Gravity.CENTER);
        TextView nameRoom= dialog.findViewById(R.id.txt_name_room);
        TextView title = dialog.findViewById(R.id.txt_title_dialog_room);
        TextView price = dialog.findViewById(R.id.txt_price);
        TextView describe = dialog.findViewById(R.id.txt_describe);

        EditText edtNameRoom =dialog.findViewById(R.id.edt_name_room);
        EditText edtPrice =dialog.findViewById(R.id.edt_price);
        EditText edtDescribe =dialog.findViewById(R.id.edt_describe);

        TextInputLayout layoutNameRoom = dialog.findViewById(R.id.layout_name_room);
        TextInputLayout layoutPrice = dialog.findViewById(R.id.layout_price);

        Button btnAddRoom = dialog.findViewById(R.id.btn_add_room);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Set dấu * đỏ cho TextView
        nameRoom.append(customizeText(" *"));
        price.append(customizeText(" *"));

        //Set thông tin cho dialog
        title.setText("Tạo mới nhà trọ");
        edtNameRoom.setHint("Ví dụ: Phòng 1");
        edtPrice.setHint("Ví dụ: 1.500.000");
        edtDescribe.setHint("Ví dụ: View Biển");


        edtNameRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edtNameRoom.getText().toString().trim();
                if (!name.isEmpty()) {
                    layoutNameRoom.setErrorEnabled(false);
                    layoutNameRoom.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edtPrice.getText().toString().trim();
                if (!name.isEmpty()) {
                    layoutPrice.setErrorEnabled(false);
                    layoutPrice.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Xử lý/ hiệu chỉnh màu nút button add room
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameRoom, edtPrice, btnAddRoom);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edtNameRoom.addTextChangedListener(textWatcher);
        edtPrice.addTextChangedListener(textWatcher);

        // Xử lý sự kiện cho button
        btnAddRoom.setOnClickListener(v -> {
            String name = edtNameRoom.getText().toString().trim();
            String priceRoom = edtPrice.getText().toString().trim();
            String describeRoom = edtDescribe.getText().toString().trim();
            Room room = new Room(name, priceRoom, describeRoom );
            roomPresenter.addRoom(room);

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());


    }

    // Cập nhật màu cho button
    private void updateButtonState(EditText edtNameRoom, EditText edtPrice, Button btn) {
        String name = edtNameRoom.getText().toString().trim();
        String address = edtPrice.getText().toString().trim();
        if (name.isEmpty() || address.isEmpty()) {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
        } else {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
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


    @Override
    public void onRoomClicked(Room room) {

    }

    @Override
    public void openPopup(View view, Room room, ItemContainerRoomBinding binding) {

    }


    @Override
    public void showErrorMessage(String message, int id) {
        TextInputLayout layout_name_room= dialog.findViewById(id);
        layout_name_room.setError(message);

    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getInfoHomeFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_HOME_ID);
    }

    @Override
    public void putRoomInfoInPreferences(String nameRoom, String priceRoom, String describeRoom, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_NAME_ROOM, documentReference.getId());
        preferenceManager.putString(Constants.KEY_PRICE, priceRoom);
        preferenceManager.putString(Constants.KEY_DESCRIBE, describeRoom);
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
    public void addRoom(List<Room> rooms, String action) {
        RoomAdapter roomAdapter = new RoomAdapter(rooms, this);
        binding.roomsRecyclerView.setAdapter(roomAdapter);

        // Sắp xếp các homes theo thứ tự từ thời gian khi theem vào
        rooms.sort(Comparator.comparing(obj -> obj.dateObject));

        if(Objects.equals(action,"init") || Objects.equals(action,"search"))
        {
            binding.roomsRecyclerView.smoothScrollToPosition(0);
        }
        else if(Objects.equals(action, "add"))
        {
            roomAdapter.addRoom(rooms);
            roomAdapter.notifyItemInserted(roomAdapter.getLastActionPosition());
            binding.roomsRecyclerView.smoothScrollToPosition(roomAdapter.getLastActionPosition());
        }
        //binding.homesRecyclerView.smoothScrollToPosition(0);

        // Do trong activity_users.xml, usersRecycleView đang được setVisibility là Gone, nên sau
        // khi setAdapter mình phải set lại là VISIBLE
        binding.txtNotification.setVisibility(View.GONE);
        binding.imgAddRoom.setVisibility(View.GONE);
        binding.roomsRecyclerView.setVisibility(View.VISIBLE);
        binding.frmMenuTools.setVisibility(View.VISIBLE);
        Log.d("RoomFragment", "Adapter set successfully");
    }

    @Override
    public void addRoomFailed() {
        binding.txtNotification.setVisibility(View.VISIBLE);
        binding.imgAddRoom.setVisibility(View.VISIBLE);
        binding.roomsRecyclerView.setVisibility(View.INVISIBLE);
        binding.frmMenuTools.setVisibility(View.VISIBLE);
    }

    public boolean isAdded2() {
        return isAdded();
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
    public void openConfirmUpdateRoom(int gravity, String newNameRoom, String newPrice, String newDescribe, Room room) {
        setupDialog(R.layout.layout_dialog_confirm_update_room, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_confirm_update_home = dialog.findViewById(R.id.btn_confirm_update_room);

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
                roomPresenter.updateSuccess(newNameRoom, newPrice, newDescribe, room);
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