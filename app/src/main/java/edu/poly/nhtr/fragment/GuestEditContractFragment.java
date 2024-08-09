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
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.R;
import edu.poly.nhtr.alarmManager.AlarmService;
import edu.poly.nhtr.databinding.FragmentGuestEditContractBinding;
import edu.poly.nhtr.listeners.GuestEditContractListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.GuestEditContractPresenter;
import edu.poly.nhtr.presenters.RoomGuestPresenter;
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
    private Room room;
    private Home home;
    private AlarmService alarmService;
    private AlarmService alarmService2;
    private int requestCode1, requestCode2;
    private String header1, body1, header2, body2;
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
                                        encodedCCCDFrontImage = encodeImageForCCCD(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CCCD_BACK:
                                        binding.imgCccdBack.setImageBitmap(bitmap);
                                        binding.imgAddCccdBack.setVisibility(View.GONE);
                                        encodedCCCDBackImage = encodeImageForCCCD(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CONTRACT_FRONT:
                                        binding.imgContractFront.setImageBitmap(bitmap);
                                        binding.imgAddContractFront.setVisibility(View.GONE);
                                        encodedContractFrontImage = encodeImageForContract(bitmap);
                                        break;
                                    case IMAGE_SELECTION_CONTRACT_BACK:
                                        binding.imgContractBack.setImageBitmap(bitmap);
                                        binding.imgAddContractBack.setVisibility(View.GONE);
                                        encodedContractBackImage = encodeImageForContract(bitmap);
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
        preferenceManager = new PreferenceManager(requireContext());

        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            home = (Home) arguments.getSerializable("home");
            if (room != null) {
                header1 = "Sắp tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
                body1 = "Bạn cần lập hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();

                header2 = "Đã tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
                body2 = "Bạn cần gửi hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();

                alarmService = new AlarmService(requireContext(), home, room, header1, body1);
                alarmService2 = new AlarmService(requireContext(), home, room, header2, body2);
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
                binding.imgAddCccdFront.setImageBitmap(getConversionImageForCCCD(mainGuest.getCccdImageFront()));
            }
            if (mainGuest.getCccdImageBack() != null) {
                binding.imgAddCccdBack.setImageBitmap(getConversionImageForCCCD(mainGuest.getCccdImageBack()));
            }
            if (mainGuest.getContractImageFront() != null) {
                binding.imgAddContractFront.setImageBitmap(getConversionImageForContract(mainGuest.getContractImageFront()));
            }
            if (mainGuest.getContractImageBack() != null) {
                binding.imgAddContractBack.setImageBitmap(getConversionImageForContract(mainGuest.getContractImageBack()));
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

    private Bitmap getConversionImageForCCCD(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int heightDp = 250;
        int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);

        int widthPx = bitmap.getWidth() * heightPx / bitmap.getHeight();

        return Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true);
    }

    private Bitmap getConversionImageForContract(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        int heightDp = 300;
        int heightPx = (int) (heightDp * getResources().getDisplayMetrics().density);

        int widthPx = bitmap.getWidth() * heightPx / bitmap.getHeight();

        return Bitmap.createScaledBitmap(bitmap, widthPx, heightPx, true);
    }

    private String encodeImage(Bitmap bitmap, int previewWidth, int previewHeight) {
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private String encodeImageForCCCD(Bitmap bitmap) {
        int previewWidth = 250;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        return encodeImage(bitmap, previewWidth, previewHeight);
    }

    private String encodeImageForContract(Bitmap bitmap) {
        int previewWidth = 300;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        return encodeImage(bitmap, previewWidth, previewHeight);
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
                    updateAlarm(() -> {
                        MainDetailedRoomActivity activity = (MainDetailedRoomActivity) getActivity();
                        if (activity != null) {
                            activity.showTabLayoutEditRoomGuestFragment();
                        }

                        dialog.dismiss();
                    });

                } else {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "Lưu hợp đồng thất bại", Toast.LENGTH_SHORT).show();
                }
            }, 2000);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void updateAlarm(onUpdateAlarm listener) {
        presenter.getDayOfMakeBill(room.getRoomId(), new GuestEditContractPresenter.OnGetDayOfMakeBillCompleteListener() {
            @Override
            public void onComplete(MainGuest mainGuest) {
                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentYear = calendar.get(Calendar.YEAR);

                int dayOfGiveBill = Integer.parseInt(mainGuest.getPayDate());
                String date = mainGuest.getDateIn();

                String[] dateParts = date.split("/");
                int dayDateInOfGuest = Integer.parseInt(dateParts[0]);
                int monthDateInOfGuest = Integer.parseInt(dateParts[1]);
                int yearDateInOfGuest = Integer.parseInt(dateParts[2]);

                int month = currentMonth;
                int year = currentYear;

                String str1 = preferenceManager.getString(Constants.KEY_NOTIFICATION_DAY_PUSH_NOTIFICATION_1, room.getRoomId() + "code1");
                String str2 = preferenceManager.getString(Constants.KEY_NOTIFICATION_DAY_PUSH_NOTIFICATION_2, room.getRoomId() + "code2");

                int previousDay1 = str1 != null ? Integer.parseInt(str1) : -1;
                int previousDay2 = str2 != null ? Integer.parseInt(str2) : -1;

                // Kiểm tra nếu ngày hóa đơn mới có hợp lệ
                if (dayOfGiveBill < 1 || dayOfGiveBill > getLastDayOfMonth2(month, year)) {
                    showToast("Ngày hóa đơn không hợp lệ");
                    return;
                }

                // Kiểm tra nếu ngày hóa đơn mới xảy ra trước ngày hiện tại
                if (currentDay > dayOfGiveBill) {
                    showToast("Ngày hóa đơn đã qua");
                    return;
                }


                // Kiểm tra nếu alarm mới là giống như alarm cũ
                if (dayOfGiveBill == previousDay2 || dayOfGiveBill - 1 == previousDay1) {
                    showToast("Alarm đã được đặt");
                    return;
                }

                // Kiểm tra và cập nhật tháng và năm nếu cần thiết
                if (currentDay >= dayOfGiveBill) {
                    month = currentMonth + 1;
                    if (month > 12) {
                        month = 1;
                        year++;
                    }
                } else {
                    if (previousDay2 >= dayOfGiveBill || previousDay1 == dayOfGiveBill) {
                        month = currentMonth + 1;
                        if (month > 12) {
                            month = 1;
                            year++;
                        }
                    }
                }

                // Đặt lại alarm cho ngày đầu tháng mới
                if (dayOfGiveBill == 1) {
                    month = currentMonth == 12 ? 1 : currentMonth + 1;
                    year = currentMonth == 12 ? currentYear + 1 : currentYear;
                    showToast("Alarm sẽ được đặt lại cho ngày đầu tháng mới");
                }

                // Sinh mã yêu cầu cho alarm
                String requestCode1Str = preferenceManager.getString(Constants.KEY_NOTIFICATION_REQUEST_CODE, room.getRoomId() + "code1");
                int requestCode1 = requestCode1Str == null ? generateRandomRequestCode() : Integer.parseInt(requestCode1Str);

                String requestCode2Str = preferenceManager.getString(Constants.KEY_NOTIFICATION_REQUEST_CODE, room.getRoomId() + "code2");
                int requestCode2 = requestCode2Str == null ? generateRandomRequestCode() : Integer.parseInt(requestCode2Str);

                // Đặt lại alarm với ngày giao hợp đồng mới
                preferenceManager.putString(Constants.KEY_NOTIFICATION_REQUEST_CODE, String.valueOf(requestCode1), room.getRoomId() + "code1");
                setAlarm(alarmService::setRepetitiveAlarm, dayOfGiveBill - 1, month, year, requestCode1);

                preferenceManager.putString(Constants.KEY_NOTIFICATION_REQUEST_CODE, String.valueOf(requestCode2), room.getRoomId() + "code2");
                setAlarm(alarmService2::setRepetitiveAlarm, dayOfGiveBill, month, year, requestCode2);

                listener.onComplete();
            }
        });
    }

    public interface onUpdateAlarm{
        void onComplete();
    }



    // Hàm để lấy ngày cuối cùng của tháng
    private int getLastDayOfMonth2(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    private interface AlarmCallback {
        void onAlarmSet(long timeInMillis, int requestCode);
    }



    private void setAlarm(AlarmCallback callback, int day, int month, int year, int requestCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Vì tháng trong Calendar bắt đầu từ 0
        calendar.set(Calendar.DAY_OF_MONTH, day);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        pushAlarm(callback, calendar, requestCode);
    }

    private void pushAlarm(AlarmCallback callback, Calendar calendar, int requestCode) {
        callback.onAlarmSet(calendar.getTimeInMillis(), requestCode);
    }

    private int generateRandomRequestCode() {
        Random random = new Random();
        return random.nextInt(1000000); // Giới hạn số ngẫu nhiên trong khoảng 0 đến 9999
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

    public boolean saveContract() {
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

        boolean isNameEmpty = binding.tilTenKhach.getError() != null;
        boolean isDateInEmpty = binding.tilNgayVaoO.getError() != null;
        boolean isPhoneEmpty = binding.tilSoDienThoai.getError() != null;
        boolean isCccdEmpty = binding.tilSoCccdCmnd.getError() != null;
        boolean isDateOfBirthEmpty = binding.tilNgaySinh.getError() != null;
        boolean isCreateDateEmpty = binding.tilNgayTaoHopDong.getError() != null;
        boolean isExpirationDateEmpty = binding.tilNgayHetHanHopDong.getError() != null;
        boolean isPayDateEmpty = binding.tilNgayTraTienPhong.getError() != null;
        boolean isRoomPriceEmpty = binding.tilGiaPhong.getError() != null;
        boolean isDaysUntilDueDateEmpty = daysUntilDueDateStr.isEmpty();
        boolean isGenderEmpty = gender.isEmpty();
        boolean isTotalMembersEmpty = totalMembers.isEmpty();
        boolean isCccdFrontEmpty = encodedCCCDFrontImage == null;
        boolean isCccdBackEmpty = encodedCCCDBackImage == null;

        int daysUntilDueDate;
        try {
            daysUntilDueDate = Integer.parseInt(daysUntilDueDateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Ngày tới hạn không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (isNameEmpty || isPhoneEmpty || isCccdEmpty || isDateOfBirthEmpty || isGenderEmpty || isTotalMembersEmpty || isCreateDateEmpty || isDateInEmpty || isExpirationDateEmpty || isPayDateEmpty || isRoomPriceEmpty || isDaysUntilDueDateEmpty || isCccdFrontEmpty || isCccdBackEmpty){
            Toast.makeText(requireContext(), "Hãy nhập đầy đủ và chính xác thông tin các trường", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            MainGuest mainGuest = new MainGuest();
            mainGuest.setNameGuest(nameGuest);
            mainGuest.setPhoneGuest(phoneGuest);
            mainGuest.setCccdNumber(cccdNumber);
            mainGuest.setDateOfBirth(dateOfBirth);
            mainGuest.setGender(gender);
            mainGuest.setTotalMembers(Integer.parseInt(totalMembers));
            mainGuest.setCreateDate(createDate);
            mainGuest.setDateIn(dateIn);
            mainGuest.setExpirationDate(expirationDate);
            mainGuest.setPayDate(payDate);
            mainGuest.setRoomPrice(Double.parseDouble(roomPrice));
            mainGuest.setDaysUntilDueDate(daysUntilDueDate);
            mainGuest.setCccdImageFront(encodedCCCDFrontImage);
            mainGuest.setCccdImageBack(encodedCCCDBackImage);
            mainGuest.setContractImageFront(encodedContractFrontImage);
            mainGuest.setContractImageBack(encodedContractBackImage);
            mainGuest.setFileStatus(true);

            presenter.saveContract(guestContract, roomId);
            return true;
        }
    }
}