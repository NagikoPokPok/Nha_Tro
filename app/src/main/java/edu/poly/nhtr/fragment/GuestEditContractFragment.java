package edu.poly.nhtr.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Objects;

import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentGuestEditContractBinding;
import edu.poly.nhtr.listeners.GuestEditContractListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.GuestEditContractPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class GuestEditContractFragment extends Fragment implements GuestEditContractListener {

    private FragmentGuestEditContractBinding binding;
    private RoomContractFragment.OnFragmentInteractionListener mListener;
    private MainGuest guestContract;
    private GuestEditContractPresenter presenter;
    private PreferenceManager preferenceManager;
    private String roomId;
    private CountryCodePicker countryCodePicker;
    private static final int IMAGE_SELECTION_CCCD_FRONT = 1;
    private static final int IMAGE_SELECTION_CCCD_BACK = 2;
    private static final int IMAGE_SELECTION_CONTRACT_FRONT = 3;
    private static final int IMAGE_SELECTION_CONTRACT_BACK = 4;
    public String encodedCCCDFrontImage;
    public String encodedCCCDBackImage;
    public String encodedContractFrontImage;
    public String encodedContractBackImage;
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
                                        binding.imgCccdFront.setImageBitmap(bitmap);
                                        binding.imgAddCccdFront.setVisibility(View.GONE);
                                        encodedCCCDFrontImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CCCD_BACK:
                                        binding.imgCccdBack.setImageBitmap(bitmap);
                                        binding.imgAddCccdBack.setVisibility(View.GONE);
                                        encodedCCCDBackImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CONTRACT_FRONT:
                                        binding.imgContractFront.setImageBitmap(bitmap);
                                        binding.imgAddContractFront.setVisibility(View.GONE);
                                        encodedContractFrontImage = encodeImage(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CONTRACT_BACK:
                                        binding.imgContractBack.setImageBitmap(bitmap);
                                        binding.imgAddContractBack.setVisibility(View.GONE);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentGuestEditContractBinding.inflate(getLayoutInflater());
        presenter = new GuestEditContractPresenter(this, getContext());
        countryCodePicker = binding.ccp;
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        Bundle arguments = getArguments();
        if (arguments != null) {
            Room room = (Room) arguments.getSerializable("room");
            if (room != null) {
                roomId = room.getRoomId();
                presenter.setGuestContract(roomId);
            } else {
                showToast("Invalid room ID");
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof RoomContractFragment.OnFragmentInteractionListener) {
            mListener = (RoomContractFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_guest_edit_contract, container, false);
        return binding.getRoot();
    }

    @Override
    public void displayContractData(MainGuest mainGuest) {
        this.guestContract = mainGuest;
        if (mainGuest != null) {
            binding.edtTenKhach.setText(mainGuest.getNameGuest());
            binding.edtSoDienThoai.setText(mainGuest.getPhoneGuest());
            binding.edtSoCccdCmnd.setText(mainGuest.getCccdNumber());
            binding.edtNgaySinh.setText(mainGuest.getDateOfBirth());
            binding.edtGioiTinh.setText(mainGuest.getGender());
            binding.edtTotalMembers.setText(String.valueOf(mainGuest.getTotalMembers()));
            binding.edtNgayTaoHopDong.setText(mainGuest.getCreateDate());
            binding.edtNgayVaoO.setText(mainGuest.getDateIn());
            binding.edtGiaPhong.setText(formatPrice(mainGuest.getRoomPrice()));
            binding.edtNgayHetHanHopDong.setText(mainGuest.getExpirationDate());
            binding.edtNgayTraTienPhong.setText(mainGuest.getPayDate());
            binding.edtHanThanhToan.setText(String.valueOf(mainGuest.getDaysUntilDueDate()));

            if (mainGuest.getCccdImageFront() != null) {
                binding.imgAddCccdFront.setImageBitmap(decodeImage(mainGuest.getCccdImageFront()));
            }
            if (mainGuest.getCccdImageBack() != null) {
                binding.imgAddCccdBack.setImageBitmap(decodeImage(mainGuest.getCccdImageBack()));
            }
            if (mainGuest.getContractImageFront() != null) {
                binding.imgAddContractFront.setImageBitmap(decodeImage(mainGuest.getContractImageFront()));
            }
            if (mainGuest.getContractImageBack() != null) {
                binding.imgAddContractBack.setImageBitmap(decodeImage(mainGuest.getContractImageBack()));
            }
        } else {
            showToast("MainGuest data is null");
        }
        setListener();
    }

    private void setListener() {
        binding.edtGioiTinh.setAdapter(presenter.getGenderAdapter());
        binding.edtTotalMembers.setAdapter(presenter.getTotalMembersAdapter());
        binding.edtNgayTraTienPhong.setAdapter(presenter.getDaysAdapter());
        presenter.setUpDateOfBirthField(binding.tilNgaySinh, binding.edtNgaySinh, binding.imgButtonCalendarNgaySinh, getString(R.string.dd_mm_yyyy));

        binding.imgAddCccdFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgAddCccdBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgCccdFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgCccdBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CCCD_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });

        binding.imgAddContractFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgAddContractBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgContractFront.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_FRONT;
            pickImage.launch(presenter.prepareImageSelection());
        });
        binding.imgContractBack.setOnClickListener(v -> {
            currentImageSelection = IMAGE_SELECTION_CONTRACT_BACK;
            pickImage.launch(presenter.prepareImageSelection());
        });

        checkName();
        checkPhoneNumber();
        checkCCCDNumber();
        setRoomPrice();

        presenter.setUpContractCreateDateField(binding.tilNgayTaoHopDong, binding.edtNgayTaoHopDong, binding.imgButtonCalendarTao,
                getString(R.string.dd_mm_yyyy));
        presenter.setUpDateInField(binding.tilNgayVaoO, binding.edtNgayVaoO, binding.imgButtonCalendarNgayVao,
                getString(R.string.dd_mm_yyyy));
        presenter.setUpContractExpireDateField(binding.tilNgayHetHanHopDong, binding.edtNgayHetHanHopDong, binding.imgButtonCalendarHetHan,
                getString(R.string.dd_mm_yyyy));

        binding.btnAddContract.setOnClickListener(v -> openSaveDialog());
        binding.btnCancel.setOnClickListener(v -> {
            back();
        });
    }

    private void back() {
        requireActivity().getSupportFragmentManager().popBackStack();
        if (mListener != null) {
            mListener.showTabLayoutAndViewPager();
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void saveSuccessfully() {

    }

    @Override
    public String getInfoHomeFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_HOME_ID);
    }

    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price);
    }

    private Bitmap decodeImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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
    public void checkName() {
        presenter.setUpNameField(binding.edtTenKhach, binding.tilTenKhach);
    }

    public void checkPhoneNumber() {
        presenter.setUpPhoneNumberField(binding.edtSoDienThoai, binding.tilSoDienThoai, countryCodePicker);
    }

    public void checkCCCDNumber() {
        presenter.setUpCCCDField(binding.edtSoCccdCmnd, binding.tilSoCccdCmnd);
    }

    public void setRoomPrice() {
        String roomPrice = String.valueOf(guestContract.getRoomPrice());
        binding.edtGiaPhong.setText(roomPrice);
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
                // clearInputFields();
                dialog.dismiss();
                progressBar.setVisibility(View.INVISIBLE);

                requireActivity().getSupportFragmentManager().popBackStack();
            }, 1000);
        });

        dialog.show();
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

                dialog.dismiss();
                if (saveContractSuccessfully) {
                    MainDetailedRoomActivity activity = (MainDetailedRoomActivity) getActivity();
                    if (activity != null) {
                        activity.showTabLayoutEditRoomGuestFragment();
                    }
                } else {
                    Toast.makeText(requireContext(), "Lưu hợp đồng thất bại", Toast.LENGTH_SHORT).show();
                }
            }, 2000);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

    private boolean saveContract() {
        String nameGuest = getStringFromEditText(binding.edtTenKhach);
        String phoneGuest = getStringFromEditText(binding.edtSoDienThoai);
        String cccdNumber = getStringFromEditText(binding.edtSoCccdCmnd);
        String dateOfBirth = getStringFromEditText(binding.edtNgaySinh);
        String gender = getStringFromAutoCompleteTextView(binding.edtGioiTinh);
        String totalMembers = getStringFromAutoCompleteTextView(binding.edtTotalMembers);
        String createDate = getStringFromEditText(binding.edtNgayTaoHopDong);
        String dateIn = getStringFromEditText(binding.edtNgayVaoO);
        String expirationDate = getStringFromEditText(binding.edtNgayHetHanHopDong);
        String payDate = getStringFromEditText(binding.edtNgayTraTienPhong);
        String roomPrice = getStringFromEditText(binding.edtGiaPhong).replace(".", "");
        String daysUntilDueDateStr = getStringFromEditText(binding.edtHanThanhToan);

        System.out.println("Name Guest: " + nameGuest);
        System.out.println("Phone Guest: " + phoneGuest);
        System.out.println("CCCD Number: " + cccdNumber);
        System.out.println("Date of Birth: " + dateOfBirth);
        System.out.println("Gender: " + gender);
        System.out.println("Total Members: " + totalMembers);
        System.out.println("Create Date: " + createDate);
        System.out.println("Date In: " + dateIn);
        System.out.println("Expiration Date: " + expirationDate);
        System.out.println("Pay Date: " + payDate);
        System.out.println("Room Price: " + roomPrice);
        System.out.println("Days Until Due Date: " + daysUntilDueDateStr);

        if (nameGuest.isEmpty() || phoneGuest.isEmpty() || cccdNumber.isEmpty() || dateOfBirth.isEmpty() ||
                gender.isEmpty() || totalMembers.isEmpty() || createDate.isEmpty() || dateIn.isEmpty() || expirationDate.isEmpty() ||
                payDate.isEmpty() || daysUntilDueDateStr.isEmpty()) {
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

        guestContract.setNameGuest(nameGuest);
        guestContract.setPhoneGuest(phoneGuest);
        guestContract.setCccdNumber(cccdNumber);
        guestContract.setDateOfBirth(dateOfBirth);
        guestContract.setGender(gender);
        guestContract.setTotalMembers(Integer.parseInt(totalMembers));
        guestContract.setCreateDate(createDate);
        guestContract.setDateIn(dateIn);
        guestContract.setExpirationDate(expirationDate);
        guestContract.setPayDate(payDate);
        guestContract.setRoomPrice(Double.parseDouble(roomPrice));
        guestContract.setDaysUntilDueDate(daysUntilDueDate);
        guestContract.setCccdImageFront(encodedCCCDFrontImage);
        guestContract.setCccdImageBack(encodedCCCDBackImage);
        guestContract.setContractImageFront(encodedContractFrontImage);
        guestContract.setContractImageBack(encodedContractBackImage);
        guestContract.setFileStatus(true);

        presenter.saveContract(guestContract, roomId);
        return true;
    }
}