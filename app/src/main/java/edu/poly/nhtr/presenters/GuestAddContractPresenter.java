package edu.poly.nhtr.presenters;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.MainGuestListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class GuestAddContractPresenter {
    private static final int REQUIRED_DATE_LENGTH = 8; // Độ dài chuỗi ngày tháng năm yêu cầu
    private final MainGuestListener mainGuestListener;
    private final Context context;

    public GuestAddContractPresenter(MainGuestListener mainGuestListener, Context context) {
        this.mainGuestListener = mainGuestListener;
        this.context = context;
    }


    public void setUpDropDownMenuGender() {
        mainGuestListener.setUpDropDownMenuGender();
    }

    public void setUpDropDownMenuTotalMembers() {
        mainGuestListener.setUpDropDownMenuTotalMembers();
    }

    public void setUpDropDownMenuDays() {
        mainGuestListener.setUpDropDownMenuDays();
    }


    public ArrayAdapter<String> getGenderAdapter() {
        String[] genderOptions = context.getResources().getStringArray(R.array.gender_options);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, genderOptions);
    }

    public ArrayAdapter<String> getTotalMembersAdapter() {
        String[] totalMembersOptions = context.getResources().getStringArray(R.array.numbers);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, totalMembersOptions);
    }

    public ArrayAdapter<String> getDaysAdapter() {
        String[] daysOptions = context.getResources().getStringArray(R.array.days);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, daysOptions);
    }

    public Intent prepareImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    public void setUpNameField(TextInputEditText textInputEditText, TextInputLayout textInputLayout) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handleNameChanged(s.toString().trim(), textInputLayout);
            }
        });
    }

    public void handleNameChanged(String name, TextInputLayout textInputLayout) {
        if (textInputLayout == null) {
            return;
        }

        if (TextUtils.isEmpty(name)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (!isValidName(name)) {
            textInputLayout.setError("Tên không được chứa số hoặc ký tự đặc biệt");
        } else {
            textInputLayout.setError(null);
        }
    }

    // Kiểm tra tên có chứa kí tự đặc biệt hoặc số không
    private boolean isValidName(String name) {
        String regex = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỂưạảấầẩẫậắằẳẵặẹẻẽềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễếệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ\\s]+$";
        return name.matches(regex);
    }

    // Sự kiện cho trường số điện thoại
    public void setUpPhoneNumberField(TextInputEditText textInputEditText, TextInputLayout textInputLayout, CountryCodePicker ccp) {
        if (textInputEditText == null || textInputLayout == null || ccp == null) {
            return;
        }

        ccp.setDefaultCountryUsingNameCode("VN");
        ccp.resetToDefaultCountry();
        // Đăng ký EditText với CountryCodePicker
        ccp.registerCarrierNumberEditText(textInputEditText);

        // Add a TextWatcher to the EditText
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handlePhoneNumberChanged(s.toString().trim(), textInputLayout, ccp);
            }
        });
    }

    // Xử lý sự kiện khi số điện thoại thay đổi
    public void handlePhoneNumberChanged(String phoneNumber, TextInputLayout textInputLayout, CountryCodePicker ccp) {
        if (textInputLayout == null) {
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            textInputLayout.setError("Số điện thoại không được để trống");
            return;
        }

        // Validate phone number with CountryCodePicker
        if (!ccp.isValidFullNumber()) {
            textInputLayout.setError("Số điện thoại không hợp lệ");
        } else {
            checkDuplicatePhoneNumber(phoneNumber, textInputLayout);
        }
    }

    private void checkDuplicatePhoneNumber(String phone, TextInputLayout textInputLayout) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.KEY_COLLECTION_GUESTS)
                .whereEqualTo(Constants.KEY_HOME_ID, mainGuestListener.getInfoHomeFromGoogleAccount())
                .whereEqualTo(Constants.KEY_GUEST_PHONE, phone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            textInputLayout.setError("Số điện thoại đã tồn tại trong nhà trọ này");
                        } else {
                            textInputLayout.setError(null);
                        }
                    } else {
                        textInputLayout.setError("Có lỗi xảy ra khi kiểm tra số điện thoại");
                    }
                });
    }



    // Sự kiện cho trường CCCD
    public void setUpCCCDField(TextInputEditText textInputEditText, TextInputLayout textInputLayout) {
        textInputEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                handleCCCDNumberChanged(s.toString().trim(), textInputLayout);
            }
        });
    }

    // Xử lý sự kiện khi số CCCD thay đổi
    public void handleCCCDNumberChanged(String cccd, TextInputLayout textInputLayout) {
        if (textInputLayout == null) {
            return;
        }
        if (TextUtils.isEmpty(cccd)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (cccd.length() != 12) {
            textInputLayout.setError("CCCD phải có đúng 12 chữ số");
        } else {
            textInputLayout.setError(null);
        }
    }

    public void setUpContractCreateDateField(TextInputLayout textInputLayout, TextInputEditText textInputEditText,
                                             ImageButton imgButtonCalendar, String hint) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        imgButtonCalendar.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, monthOfYear, dayOfMonth) -> {
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
                } else {
                    textInputLayout.setHint("");
                }
            }
        });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            private final Calendar cal = Calendar.getInstance();
            private String current = "";

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

                    final int MAX_DAY_MONTH_FORMAT_LENGTH = 6;

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;

                    if (clean.length() < REQUIRED_DATE_LENGTH) {
                        String ddmmyyyy = "DDmmYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        Calendar c = Calendar.getInstance();
                        year = (year < 2000) ? 2000 : Math.min(year, c.get(Calendar.YEAR));
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

    public void setUpDateInField(TextInputLayout textInputLayout, TextInputEditText textInputEditText,
                                 ImageButton imgButtonCalendar, String hint) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        imgButtonCalendar.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, monthOfYear, dayOfMonth) -> {
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
                } else {
                    textInputLayout.setHint("");
                }
            }
        });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            private final Calendar cal = Calendar.getInstance();
            private String current = "";

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

                    final int MAX_DAY_MONTH_FORMAT_LENGTH = 6;

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;

                    if (clean.length() < REQUIRED_DATE_LENGTH) {
                        String ddmmyyyy = "DDmmYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        Calendar c = Calendar.getInstance();
                        year = (year < 2000) ? 2000 : Math.min(year, c.get(Calendar.YEAR));
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


    public void setUpContractExpireDateField(TextInputLayout textInputLayout, TextInputEditText textInputEditText,
                                             ImageButton imgButtonCalendar, String hint) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        imgButtonCalendar.setOnClickListener(v -> {
            // Tạo DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, monthOfYear, dayOfMonth) -> {
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
                } else {
                    textInputLayout.setHint("");
                }
            }
        });

        textInputEditText.addTextChangedListener(new TextWatcher() {
            private String current = "";

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

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;

                    if (clean.length() < REQUIRED_DATE_LENGTH) {
                        String ddmmyyyy = "DDmmYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        month = Math.min(month, 12);
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.MONTH, month - 1);
                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        year = Math.max(2000, Math.min(year, 2100));

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

    // Sự kiện cho trường ngày sinh
    public void setUpDateOfBirthField(TextInputLayout textInputLayout, TextInputEditText textInputEditText, ImageButton imgButtonCalendar, String hint) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        imgButtonCalendar.setOnClickListener(v -> {
            // Create DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, monthOfYear, dayOfMonth) -> {
                String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year1);
                textInputEditText.setText(selectedDate);
                handleDateOfBirthChanged(selectedDate, textInputLayout); // Validate date of birth when date is selected
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
            private final Calendar cal = Calendar.getInstance();
            private String current = "";

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

                    // Maximum length for day and month formatting without slashes
                    final int MAX_DAY_MONTH_FORMAT_LENGTH = 6;

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;

                    if (clean.length() < REQUIRED_DATE_LENGTH) {
                        String ddmmyyyy = "DDmmYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        Calendar c = Calendar.getInstance();
                        year = (year < 1900) ? 1900 : Math.min(year, c.get(Calendar.YEAR));
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
                    handleDateOfBirthChanged(current, textInputLayout); // Validate date of birth as text changes
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

    // Xử lý sự kiện khi ngày sinh thay đổi
    public void handleDateOfBirthChanged(String dateOfBirth, TextInputLayout textInputLayout) {
        if (textInputLayout == null) {
            return;
        }
        if (TextUtils.isEmpty(dateOfBirth)) {
            textInputLayout.setError("Không được bỏ trống");
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            simpleDateFormat.setLenient(false);
            try {
                Date dob = simpleDateFormat.parse(dateOfBirth); // Date of birth
                Calendar calDob = Calendar.getInstance();
                calDob.setTime(dob);
                Calendar today = Calendar.getInstance();
                int year = today.get(Calendar.YEAR) - calDob.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < calDob.get(Calendar.DAY_OF_YEAR)) {
                    year--;
                }
                if (year < 16) {
                    textInputLayout.setError("Tuổi chủ phòng phải lớn hơn hoặc bằng 18");
                } else {
                    textInputLayout.setError(null);
                }
            } catch (ParseException e) {
                textInputLayout.setError("Ngày sinh không hợp lệ");
            }
        }
    }


    public void addContractToFirestore(MainGuest mainGuest) {
        HashMap<String, Object> contract = new HashMap<>();
        contract.put(Constants.KEY_ROOM_TOTAl_MEMBERS, mainGuest.getTotalMembers());
        contract.put(Constants.KEY_GUEST_NAME, mainGuest.getNameGuest());
        contract.put(Constants.KEY_GUEST_PHONE, mainGuest.getPhoneGuest());
        contract.put(Constants.KEY_GUEST_CCCD, mainGuest.getCccdNumber());
        contract.put(Constants.KEY_GUEST_DATE_OF_BIRTH, mainGuest.getDateOfBirth());
        contract.put(Constants.KEY_GUEST_GENDER, mainGuest.getGender());
        contract.put(Constants.KEY_CONTRACT_CREATED_DATE, mainGuest.getCreateDate());
        contract.put(Constants.KEY_GUEST_DATE_IN, mainGuest.getDateIn());
        contract.put(Constants.KEY_CONTRACT_ROOM_PRICE, mainGuest.getRoomPrice());
        contract.put(Constants.KEY_CONTRACT_EXPIRATION_DATE, mainGuest.getExpirationDate());
        contract.put(Constants.KEY_CONTRACT_PAY_DATE, mainGuest.getPayDate());
        contract.put(Constants.KEY_CONTRACT_DAYS_UNTIL_DUE_DATE, mainGuest.getDaysUntilDueDate());
        contract.put(Constants.KEY_GUEST_CCCD_IMAGE_FRONT, mainGuest.getCccdImageFront());
        contract.put(Constants.KEY_GUEST_CCCD_IMAGE_BACK, mainGuest.getCccdImageBack());
        contract.put(Constants.KEY_GUEST_CONTRACT_IMAGE_FRONT, mainGuest.getContractImageFront());
        contract.put(Constants.KEY_GUEST_CONTRACT_IMAGE_BACK, mainGuest.getContractImageBack());
        contract.put(Constants.KEY_CONTRACT_STATUS, mainGuest.isFileStatus());
        contract.put(Constants.KEY_TIMESTAMP, new Date());
        contract.put(Constants.KEY_ROOM_ID, mainGuestListener.getInfoRoomFromGoogleAccount());
        contract.put(Constants.KEY_HOME_ID, mainGuestListener.getInfoHomeFromGoogleAccount());

        // Add contract to Firebase
        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONTRACTS)
                .add(contract)
                .addOnSuccessListener(documentReference -> {
                    mainGuestListener.putContractInfoInPreferences(
                            mainGuest.getNameGuest(),
                            mainGuest.getPhoneGuest(),
                            mainGuest.getCccdNumber(),
                            mainGuest.getDateOfBirth(),
                            mainGuest.getGender(),
                            mainGuest.getTotalMembers(),
                            mainGuest.getCreateDate(),
                            mainGuest.getDateIn(),
                            mainGuest.getRoomPrice(),
                            mainGuest.getExpirationDate(),
                            mainGuest.getPayDate(),
                            mainGuest.getDaysUntilDueDate(),
                            mainGuest.getCccdImageFront(),
                            mainGuest.getCccdImageBack(),
                            mainGuest.getContractImageFront(),
                            mainGuest.getContractImageBack(),
                            mainGuest.isFileStatus(),
                            mainGuestListener.getInfoRoomFromGoogleAccount(),
                            mainGuestListener.getInfoHomeFromGoogleAccount(),
                            documentReference
                    );
                    mainGuestListener.showToast("Thêm hợp đồng thành công");
                })
                .addOnFailureListener(e -> mainGuestListener.showToast("Thêm hợp đồng thất bại"));

        // Thêm MainGuest vào collection guests trên Firebase
        HashMap<String, Object> guest = new HashMap<>();
        guest.put(Constants.KEY_GUEST_NAME, mainGuest.getNameGuest());
        guest.put(Constants.KEY_GUEST_PHONE, mainGuest.getPhoneGuest());
        guest.put(Constants.KEY_GUEST_DATE_IN, mainGuest.getDateIn());
        guest.put(Constants.KEY_CONTRACT_STATUS, mainGuest.isFileStatus());
        guest.put(Constants.KEY_ROOM_ID, mainGuestListener.getInfoRoomFromGoogleAccount());
        guest.put(Constants.KEY_HOME_ID, mainGuestListener.getInfoHomeFromGoogleAccount());
        guest.put(Constants.KEY_TIMESTAMP, new Date());
        guest.put(Constants.KEY_IS_MAIN_GUEST, true);

        FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_GUESTS)
                .add(guest)
                .addOnSuccessListener(documentReference -> {
                    mainGuestListener.putMainGuestInfoInPreferences(
                            mainGuest.getNameGuest(),
                            mainGuest.getPhoneGuest(),
                            mainGuest.getDateIn(),
                            mainGuest.isFileStatus(),
                            mainGuestListener.getInfoRoomFromGoogleAccount(),
                            mainGuestListener.getInfoHomeFromGoogleAccount(),
                            documentReference
                    );
                    mainGuestListener.showToast("Thêm khách thành công");
                })
                .addOnFailureListener(e -> mainGuestListener.showToast("Thêm khách thất bại"));
    }
}

