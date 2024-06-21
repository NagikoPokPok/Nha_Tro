package edu.poly.nhtr.presenters;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

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

    public void initializeViews() {
        view.initializeViews();
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

    public ArrayAdapter<String> getGenderAdapter() {
        String[] genderOptions = context.getResources().getStringArray(R.array.gender_options);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, genderOptions);
    }

    public ArrayAdapter<String> getTotalMembersAdapter() {
        String[] totalMembersOptions = context.getResources().getStringArray(R.array.numbers);
        return new ArrayAdapter<>(context, R.layout.dropdown_layout, totalMembersOptions);
    }
}
