package edu.poly.nhtr.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestAddContractBinding;
import edu.poly.nhtr.interfaces.GuestAddContractInterface;
import edu.poly.nhtr.presenters.GuestAddContractPresenter;

public class GuestAddContractFragment extends Fragment implements GuestAddContractInterface {

    private FragmentGuestAddContractBinding binding;
    private TextInputEditText edtHoTen;
    private TextInputLayout tilHoTen;
    private TextInputEditText edtSoDienThoai;
    private TextInputLayout tilSoDienThoai;
    private TextInputEditText edtSoCCCD;
    private TextInputLayout tilSoCCCD;
    private TextInputEditText edtTienPhong;
    private TextInputEditText edtHanThanhToan;
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

    public String encodedCCCDFrontImage;
    public String encodedCCCDBackImage;
    private RoundedImageView imgCCCDFront;
    private RoundedImageView imgCCCDBack;
    private ImageView imgAddCCCDFront;
    private ImageView imgAddCCCDBack;

    public String encodedContractFrontImage;
    public String encodedContractBackImage;
    private RoundedImageView imgContractFront;
    private RoundedImageView imgContractBack;
    private ImageView imgAddContractFront;
    private ImageView imgAddContractBack;

    private int currentImageSelection;
    private static final int IMAGE_SELECTION_NONE = 0;
    private static final int IMAGE_SELECTION_CCCD_FRONT = 1;
    private static final int IMAGE_SELECTION_CCCD_BACK = 2;
    private static final int IMAGE_SELECTION_CONTRACT_FRONT = 3;
    private static final int IMAGE_SELECTION_CONTRACT_BACK = 4;

    // Hàm truy cập thư viện để lấy ảnh
    public final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                            if (bitmap != null) {
                                switch (currentImageSelection) {
                                    case IMAGE_SELECTION_CCCD_FRONT:
                                        imgCCCDFront.setImageBitmap(bitmap);
                                        imgAddCCCDFront.setVisibility(View.GONE);
                                        encodedCCCDFrontImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CCCD_BACK:
                                        imgCCCDBack.setImageBitmap(bitmap);
                                        imgAddCCCDBack.setVisibility(View.GONE);
                                        encodedCCCDBackImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CONTRACT_FRONT:
                                        imgContractFront.setImageBitmap(bitmap);
                                        imgAddContractFront.setVisibility(View.GONE);
                                        encodedContractFrontImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CONTRACT_BACK:
                                        imgContractBack.setImageBitmap(bitmap);
                                        imgAddContractBack.setVisibility(View.GONE);
                                        encodedContractBackImage = encodeImage(bitmap);
                                        break;
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

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
        initializeViews();
        setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void initializeViews() {
        edtHoTen = binding.edtTenKhach;
        tilHoTen = binding.tilTenKhach;
        edtSoDienThoai = binding.edtSoDienThoai;
        edtSoCCCD = binding.edtSoCccdCmnd;
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
        imgAddCCCDFront = binding.imgAddCccdFront;
        imgAddCCCDBack = binding.imgAddCccdBack;
        imgCCCDFront = binding.imgCccdFront;
        imgCCCDBack = binding.imgCccdBack;
        imgAddContractFront = binding.imgAddContractFront;
        imgAddContractBack = binding.imgAddContractBack;
        imgContractFront = binding.imgContractFront;
        imgContractBack = binding.imgContractBack;
    }

    private void setListeners() {
        presenter.setUpDropDownMenuGender();
        presenter.setUpDropDownMenuTotalMembers();
        presenter.setUpDateField(tilNgaySinh, edtNgaySinh, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayTao, edtNgayTao, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayHetHan, edtNgayHetHan, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayTraTien, edtNgayTraTien, getString(R.string.dd_mm_yyyy));

        imgAddCCCDFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        imgAddCCCDBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
        imgCCCDFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        imgCCCDBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });

        imgAddContractFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        imgAddContractBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
        imgContractFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        imgContractBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
    }

    @Override
    public void setUpDropDownMenuGender() {
        edtGioiTinh.setAdapter(presenter.getGenderAdapter());
    }

    @Override
    public void setUpDropDownMenuTotalMembers() {
        edtTotalMembers.setAdapter(presenter.getTotalMembersAdapter());
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 250;
        int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(requireContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setNameErrorMessage(String message) {
        tilHoTen.setError(message);
    }

    @Override
    public void setPhoneErrorMessage(String message) {
        tilSoDienThoai.setError(message);
    }

    @Override
    public void setCCCDNumberErrorMessage(String message) {
        tilSoCCCD.setError(message);
    }

    @Override
    public void setNameErrorEnabled(Boolean isEmpty) {
        tilHoTen.setErrorEnabled(isEmpty);
    }

    @Override
    public void setPhoneNumberlErrorEnabled(Boolean isEmpty) {
        tilSoDienThoai.setErrorEnabled(isEmpty);
    }

    @Override
    public void setCCCDNumberlErrorEnabled(Boolean isEmpty) {
        tilSoCCCD.setErrorEnabled(isEmpty);
    }
}
