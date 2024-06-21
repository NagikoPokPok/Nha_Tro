package edu.poly.nhtr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestAddContractBinding;
import edu.poly.nhtr.interfaces.GuestAddContractInterface;
import edu.poly.nhtr.presenters.GuestAddContractPresenter;

public class GuestAddContractFragment extends Fragment implements GuestAddContractInterface {

    private FragmentGuestAddContractBinding binding;
    private TextInputLayout tilNgaySinh;
    private TextInputEditText edtNgaySinh;
    private AutoCompleteTextView edtGioiTinh;
    private AutoCompleteTextView edtTotalMembers;
    private TextInputLayout tilNgayTao;
    private TextInputLayout tilNgayHetHan;
    private TextInputLayout tilNgayTraTien;
    private TextInputEditText edtNgayTao;
    private TextInputEditText edtNgayHetHan;
    private TextInputEditText edtNgayTraTien;
    private GuestAddContractPresenter presenter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGuestAddContractBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter = new GuestAddContractPresenter(this, requireContext());
        presenter.initializeViews();
        presenter.setUpDropDownMenuGender();
        presenter.setUpDropDownMenuTotalMembers();
        presenter.setUpDateField(tilNgaySinh, edtNgaySinh, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayTao, edtNgayTao, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayHetHan, edtNgayHetHan, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayTraTien, edtNgayTraTien, getString(R.string.dd_mm_yyyy));
    }

    @Override
    public void initializeViews() {
        tilNgaySinh = binding.tilNgaySinh;
        edtNgaySinh = binding.edtNgaySinh;
        edtGioiTinh = binding.edtGioiTinh;
        edtTotalMembers = binding.edtTotalMembers;
        tilNgayTao = binding.tilNgayTao;
        tilNgayHetHan = binding.tilNgayHetHanHopDong;
        tilNgayTraTien = binding.tilNgayTraTienPhong;
        edtNgayTao = binding.edtNgayTao;
        edtNgayHetHan = binding.edtNgayHetHanHopDong;
        edtNgayTraTien = binding.edtNgayTraTienPhong;
    }

    @Override
    public void setUpDropDownMenuGender() {
        edtGioiTinh.setAdapter(presenter.getGenderAdapter());
    }

    @Override
    public void setUpDropDownMenuTotalMembers() {
        edtTotalMembers.setAdapter(presenter.getTotalMembersAdapter());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
