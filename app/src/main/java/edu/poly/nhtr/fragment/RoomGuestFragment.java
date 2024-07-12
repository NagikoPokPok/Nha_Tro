package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Adapter.GuestAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.presenters.RoomGuestPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import timber.log.Timber;

public class RoomGuestFragment extends Fragment implements RoomGuestInterface.View {

    private FragmentRoomGuestBinding binding;
    private RecyclerView recyclerView;
    private GuestAdapter adapter;
    private PreferenceManager preferenceManager;
    private Dialog dialog;
    private RoomGuestInterface.Presenter presenter;
    private RoomViewModel roomViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomGuestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomViewModel = new ViewModelProvider(requireActivity()).get(RoomViewModel.class);
        presenter = new RoomGuestPresenter(this, roomViewModel);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        recyclerView = binding.guestsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo adapter và gán cho RecyclerView
        adapter = new GuestAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Lấy danh sách khách từ ViewModel và cập nhật RecyclerView
        roomViewModel.getGuests().observe(getViewLifecycleOwner(), guests -> {
            if (guests != null && !guests.isEmpty()) {
                List<Object> items = new ArrayList<>(guests);
                adapter.setGuestList(items);
                binding.progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                showNoDataFound();
            }
        });


        // Lấy ra Room ID từ ViewModel và lấy danh sách khách theo Room ID
        roomViewModel.getRoom().observe(getViewLifecycleOwner(), room -> {
            if (room != null) {
                String roomId = room.getRoomId();
                preferenceManager.putString(Constants.PREF_KEY_ROOM_ID, roomId);
                Timber.tag("RoomGuestFragment").d("Room ID: %s", roomId);
                presenter.getGuests(roomId);
            } else {
                Timber.tag("RoomGuestFragment").e("Room object is null");
                showError("Room data is not available");
            }
        });

        setListeners();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showNoDataFound() {
        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public String getInfoRoomFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_ROOM_ID);
    }

    @Override
    public void putGuestInfoInPreferences(String nameGuest, String phoneGuest, String dateIn, boolean status, String roomId, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_GUEST_NAME, documentReference.getId());
        preferenceManager.putString(Constants.KEY_GUEST_PHONE, phoneGuest);
        preferenceManager.putString(Constants.KEY_GUEST_DATE_IN, dateIn);
        preferenceManager.putString(Constants.KEY_CONTRACT_STATUS, status + "");
        preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setListeners() {
        binding.btnAddGuest.setOnClickListener(v -> openAddGuestDialog());
    }

    // Hàm set mau va font chu cho Text
    private Spannable customizeText(String s)
    {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }


    private void openAddGuestDialog() {
        setUpDialog(R.layout.layout_dialog_add_guest);
        TextView nameGuest = dialog.findViewById(R.id.txt_name_guest);
        TextView phoneGuest = dialog.findViewById(R.id.txt_phone_number);
        TextView dateInGuest = dialog.findViewById(R.id.txt_date_in);

        EditText edtNameGuest = dialog.findViewById(R.id.edt_name_guest);
        EditText edtPhoneGuest = dialog.findViewById(R.id.edt_phone_number);
        EditText edtDateIn = dialog.findViewById(R.id.edt_date_in);

        TextInputLayout nameGuestLayout = dialog.findViewById(R.id.layout_name_guest);
        TextInputLayout phoneGuestLayout = dialog.findViewById(R.id.layout_phone_number);
        TextInputLayout dateInLayout = dialog.findViewById(R.id.layout_date_in);

        Button btnAddGuest = dialog.findViewById(R.id.btn_add_new_guest);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Thêm dấu * đỏ cho TextView cần
        nameGuest.append(customizeText("*"));

        // Set hint cho các trường và nút
        edtNameGuest.setHint("Ví dụ: Nguyễn Văn A");
        edtPhoneGuest.setHint("Ví dụ: 0123456789");
        edtDateIn.setHint("Ví dụ: 01/01/2022");
        btnAddGuest.setText("Thêm khách");

        // Handle input field changes
        edtNameGuest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edtNameGuest.getText().toString().trim();
                if (!name.isEmpty()) {
                    nameGuestLayout.setErrorEnabled(false);
                    nameGuestLayout.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Handle add guest button state
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameGuest, btnAddGuest);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        };

        // Add textWatcher to input fields
        edtNameGuest.addTextChangedListener(textWatcher);

        // Handle add guest button click
        btnAddGuest.setOnClickListener(v -> {
            String name = edtNameGuest.getText().toString().trim();
            String phone = edtPhoneGuest.getText().toString().trim();
            String dateIn = edtDateIn.getText().toString().trim();
            Guest guest = new Guest(name, phone, false, dateIn);
            presenter.addGuestToFirebase(guest);
            dialog.dismiss();

            String roomId = preferenceManager.getString(Constants.PREF_KEY_ROOM_ID);
            presenter.getGuests(roomId);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void setUpDialog(int layoutId) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    // Update button color based on input field state
    private void updateButtonState(EditText edtNameGuest, Button btnAdd) {
        String name = edtNameGuest.getText().toString().trim();
        if (name.isEmpty()) {
            btnAdd.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_clicked, null));
        } else {
            btnAdd.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_add, null));
        }
    }
}
