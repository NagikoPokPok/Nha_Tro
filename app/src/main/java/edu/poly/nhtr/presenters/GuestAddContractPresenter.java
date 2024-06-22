package edu.poly.nhtr.presenters;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ArrayAdapter;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import edu.poly.nhtr.R;
import edu.poly.nhtr.interfaces.GuestAddContractInterface;

public class GuestAddContractPresenter {
    private final GuestAddContractInterface view;
    private final Context context;
    private static final int REQUIRED_DATE_LENGTH = 8;

    public GuestAddContractPresenter(GuestAddContractInterface view, Context context) {
        this.view = view;
        this.context = context;
    }


    public void setUpDropDownMenuGender() {
        view.setUpDropDownMenuGender();
    }

    public void setUpDropDownMenuTotalMembers() {
        view.setUpDropDownMenuTotalMembers();
    }

    public void setUpDateField(TextInputLayout textInputLayout, TextInputEditText textInputEditText, String hint) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        textInputEditText.setOnClickListener(v -> {
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

    public ArrayAdapter<String> getGenderAdapter() {
        String[] genderOptions = context.getResources().getStringArray(R.array.gender_options);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, genderOptions);
    }

    public ArrayAdapter<String> getTotalMembersAdapter() {
        String[] totalMembersOptions = context.getResources().getStringArray(R.array.numbers);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, totalMembersOptions);
    }

    public Intent prepareImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }


    private boolean isValidSignUpDetails(String name, String phoneNumber, String cccd) {
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        if (TextUtils.isEmpty(name)) {
            view.showErrorMessage("Hãy nhập tên");
            view.setNameErrorMessage("Không được bỏ trống");
            return false;
        } else if (TextUtils.isEmpty(phoneNumber)) {
            view.showErrorMessage("Hãy nhập số điện thoại");
            view.setPhoneErrorMessage("Không được bỏ trống");
            return false;
        } else if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            view.showErrorMessage("Nhập số điện thoại hợp lệ");
            view.setPhoneErrorMessage("Số điện thoại không hợp lệ");
            return false;
        } else if (TextUtils.isEmpty(cccd)) {
            view.showErrorMessage("Hãy nhập số CCCD");
            view.setCCCDNumberErrorMessage("Không được bỏ trống");
            return false;
        } else if (cccd.length() != 12 ) {
            view.showErrorMessage("Hãy nhập CCCD đúng định dạng");
            view.setCCCDNumberErrorMessage("CCCD không hợp lệ");
            return false;
        }
        return true;
    }

    public void handleNameChanged(String name) {
        view.setNameErrorEnabled(name == null || name.isEmpty());
    }

    public void handlePhoneNumberChanged(String phoneNumber) {
        view.setPhoneNumberlErrorEnabled(phoneNumber == null || phoneNumber.isEmpty());
    }

    public void handleCCCDNumberChanged(String cccd) {
        view.setCCCDNumberlErrorEnabled(cccd == null || cccd.isEmpty());
    }


//    public void signUp(Guest guest) {
//        if (isValidSignUpDetails(guest.getNameGuest(), guest.getPhoneGuest(), guest.ge(), user.getConfirmPassword())) {
//            checkExisted(user);
//        }
//    }
}
