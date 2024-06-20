package edu.poly.nhtr.fragment;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestAddContractBinding;

public class GuestAddContractFragment extends Fragment {

    private FragmentGuestAddContractBinding binding;
    private TextInputLayout tilNgaySinh;
    private TextInputEditText edtNgaySinh;
    private AutoCompleteTextView edtGioiTinh;
    private AutoCompleteTextView edtTotalMembers;
    ArrayAdapter<String> adapterItems;
    private static final int REQUIRED_DATE_LENGTH = 8;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGuestAddContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
        setUpDropDownMenuGender();
        setUpDropDownMenuTotalMembers();
        setUpDateField(tilNgaySinh, edtNgaySinh, getString(R.string.dd_mm_yyyy));
    }

    private void initializeViews() {
        tilNgaySinh = binding.tilNgaySinh;
        edtNgaySinh = binding.edtNgaySinh;
        edtGioiTinh = binding.edtGioiTinh;
        edtTotalMembers = binding.edtTotalMembers;
    }

    // Hàm xử lý cho các trường thông tin sử dùng ngày và lịch
    private void setUpDateField(TextInputLayout textInputLayout, TextInputEditText textInputEditText, String hint) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        textInputEditText.setOnClickListener(v -> {
            // Tạo DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, monthOfYear, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
                textInputEditText.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        textInputEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                textInputLayout.setHint("");
            } else {
                if (Objects.requireNonNull(textInputEditText.getText()).toString().isEmpty()) {
                    textInputLayout.setHint(hint);
                }
            }
        });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("\\D", "");
                    String cleanCurr = current.replaceAll("\\D", "");

                    int c1 = clean.length();
                    int sel = c1; // Selection position (Vị trí được chọn)

                    // Chiều dài tối đa cho phần định dạng ngày mà không có dấu gạch chéo
                    final int MAX_DAY_MONTH_FORMAT_LENGTH = 6;

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i+=2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;


                    if (clean.length() < REQUIRED_DATE_LENGTH) {
                        String ddmmyyyy = "DDMMYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        year = (year < 1900) ? 1900 : Math.min(year, 2100);
                        cal.set(Calendar.YEAR, year);

                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format(Locale.getDefault(), "%02d%02d%02d", day, month, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    textInputEditText.setText(current);
                    textInputEditText.setSelection(Math.min(sel, current.length()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    textInputLayout.setHint("");
                } else {
                    textInputLayout.setHint(hint);
                }
            }
        });
    }

    // Hàm xử lý menu dropdown của giới tính
    private void setUpDropDownMenuGender() {
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_layout, genderOptions);
        edtGioiTinh.setAdapter(adapterItems);


//        // Xử lý sự kiện khi chọn giới tính
//        edtGioiTinh.setOnItemClickListener((parent, view, position, id) -> {
//            String selectedGender = (String) parent.getItemAtPosition(position);
//
//        });
    }

    // Hàm xử lý menu dropdown của tổng thành viên
    private void setUpDropDownMenuTotalMembers() {
        String[] totalMembersOptions = getResources().getStringArray(R.array.numbers);
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_layout, totalMembersOptions);
        edtTotalMembers.setAdapter(adapterItems);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
