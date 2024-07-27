package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import edu.poly.nhtr.Adapter.GuestAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.alarmManager.AlarmService;
import edu.poly.nhtr.databinding.FragmentRoomGuestBinding;
import edu.poly.nhtr.databinding.ItemContainerGuestBinding;
import edu.poly.nhtr.interfaces.RoomGuestInterface;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomViewModel;
import edu.poly.nhtr.presenters.RoomBillPresenter;
import edu.poly.nhtr.presenters.RoomGuestPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;
import timber.log.Timber;

public class RoomGuestFragment extends Fragment implements RoomGuestInterface.View {

    private static final int REQUIRED_DATE_LENGTH = 8; // Độ dài chuỗi ngày tháng năm yêu cầu
    private static final int FULL_DATE_LENGTH = 10; // dd/MM/yyyy
    private FragmentRoomGuestBinding binding;
    private RecyclerView recyclerView;
    private GuestAdapter adapter;
    private PreferenceManager preferenceManager;
    private Dialog dialog;
    private RoomGuestInterface.Presenter presenter;
    private RoomViewModel roomViewModel;
    private Room room;
    private Home home;
    private AlarmService alarmService;
    private AlarmService alarmService2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRoomGuestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomViewModel = new ViewModelProvider(requireActivity()).get(RoomViewModel.class);
        presenter = new RoomGuestPresenter(this, roomViewModel);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());

        recyclerView = binding.guestsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo adapter và gán cho RecyclerView
        adapter = new GuestAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Lấy danh sách khách từ ViewModel và cập nhật RecyclerView
        roomViewModel.getGuests().observe(getViewLifecycleOwner(), guests -> {
            if (guests != null && !guests.isEmpty()) {
                List<Object> items = new ArrayList<>(guests);
                adapter.setGuestList(items);
                binding.progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                showNoDataFound();
            }
        });


        // Lấy ra Room ID từ ViewModel và lấy danh sách khách theo Room ID
        roomViewModel.getRoom().observe(getViewLifecycleOwner(), room -> {
            if (room != null) {
                String roomId = room.getRoomId();
                preferenceManager.putString(Constants.PREF_KEY_ROOM_ID, roomId);
                Timber.tag("RoomGuestFragment").d("Room ID: %s", roomId);
                presenter.getGuests(roomId);
            } else {
                Timber.tag("RoomGuestFragment").e("Room object is null");
                showError("Room data is not available");
            }
        });

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            home = (Home) arguments.getSerializable("home");
            if (room != null && home != null) {
                setAlarmForBill();
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }



        setListeners();
    }

    private void setAlarmForBill() {
        String header1 = "Sắp tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
        String body1 = "Bạn cần lập hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();

        String header2 = "Đã tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
        String body2 = "Bạn cần gửi hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
        presenter.getDayOfMakeBill(room.getRoomId(), new RoomGuestPresenter.OnGetDayOfMakeBillCompleteListener() {
            @Override
            public void onComplete(String dayOfMakeBill) {
                presenter.checkNotificationIsGiven(room.getRoomId(), home.getIdHome(), new RoomGuestPresenter.OnGetNotificationCompleteListener() {
                    @Override
                    public void onComplete(List<Notification> notificationList) {
                        if (notificationList.isEmpty()) {
                            int dayOfGiveBill = Integer.parseInt(dayOfMakeBill);

                            // Set alarm for reminding make bill
                            alarmService = new AlarmService(requireContext(), home, room, header1, body1);
                            setAlarm(alarmService::setRepetitiveAlarm, dayOfGiveBill - 1, generateRandomRequestCode()); // requestCode 1

                            // Set alarm for reminding give bill
                            alarmService2 = new AlarmService(requireContext(), home, room, header2, body2);
                            setAlarm(alarmService2::setRepetitiveAlarm, dayOfGiveBill, generateRandomRequestCode()); // requestCode 2
                        }
                    }
                });
            }
        });
    }

    private int generateRandomRequestCode() {
        Random random = new Random();
        return random.nextInt(1000000); // Giới hạn số ngẫu nhiên trong khoảng 0 đến 9999
    }

    private interface AlarmCallback {
        void onAlarmSet(long timeInMillis, int requestCode);
    }

    private void setAlarm(AlarmCallback callback, int day, int requestCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        pushAlarm(callback, calendar, requestCode);
    }

    private void pushAlarm(AlarmCallback callback, Calendar calendar, int requestCode) {
        callback.onAlarmSet(calendar.getTimeInMillis(), requestCode);
        dialog.dismiss();
    }

    @Override
    public View getRootView() {
        return getView();
    }

    @Override
    public void onGuestClick(Guest guest) {

    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showNoDataFound() {
        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingOfFunctions(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    @Override
    public String getInfoHomeFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_HOME_ID);
    }

    @Override
    public String getInfoRoomFromGoogleAccount() {
        return preferenceManager.getString(Constants.KEY_ROOM_ID);
    }

    @Override
    public void putGuestInfoInPreferences(String nameGuest, String phoneGuest, String dateIn, boolean status, String roomId, String homeId, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_GUEST_NAME, documentReference.getId());
        preferenceManager.putString(Constants.KEY_GUEST_PHONE, phoneGuest);
        preferenceManager.putString(Constants.KEY_GUEST_DATE_IN, dateIn);
        preferenceManager.putString(Constants.KEY_CONTRACT_STATUS, status + "");
        preferenceManager.putString(Constants.KEY_ROOM_ID, roomId);
        preferenceManager.putString(Constants.KEY_HOME_ID, homeId);

    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showErrorMessage(String message, int id) {
        TextInputLayout layout_name_guest = dialog.findViewById(id);
        layout_name_guest.setError(message);
    }


    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void disableAddGuestButton() {
        binding.btnAddGuest.setEnabled(false);
        binding.btnAddGuest.setBackground(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_clicked, null)));
    }

    @Override
    public void enableAddGuestButton() {
        binding.btnAddGuest.setEnabled(true);
        binding.btnAddGuest.setBackground(Objects.requireNonNull(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_add, null)));
    }

    @Override
    public void openPopup(View view, Guest guest, ItemContainerGuestBinding binding) {
        openMenuForEachRoom(view, guest, binding);
    }

    @Override
    public void openDialogSuccess(int id) {
        setUpDialog(id);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void dialogClose() {
        dialog.dismiss();
    }

    @Override
    public boolean isAdded2() {
        return isAdded();
    }

    @Override
    public void cancelDeleteAll() {
        binding.layoutDeleteAll.setVisibility(View.GONE);
        binding.btnAddGuest.setVisibility(View.VISIBLE);
    }

    @Override
    public void noGuestData() {
        binding.guestsRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void openDeleteListDialog(List<Guest> listGuest) {
        dialog.setContentView(R.layout.layout_dialog_delete_guest);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(true);
            dialog.show();
        }
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel_delete_guest);
        Button btn_delete_guest = dialog.findViewById(R.id.btn_delete_guest);


        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        btn_delete_guest.setOnClickListener(v -> {
            presenter.deleteListGuests(listGuest);
            dialog.dismiss();
            cancelDeleteAll();
        });

    }

    @Override
    public void deleteListAll(List<Guest> list) {
        binding.txtDeleteHere.setOnClickListener(v -> openDeleteListDialog(list));
        binding.txtCancelDeleteAll.setOnClickListener(v -> adapter.cancelDeleteAll());
    }

    @Override
    public void setDeleteAllUI() {
        binding.layoutDeleteAll.setVisibility(View.VISIBLE);
        binding.btnAddGuest.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setListeners() {
        binding.btnAddGuest.setOnClickListener(v -> openAddGuestDialog());
    }

    // Hàm set mau va font chu cho Text
    private Spannable customizeText(String s) {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }

    private void setUpDialog(int layoutId) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(true);
            dialog.show();
        }
    }

    // Update button color based on input field state
    private void updateButtonState(EditText edtNameGuest, EditText edtPhoneGuest, Button btnAdd) {
        String name = edtNameGuest.getText().toString().trim();
        String phone = edtPhoneGuest.getText().toString().trim();
        if (name.isEmpty() || phone.isEmpty()) {
            btnAdd.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_clicked, null));
        } else {
            btnAdd.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.custom_button_add, null));
        }
    }


    private void openAddGuestDialog() {
        setUpDialog(R.layout.layout_dialog_add_guest);
        TextView title = dialog.findViewById(R.id.txt_title_dialog_guest);
        TextView nameGuest = dialog.findViewById(R.id.txt_name_guest);
        TextView phoneGuest = dialog.findViewById(R.id.txt_phone_number);
        TextView dateInGuest = dialog.findViewById(R.id.txt_date_in);

        EditText edtNameGuest = dialog.findViewById(R.id.edt_name_guest);
        EditText edtPhoneGuest = dialog.findViewById(R.id.edt_phone_number);
        EditText edtDateIn = dialog.findViewById(R.id.edt_date_in);

        TextInputLayout nameGuestLayout = dialog.findViewById(R.id.layout_name_guest);
        TextInputLayout phoneGuestLayout = dialog.findViewById(R.id.layout_phone_number);
        TextInputLayout dateInLayout = dialog.findViewById(R.id.layout_date_in);

        Button btnAddGuest = dialog.findViewById(R.id.btn_add_new_guest);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        int boxStrokeColor = getResources().getColor(R.color.colorPrimary);

        nameGuest.append(customizeText("*"));
        phoneGuest.append(customizeText("*"));

        title.setText("Thêm khách mới");
        edtNameGuest.setHint("Ví dụ: Nguyễn Văn A");
        edtPhoneGuest.setHint("Ví dụ: 0123456789");
        edtDateIn.setHint("Ví dụ: 01/01/2022");
        btnAddGuest.setText("Thêm khách");

        edtNameGuest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.handleNameChanged(s.toString(), nameGuestLayout, boxStrokeColor);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        edtPhoneGuest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.handlePhoneChanged(s.toString(), phoneGuestLayout, boxStrokeColor);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        edtDateIn.addTextChangedListener(new TextWatcher() {
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
                    int sel = c1;

                    final int MAX_DAY_MONTH_FORMAT_LENGTH = 6;

                    for (int i = 2; i < c1 && i < MAX_DAY_MONTH_FORMAT_LENGTH; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanCurr)) sel--;

                    if (clean.length() < 8) {
                        String ddmmyyyy = "DDMMYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        year = Math.min(Math.max(year, 2000), 2100);
                        cal.set(Calendar.YEAR, year);

                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format(Locale.getDefault(), "%02d%02d%04d", day, month, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    edtDateIn.setText(current);
                    edtDateIn.setSelection(Math.min(sel, current.length()));
                }

                if (s.length() == REQUIRED_DATE_LENGTH) {
                    presenter.handleCheckInDateChanged(s.toString(), preferenceManager.getString(Constants.KEY_ROOM_ID), dateInLayout, boxStrokeColor);
                } else {
                    dateInLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameGuest, edtPhoneGuest, btnAddGuest);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        edtNameGuest.addTextChangedListener(textWatcher);
        edtPhoneGuest.addTextChangedListener(textWatcher);

        btnAddGuest.setOnClickListener(v -> {
            if (nameGuestLayout.getError() == null && phoneGuestLayout.getError() == null && dateInLayout.getError() == null) {
                String name = edtNameGuest.getText().toString().trim();
                String phone = edtPhoneGuest.getText().toString().trim();
                String dateIn = edtDateIn.getText().toString().trim();
                Guest guest = new Guest(name, phone, true, dateIn);
                presenter.addGuestToFirebase(guest);

                String roomId = preferenceManager.getString(Constants.PREF_KEY_ROOM_ID);
                presenter.getGuests(roomId);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Hãy điền thông tin chính xác để lưu", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }




    private void openUpdateGuestDialog(Guest guest) {

        setUpDialog(R.layout.layout_dialog_update_guest);

        // Ánh xạ ID
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnUpdateGuest = dialog.findViewById(R.id.btn_add_new_guest);
        EditText edtNameGuest = dialog.findViewById(R.id.edt_name_guest);
        EditText edtPhoneGuest = dialog.findViewById(R.id.edt_phone_number);
        EditText edtDateIn = dialog.findViewById(R.id.edt_date_in);
        TextView title = dialog.findViewById(R.id.txt_title_dialog_guest);
        TextView txtNameGuest = dialog.findViewById(R.id.txt_name_guest);
        TextView txtPhoneGuest = dialog.findViewById(R.id.txt_phone_number);
        TextInputLayout layoutNameGuest = dialog.findViewById(R.id.layout_name_guest);
        TextInputLayout layoutPhoneGuest = dialog.findViewById(R.id.layout_phone_number);
        TextInputLayout layoutDateIn = dialog.findViewById(R.id.layout_date_in);
        int boxStrokeColor = getResources().getColor(R.color.colorPrimary);

        //Hiện thông tin lên edt
        edtNameGuest.setText(guest.getNameGuest());
        edtPhoneGuest.setText(guest.getPhoneGuest());
        edtDateIn.setText(guest.getDateIn());
        title.setText("Chỉnh sửa thông tin khách");
        btnUpdateGuest.setText("Cập nhật");
        txtNameGuest.append(customizeText(" *"));
        txtPhoneGuest.append(customizeText(" *"));
        btnUpdateGuest.setBackground(getResources().getDrawable(R.drawable.custom_button_add));

        edtNameGuest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.handleNameChanged(s.toString(), layoutNameGuest, boxStrokeColor);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtPhoneGuest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.handlePhoneChanged(s.toString(), layoutPhoneGuest, boxStrokeColor);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtDateIn.addTextChangedListener(new TextWatcher() {
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

                    if (clean.length() < 8) {
                        String ddmmyyyy = "DDMMYYYY";
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        cal.set(Calendar.MONTH, month - 1);
                        year = Math.min(Math.max(year, 2000), 2100);
                        cal.set(Calendar.YEAR, year);

                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format(Locale.getDefault(), "%02d%02d%04d", day, month, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    edtDateIn.setText(current);
                    edtDateIn.setSelection(Math.min(sel, current.length()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == REQUIRED_DATE_LENGTH) {
                    presenter.handleCheckInDateChanged(s.toString(), preferenceManager.getString(Constants.KEY_ROOM_ID), layoutDateIn, boxStrokeColor);
                } else {
                    layoutDateIn.setError(null);
                }
            }
        });


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameGuest, edtPhoneGuest, btnUpdateGuest);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edtNameGuest.addTextChangedListener(textWatcher);
        edtPhoneGuest.addTextChangedListener(textWatcher);

        // Xử lý sự kiện cho Button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpdateGuest.setOnClickListener(v -> {

            if (layoutNameGuest.getError() == null && layoutPhoneGuest.getError() == null) {
                // Lấy dữ liệu
                String newNameGuest = edtNameGuest.getText().toString().trim();
                String newPhoneGuest = edtPhoneGuest.getText().toString().trim();
                String newDateIn = edtDateIn.getText().toString().trim();

                // Cập nhật dữ liệu
                guest.setNameGuest(newNameGuest);
                guest.setPhoneGuest(newPhoneGuest);
                guest.setDateIn(newDateIn);
                guest.setFileStatus(true);

                presenter.updateGuestInFirebase(guest);
            } else {
                Toast.makeText(getContext(), "Please correct the errors before adding a guest", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDeleteGuestDialog(Guest guest) {

        setUpDialog(R.layout.layout_dialog_delete_guest);

        // Ánh xạ ID
        TextView txtConfirmDeleteGuest = dialog.findViewById(R.id.txt_confirm_delete_guest);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_delete_guest);
        Button btnDeleteGuest = dialog.findViewById(R.id.btn_delete_guest);

        // Hiệu chỉnh TextView
        String text = " " + guest.getNameGuest() + " ?";
        txtConfirmDeleteGuest.append(text);

        // Xử lý sự kiện cho Button
        btnCancel.setOnClickListener(v -> dialog.dismiss());


        btnDeleteGuest.setOnClickListener(v -> {
            presenter.deleteGuest(guest);
            dialog.dismiss();
        });

    }

    private void openMenuForEachRoom(View view, Guest guest, ItemContainerGuestBinding binding) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit_guest) {
                // Thực hiện hành động cho mục chỉnh sửa
                openUpdateGuestDialog(guest);
                return true;
            } else if (itemId == R.id.menu_delete_guest) {
                // Thực hiện hành động cho mục xóa
                openDeleteGuestDialog(guest);
                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(menu -> {
            binding.frmImage2.setVisibility(View.INVISIBLE);
            binding.frmImage.setVisibility(View.VISIBLE);
        });

        popupMenu.inflate(R.menu.menu_edit_delete_guest);
        popupMenu.show();
    }

}
