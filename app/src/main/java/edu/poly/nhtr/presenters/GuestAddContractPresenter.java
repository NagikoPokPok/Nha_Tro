package edu.poly.nhtr.presenters;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.poly.nhtr.R;
import edu.poly.nhtr.listeners.MainGuestListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.utilities.Constants;

public class GuestAddContractPresenter {
    private final MainGuestListener mainGuestListener;
    private final Context context;
    private static final int REQUIRED_DATE_LENGTH = 8;


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

    public void setUpDateField(TextInputLayout textInputLayout, TextInputEditText textInputEditText, ImageButton imgButtonCalendar, String hint) {
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

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
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

    public void setUpNameField(TextInputEditText textInputEditText, TextInputLayout textInputLayout) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleNameChanged(s.toString().trim(), textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    public void setUpPhoneNumberField(TextInputEditText textInputEditText, TextInputLayout textInputLayout) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handlePhoneNumberChanged(s.toString().trim(), textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
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


    public void handleNameChanged(String name, TextInputLayout textInputLayout) {
        if (textInputLayout == null) {
            Log.e("GuestAddContractPresenter", "TextInputLayout for name is null");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            textInputLayout.setError("Không được bỏ trống");
        } else {
            textInputLayout.setError(null);
        }
    }

    public void handlePhoneNumberChanged(String phoneNumber, TextInputLayout textInputLayout) {
        if (textInputLayout == null) {
            Log.e("GuestAddContractPresenter", "TextInputLayout for phone number is null");
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            textInputLayout.setError("Không được bỏ trống");
        } else if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            textInputLayout.setError("Số điện thoại không hợp lệ");
        } else {
            textInputLayout.setError(null);
        }
    }


    public void handleCCCDNumberChanged(String cccd) {
        mainGuestListener.setCCCDNumberlErrorEnabled(cccd == null || cccd.isEmpty());
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
        contract.put(Constants.KEY_CONTRACT_ROOM_PRICE, mainGuest.getRoomPrice());
        contract.put(Constants.KEY_CONTRACT_EXPIRATION_DATE, mainGuest.getExpirationDate());
        contract.put(Constants.KEY_CONTRACT_PAY_DATE, mainGuest.getPayDate());
        contract.put(Constants.KEY_CONTRACT_DAYS_UNTIL_DUE_DATE, mainGuest.getDaysUntilDueDate());
        contract.put(Constants.KEY_GUEST_CCCD_IMAGE_FRONT, mainGuest.getCccdImageFront());
        contract.put(Constants.KEY_GUEST_CCCD_IMAGE_BACK, mainGuest.getCccdImageBack());
        contract.put(Constants.KEY_GUEST_CONTRACT_IMAGE_FRONT, mainGuest.getContractImageFront());
        contract.put(Constants.KEY_GUEST_CONTRACT_IMAGE_BACK, mainGuest.getContractImageBack());
        contract.put(Constants.KEY_CONTRACT_STATUS, mainGuest.getFileStatus());
        contract.put(Constants.KEY_TIMESTAMP, new Date());
        contract.put(Constants.KEY_HOME_ID, mainGuestListener.getInfoHomeFromGoogleAccount());
        contract.put(Constants.KEY_ROOM_ID, mainGuestListener.getInfoRoomFromGoogleAccount());

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
                            mainGuest.getRoomPrice(),
                            mainGuest.getExpirationDate(),
                            mainGuest.getPayDate(),
                            mainGuest.getDaysUntilDueDate(),
                            mainGuest.getCccdImageFront(),
                            mainGuest.getCccdImageBack(),
                            mainGuest.getContractImageFront(),
                            mainGuest.getContractImageBack(),
                            mainGuest.getFileStatus(),
                            mainGuestListener.getInfoHomeFromGoogleAccount(),
                            mainGuestListener.getInfoRoomFromGoogleAccount(),
                            documentReference
                    );
                    getMainGuest("add");
                    mainGuestListener.showToast("Thêm hợp đồng thành công");
                })
                .addOnFailureListener(e -> {
                    mainGuestListener.showToast("Thêm hợp đồng thất bại");
                });

    }

    public void getMainGuest(String action) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Query homes collection to get main guest IDs
        database.collection(Constants.KEY_COLLECTION_HOMES)
                .whereEqualTo(Constants.KEY_HOME_ID, mainGuestListener.getInfoHomeFromGoogleAccount())
                .whereEqualTo(Constants.KEY_ROOM_ID, mainGuestListener.getInfoRoomFromGoogleAccount())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> mainGuestIDs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            mainGuestIDs.add(document.getId());
                        }

                        // Check if there are main guests associated with the user
                        if (!mainGuestIDs.isEmpty()) {
                            // Query rooms collection using main guest IDs
                            database.collection(Constants.KEY_COLLECTION_ROOMS)
                                    .whereIn(Constants.KEY_CONTRACT_ID, mainGuestIDs)
                                    .get()
                                    .addOnCompleteListener(roomTask -> {
                                        if (roomTask.isSuccessful() && roomTask.getResult() != null) {
                                            List<MainGuest> mainGuests = new ArrayList<>();

                                            // Iterate through room documents to build main guest list
                                            for (QueryDocumentSnapshot roomDocument : roomTask.getResult()) {
                                                MainGuest mainGuest = roomDocument.toObject(MainGuest.class);
                                                mainGuests.add(mainGuest);
                                            }

                                            // Sort main guests by some criteria if needed
                                            mainGuests.sort(Comparator.comparing(MainGuest::getCreateDate));

                                            // Notify listener with main guest data
                                            mainGuestListener.onMainGuestsLoaded(mainGuests, action);
                                        } else {
                                            // Handle failure to fetch rooms
                                            Log.e("GuestAddContractPresenter", "Error fetching rooms: ", roomTask.getException());
                                            mainGuestListener.onMainGuestsLoadFailed();
                                        }
                                    });
                        } else {
                            // No main guests found for the user
                            mainGuestListener.onMainGuestsLoadFailed();
                        }
                    } else {
                        // Handle failure to fetch homes
                        Log.e("GuestAddContractPresenter", "Error fetching homes: ", task.getException());
                        mainGuestListener.onMainGuestsLoadFailed();
                    }
                });
    }

}
