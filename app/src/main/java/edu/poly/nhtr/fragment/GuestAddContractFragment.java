package edu.poly.nhtr.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.hbb20.CountryCodePicker;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestAddContractBinding;
import edu.poly.nhtr.listeners.MainGuestListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.GuestAddContractPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class GuestAddContractFragment extends Fragment implements MainGuestListener {

    private static final int IMAGE_SELECTION_CCCD_FRONT = 1;
    private static final int IMAGE_SELECTION_CCCD_BACK = 2;
    private static final int IMAGE_SELECTION_CONTRACT_FRONT = 3;
    private static final int IMAGE_SELECTION_CONTRACT_BACK = 4;
    public String encodedCCCDFrontImage;
    public String encodedCCCDBackImage;
    public String encodedContractFrontImage;
    public String encodedContractBackImage;
    private PreferenceManager preferenceManager;
    private FragmentGuestAddContractBinding binding;
    private TextInputEditText edtHoTen;
    private TextInputLayout tilHoTen;
    private TextInputEditText edtSoDienThoai;
    private TextInputLayout tilSoDienThoai;
    private CountryCodePicker countryCodePicker;
    private TextInputEditText edtSoCCCD;
    private TextInputLayout tilSoCCCD;
    private TextInputEditText edtTienPhong;
    private EditText edtHanThanhToan;
    private TextInputLayout tilNgaySinh;
    private TextInputEditText edtNgaySinh;
    private AutoCompleteTextView edtGioiTinh;
    private AutoCompleteTextView edtTotalMembers;
    private TextInputLayout tilNgayTao;
    private TextInputLayout tilNgayHetHan;
    private TextInputLayout tilNgayTraTien;
    private TextInputEditText edtNgayTao;
    private TextInputEditText edtNgayHetHan;
    private AutoCompleteTextView edtNgayTraTien;
    private GuestAddContractPresenter presenter;
    private ImageButton imgButtonLichNgaySinh;
    private ImageButton imgButtonLichNgayTao;
    private ImageButton imgButtonLichNgayHetHan;
    private ImageButton imgButtonLichNgayTraTien;
    private RoundedImageView imgCCCDFront;
    private RoundedImageView imgCCCDBack;
    private ImageView imgAddCCCDFront;
    private ImageView imgAddCCCDBack;
    private RoundedImageView imgContractFront;
    private RoundedImageView imgContractBack;
    private ImageView imgAddContractFront;
    private ImageView imgAddContractBack;
    private int currentImageSelection;
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
    private AppCompatButton btnAddContract;
    private AppCompatButton btnCancel;
    private Dialog dialog;
    private Room room;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        if (getArguments() != null) {
            room = (Room) getArguments().getSerializable("room");
            String roomId = Objects.requireNonNull(room).getRoomId();
            preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
        }
    }

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
        dialog = new Dialog(requireActivity());

        initializeViews();
        setListeners();

        if (room != null) {
            Toast.makeText(requireContext(), "Room ID: " + room.getRoomId(), Toast.LENGTH_SHORT).show();
            // Use room object to set up views or perform other operations
        } else {
            room = getRoomFromPreference();
            if (room != null) {
                Toast.makeText(requireContext(), "Room ID from preferences: " + room.getRoomId(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No room data available", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private Room getRoomFromPreference() {
        String roomId = preferenceManager.getString(Constants.KEY_ROOM_ID);
        // Example: Retrieve other fields if needed
        // String roomName = preferenceManager.getString(Constants.KEY_ROOM_NAME);

        if (roomId != null && !roomId.isEmpty()) {
            Room room = new Room();
            room.setRoomId(roomId);
            // Set other fields if needed
            return room;
        } else {
            return null;
        }
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
        countryCodePicker = binding.ccp;
        tilSoDienThoai = binding.tilSoDienThoai;
        edtSoCCCD = binding.edtSoCccdCmnd;
        tilNgaySinh = binding.tilNgaySinh;
        tilSoCCCD = binding.tilSoCccdCmnd;
        edtNgaySinh = binding.edtNgaySinh;
        edtGioiTinh = binding.edtGioiTinh;
        edtTotalMembers = binding.edtTotalMembers;
        tilNgayTao = binding.tilNgayTaoHopDong;
        tilNgayHetHan = binding.tilNgayHetHanHopDong;
        tilNgayTraTien = binding.tilNgayTraTienPhong;
        edtNgayTao = binding.edtNgayTaoHopDong;
        edtTienPhong = binding.edtGiaPhong;
        edtNgayHetHan = binding.edtNgayHetHanHopDong;
        edtNgayTraTien = binding.edtNgayTraTienPhong;
        edtHanThanhToan = binding.edtHanThanhToan;
        imgAddCCCDFront = binding.imgAddCccdFront;
        imgAddCCCDBack = binding.imgAddCccdBack;
        imgCCCDFront = binding.imgCccdFront;
        imgCCCDBack = binding.imgCccdBack;
        imgAddContractFront = binding.imgAddContractFront;
        imgAddContractBack = binding.imgAddContractBack;
        imgContractFront = binding.imgContractFront;
        imgContractBack = binding.imgContractBack;
        imgButtonLichNgaySinh = binding.imgButtonCalendarNgaySinh;
        imgButtonLichNgayTao = binding.imgButtonCalendarTao;
        imgButtonLichNgayHetHan = binding.imgButtonCalendarHetHan;
        btnAddContract = binding.btnAddContract;
        btnCancel = binding.btnCancel;
    }

    private void setListeners() {
        presenter.setUpDropDownMenuGender();
        presenter.setUpDropDownMenuTotalMembers();
        presenter.setUpDropDownMenuDays();
        presenter.setUpDateOfBirthField(tilNgaySinh, edtNgaySinh, imgButtonLichNgaySinh, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayTao, edtNgayTao, imgButtonLichNgayTao, getString(R.string.dd_mm_yyyy));
        presenter.setUpDateField(tilNgayHetHan, edtNgayHetHan, imgButtonLichNgayHetHan, getString(R.string.dd_mm_yyyy));

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

        checkName();
        checkPhoneNumber();
        checkCCCDNumber();

        btnAddContract.setOnClickListener(v -> {
            openSaveDialog();
        });

        btnCancel.setOnClickListener(v -> {
            openCancelSaveDialog();
        });
    }

    @Override
    public void showToast(String message) {

    }

    @Override
    public void showLoading() {

    }


    @Override
    public String getInfoRoomFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_ROOM_ID);
    }

    @Override
    public void getListMainGuest(List<MainGuest> listContracts) {

    }

    @Override
    public void putContractInfoInPreferences(String nameGuest, String phoneGuest, String cccdNumber, String dateOfBirth, String gender, int totalMembers, String createDate, double roomPrice, String expirationDate, String payDate, int daysUntilDueDate, String cccdImageFront, String cccdImageBack, String contractImageFront, String contractImageBack, boolean status, String roomId, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_GUEST_NAME, documentReference.getId());
        preferenceManager.putString(Constants.KEY_GUEST_PHONE, phoneGuest);
        preferenceManager.putString(Constants.KEY_GUEST_CCCD, cccdNumber);
        preferenceManager.putString(Constants.KEY_GUEST_DATE_OF_BIRTH, dateOfBirth);
        preferenceManager.putString(Constants.KEY_GUEST_GENDER, gender);
        preferenceManager.putString(Constants.KEY_ROOM_TOTAl_MEMBERS, totalMembers + "");
        preferenceManager.putString(Constants.KEY_CONTRACT_CREATED_DATE, createDate);
        preferenceManager.putString(Constants.KEY_CONTRACT_ROOM_PRICE, roomPrice + "");
        preferenceManager.putString(Constants.KEY_CONTRACT_EXPIRATION_DATE, expirationDate);
        preferenceManager.putString(Constants.KEY_CONTRACT_PAY_DATE, payDate);
        preferenceManager.putString(Constants.KEY_CONTRACT_DAYS_UNTIL_DUE_DATE, daysUntilDueDate + "");
        preferenceManager.putString(Constants.KEY_GUEST_CCCD_IMAGE_FRONT, cccdImageFront);
        preferenceManager.putString(Constants.KEY_GUEST_CCCD_IMAGE_BACK, cccdImageBack);
        preferenceManager.putString(Constants.KEY_GUEST_CONTRACT_IMAGE_FRONT, contractImageFront);
        preferenceManager.putString(Constants.KEY_GUEST_CONTRACT_IMAGE_BACK, contractImageBack);
        preferenceManager.putString(Constants.KEY_CONTRACT_STATUS, status + "");
        preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
    }


    @Override
    public void onMainGuestsLoaded(List<MainGuest> mainGuests, String action) {

    }

    @Override
    public void onMainGuestsLoadFailed() {

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
    public void setUpDropDownMenuDays() {
        edtNgayTraTien.setAdapter(presenter.getDaysAdapter());
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

    public void checkName() {
        presenter.setUpNameField(edtHoTen, tilHoTen);
    }

    public void checkPhoneNumber() {
        presenter.setUpPhoneNumberField(edtSoDienThoai, tilSoDienThoai, countryCodePicker);
    }


    public void checkCCCDNumber() {
        presenter.setUpCCCDField(edtSoCCCD, tilSoCCCD);
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

    private String getStringFromEditText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private String getStringFromEditText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private String getStringFromAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView) {
        if (autoCompleteTextView == null || autoCompleteTextView.getText() == null) {
            return "";
        }
        return autoCompleteTextView.getText().toString().trim();
    }

    public boolean isAdded2() {
        return isAdded();
    }

    private void openSaveDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_confirm_save_contract);

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel_save_contract);
        MaterialButton btnSave = dialog.findViewById(R.id.btn_confirm_save_contract);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        btnSave.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(() -> {
                boolean saveContractSuccessfully = saveContract();

                progressBar.setVisibility(View.INVISIBLE);

                if (saveContractSuccessfully) {
                    dialog.dismiss();
                    RoomGuestFragment roomGuestFragment = new RoomGuestFragment();
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, roomGuestFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Lưu hợp đồng thất bại", Toast.LENGTH_SHORT).show();
                }
            }, 2000);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public boolean saveContract() {
        String nameGuest = getStringFromEditText(edtHoTen);
        String phoneGuest = getStringFromEditText(edtSoDienThoai);
        String cccdNumber = getStringFromEditText(edtSoCCCD);
        String dateOfBirth = getStringFromEditText(edtNgaySinh);
        String gender = getStringFromAutoCompleteTextView(edtGioiTinh);
        String totalMembers = getStringFromAutoCompleteTextView(edtTotalMembers);
        String createDate = getStringFromEditText(edtNgayTao);
        String expirationDate = getStringFromEditText(edtNgayHetHan);
        String payDate = getStringFromEditText(edtNgayTraTien);
        String roomPrice = getStringFromEditText(edtTienPhong);
        String daysUntilDueDateStr = getStringFromEditText(edtHanThanhToan);

        // Debug logs to print out values
        System.out.println("Name Guest: " + nameGuest);
        System.out.println("Phone Guest: " + phoneGuest);
        System.out.println("CCCD Number: " + cccdNumber);
        System.out.println("Date of Birth: " + dateOfBirth);
        System.out.println("Gender: " + gender);
        System.out.println("Total Members: " + totalMembers);
        System.out.println("Create Date: " + createDate);
        System.out.println("Expiration Date: " + expirationDate);
        System.out.println("Pay Date: " + payDate);
        System.out.println("Room Price: " + roomPrice);
        System.out.println("Days Until Due Date: " + daysUntilDueDateStr);

        // Check if any required fields are empty and handle the error
        if (nameGuest.isEmpty() || phoneGuest.isEmpty() || cccdNumber.isEmpty() || dateOfBirth.isEmpty() ||
                gender.isEmpty() || totalMembers.isEmpty() || createDate.isEmpty() || expirationDate.isEmpty() ||
                payDate.isEmpty() || roomPrice.isEmpty() || daysUntilDueDateStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        int daysUntilDueDate;
        try {
            daysUntilDueDate = Integer.parseInt(daysUntilDueDateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format for days until due date", Toast.LENGTH_SHORT).show();
            return false;
        }

        MainGuest mainGuest = new MainGuest();
        mainGuest.setNameGuest(nameGuest);
        mainGuest.setPhoneGuest(phoneGuest);
        mainGuest.setCccdNumber(cccdNumber);
        mainGuest.setDateOfBirth(dateOfBirth);
        mainGuest.setGender(gender);
        mainGuest.setTotalMembers(Integer.parseInt(totalMembers));
        mainGuest.setCreateDate(createDate);
        mainGuest.setExpirationDate(expirationDate);
        mainGuest.setPayDate(payDate);
        mainGuest.setRoomPrice(Double.parseDouble(roomPrice));
        mainGuest.setDaysUntilDueDate(daysUntilDueDate);
        mainGuest.setCccdImageFront(encodedCCCDFrontImage);
        mainGuest.setCccdImageBack(encodedCCCDBackImage);
        mainGuest.setContractImageFront(encodedContractFrontImage);
        mainGuest.setContractImageBack(encodedContractBackImage);
        mainGuest.setFileStatus(true);

        presenter.addContractToFirestore(mainGuest);
        return true;
    }

    private void openCancelSaveDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_confirm_cancel_save_contract);

        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        MaterialButton btnNo = dialog.findViewById(R.id.btn_no_delete_contract);
        MaterialButton btnYes = dialog.findViewById(R.id.btn_yes_delete_contract);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        btnNo.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            btnYes.setVisibility(View.INVISIBLE);
            new Handler().postDelayed(() -> {
                clearInputFields();
                dialog.dismiss();
                progressBar.setVisibility(View.INVISIBLE);

                requireActivity().getSupportFragmentManager().popBackStack();
            }, 1000);
        });

        dialog.show();
    }

    private void clearInputFields() {
        edtHoTen.setText("");
        edtSoDienThoai.setText("");
        edtSoCCCD.setText("");
        edtNgaySinh.setText("");
        edtGioiTinh.setText("");
        edtTotalMembers.setText("");
        edtNgayTao.setText("");
        edtTienPhong.setText("");
        edtNgayHetHan.setText("");
        edtNgayTraTien.setText("");
        edtHanThanhToan.setText("");

        imgCCCDFront.setImageDrawable(null);
        imgCCCDBack.setImageDrawable(null);
        imgContractFront.setImageDrawable(null);
        imgContractBack.setImageDrawable(null);

        imgAddCCCDFront.setVisibility(View.VISIBLE);
        imgAddCCCDBack.setVisibility(View.VISIBLE);
        imgAddContractFront.setVisibility(View.VISIBLE);
        imgAddContractBack.setVisibility(View.VISIBLE);

        encodedCCCDFrontImage = null;
        encodedCCCDBackImage = null;
        encodedContractFrontImage = null;
        encodedContractBackImage = null;
    }


}
