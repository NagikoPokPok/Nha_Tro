package edu.poly.nhtr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestAddContractBinding;

public class GuestAddContractFragment extends Fragment {

    private FragmentGuestAddContractBinding binding;
    private TextInputLayout tilNgaySinh;
    private TextInputEditText edtNgaySinh;
    private AutoCompleteTextView edtGioiTinh;
    ArrayAdapter<String> adapterItems;

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
        setUpNgaySinhField();
        setUpDropDownMenuGender();
    }

    private void initializeViews() {
        tilNgaySinh = binding.tilNgaySinh;
        edtNgaySinh = binding.edtNgaySinh;
        edtGioiTinh = binding.edtGioiTinh;
    }

    private void setUpNgaySinhField() {
        edtNgaySinh.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tilNgaySinh.setHint("");
            } else {
                tilNgaySinh.setHint(getString(R.string.dd_mm_yyyy));
            }
        });
    }

    // Hàm xử lý menu dropdown của giới tính
    private void setUpDropDownMenuGender() {
        String[] genderOptions = getResources().getStringArray(R.array.gender_options);
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.custom_dropdown_item, genderOptions);
        edtGioiTinh.setAdapter(adapterItems);



//        // Xử lý sự kiện khi chọn giới tính
//        edtGioiTinh.setOnItemClickListener((parent, view, position, id) -> {
//            String selectedGender = (String) parent.getItemAtPosition(position);
//
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
