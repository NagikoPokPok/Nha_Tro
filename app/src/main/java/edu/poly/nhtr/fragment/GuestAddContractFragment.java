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
        private TextInputEditText edtSoDienThoai;
        private TextInputEditText edtSoCCCD;
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

        // Hàm truy cập thư viện để lấy ảnh
        public final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {
                                InputStream inputStream = requireContext().getContentResolver().openInputStream(Objects.requireNonNull(imageUri));
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                                // Determine which image view was clicked and update accordingly
                                if (imgAddCCCDFront.getVisibility() == View.VISIBLE) {
                                    imgCCCDFront.setImageBitmap(bitmap);
                                    imgAddCCCDFront.setVisibility(View.GONE);
                                    encodedCCCDFrontImage = encodedCCCDImage(bitmap);
                                } else if (imgAddCCCDBack.getVisibility() == View.VISIBLE) {
                                    imgCCCDBack.setImageBitmap(bitmap);
                                    imgAddCCCDBack.setVisibility(View.GONE);
                                    encodedCCCDBackImage = encodedCCCDImage(bitmap);
                                } else if (imgAddContractFront.getVisibility() == View.VISIBLE) {
                                    imgContractFront.setImageBitmap(bitmap);
                                    imgAddContractFront.setVisibility(View.GONE);
                                    encodedContractFrontImage = encodedContractImage(bitmap);
                                } else if (imgAddContractBack.getVisibility() == View.VISIBLE) {
                                    imgContractBack.setImageBitmap(bitmap);
                                    imgAddContractBack.setVisibility(View.GONE);
                                    encodedContractBackImage = encodedContractImage(bitmap);
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

            imgAddCCCDFront.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
            imgAddCCCDBack.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
            imgCCCDFront.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
            imgCCCDBack.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));

            imgAddContractFront.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
            imgAddContractBack.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
            imgContractFront.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
            imgContractBack.setOnClickListener(v -> pickImage.launch(presenter.prepareImageSelection()));
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
        public void setCCCDImage(Uri image, int requestCode) {
            if (requestCode == GuestAddContractPresenter.PICK_IMAGE_FRONT) {
                imgCCCDFront.setImageURI(image);
                imgAddCCCDFront.setVisibility(View.GONE);
            } else if (requestCode == GuestAddContractPresenter.PICK_IMAGE_BACK) {
                imgCCCDBack.setImageURI(image);
                imgAddCCCDBack.setVisibility(View.GONE);
            }
        }

        @Override
        public void setContractImage(Uri image, int requestCode) {
            if (requestCode == GuestAddContractPresenter.PICK_IMAGE_FRONT) {
                imgContractFront.setImageURI(image);
                imgAddContractFront.setVisibility(View.GONE);
            } else if (requestCode == GuestAddContractPresenter.PICK_IMAGE_BACK) {
                imgContractBack.setImageURI(image);
                imgAddContractBack.setVisibility(View.GONE);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            presenter.handleCCCDImageSelection(requestCode, resultCode, data);
            presenter.handleContractImageSelection(requestCode, resultCode, data);
        }


        private String encodedCCCDImage(Bitmap bitmap) // Hàm mã hoá ảnh thành chuỗi Base64
        {
            int previewWidth = 250;
            int previewHeight = bitmap.getHeight() + previewWidth / bitmap.getWidth();
            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        private String encodedContractImage(Bitmap bitmap) // Hàm mã hoá ảnh thành chuỗi Base64
        {
            int previewHeight = 300;
            int previewWidth = bitmap.getWidth() + previewHeight / bitmap.getHeight();
            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }

    }
