package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.flexbox.FlexboxLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import edu.poly.nhtr.Activity.MonthPickerDialogCustom;
import edu.poly.nhtr.Adapter.RoomBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.alarmManager.AlarmService;
import edu.poly.nhtr.databinding.FragmentRoomBillBinding;
import edu.poly.nhtr.databinding.ItemContainerInformationOfBillBinding;
import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Notification;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.presenters.RoomBillPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class RoomBillFragment extends Fragment implements RoomBillListener, SwipeRefreshLayout.OnRefreshListener {

    private FragmentRoomBillBinding binding;
    private RoomBillPresenter roomBillPresenter;
    private RoomBillAdapter roomBillAdapter;
    private List<RoomBill> billList = new ArrayList<>();
    private Room room;
    private OnMakeBillClickListener onMakeBillClickListener;
    private OnViewBillListener onViewBillListener;
    private int currentMonth;
    private int currentYear;
    private int currentDay;
    private boolean visible = true;
    private String date = "";
    private boolean isSelectAllChecked = false;
    private Dialog dialog;
    private PreferenceManager preferenceManager;
    private AlarmService alarmService;
    private AlarmService alarmService2;
    private Home home;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomBillPresenter = new RoomBillPresenter(this);
        roomBillAdapter = new RoomBillAdapter(requireContext(), billList, roomBillPresenter, this);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentRoomBillBinding.inflate(inflater, container, false);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        dialog = new Dialog(requireContext());

        binding.swipeRefreshFragment.setOnRefreshListener(this);


        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            home = (Home) arguments.getSerializable("home");
            if (room != null && home != null) {
                checkAndAddBillIfNeeded();
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

//        String header1 = "Sắp tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
//        String body1 = "Bạn cần lập hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
//
//        String header2 = "Đã tới ngày gửi hoá đơn cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
//        String body2 = "Bạn cần gửi hoá đơn tháng này cho phòng " + room.getNameRoom() + " tại nhà trọ " + home.getNameHome();
//
//        roomBillPresenter.getDayOfMakeBill(room.getRoomId(), new RoomBillPresenter.OnGetDayOfMakeBillCompleteListener() {
//            @Override
//            public void onComplete(String dayOfMakeBill) {
//                roomBillPresenter.checkNotificationIsGiven(room.getRoomId(), home.getIdHome(), new RoomBillPresenter.OnGetNotificationCompleteListener() {
//                    @Override
//                    public void onComplete(List<Notification> notificationList) {
//                        if (notificationList.isEmpty()) {
//                            int dayOfGiveBill = Integer.parseInt(dayOfMakeBill);
//
//                            // Set alarm for reminding make bill
//                            alarmService = new AlarmService(requireContext(), home, room, header1, body1);
//                            setAlarm(alarmService::setRepetitiveAlarm, dayOfGiveBill - 1, generateRandomRequestCode()); // requestCode 1
//
//                            // Set alarm for reminding give bill
//                            alarmService2 = new AlarmService(requireContext(), home, room, header2, body2);
//                            setAlarm(alarmService2::setRepetitiveAlarm, dayOfGiveBill, generateRandomRequestCode()); // requestCode 2
//                        }
//                    }
//                });
//            }
//        });

        removeStatusOfCheckBoxFilterBill();
        setupRecyclerView();
        setupMonthPicker();
        setupDeleteManyBills();
        setupFilterBills();



        return binding.getRoot();
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
    public void onRefresh() {
        // Get list bill
        checkAndAddBillIfNeeded();
        // Remove layout delete bills
        closeLayoutDeleteBills();
        // Remove layout filter bills
        closeLayoutFilterBills();
        // Remove date time
        binding.txtDateTime.setText("");
        binding.btnCancelMonthPicker.setVisibility(View.GONE);
        removeStatusOfCheckBoxFilterBill();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.swipeRefreshFragment.setRefreshing(false);
            }
        }, 2000);
    }




    private void checkAndAddBillIfNeeded() {

        roomBillPresenter.checkContractIsCreated(room, new RoomBillPresenter.OnGetContractCompleteListener() {
            @Override
            public void onComplete(boolean isHave) {
                if (!isHave) {
                    return;
                }else {
                    checkAndHandleBillCreation();
                }
            }
        });
    }

    private void checkAndHandleBillCreation() {
        roomBillPresenter.checkBillIsCreated(room, currentMonth + 1, currentYear, new RoomBillPresenter.OnCheckBillIsCreatedCompleteListener() {
            @Override
            public void onComplete(boolean isCreated) {
                if (isCreated) {
                    fetchBillList();
                } else {
                    roomBillPresenter.addBill(room);
                }
            }
        });
    }

    private void fetchBillList() {
        roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
            @Override
            public void onComplete(List<RoomBill> billList) {
                roomBillAdapter.setBillList(billList);
            }
        });
    }


    @Override
    public void setBillList(List<RoomBill> billList) {
        this.billList = billList;
        if (roomBillAdapter != null) {
            roomBillAdapter.setBillList(this.billList);
        }
    }






    private void setupFilterBills() {
        binding.layoutFilterBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnFilterBill.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                binding.frameRoundFilterBill.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
                openDialogFilterBills();

            }
        });

        binding.btnFilterBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnFilterBill.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                binding.frameRoundFilterBill.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
                openDialogFilterBills();
            }
        });
    }

    private void openDialogFilterBills() {
        setupDialog(R.layout.layout_dialog_filter_bill);

        // Intitial components
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirmApply = dialog.findViewById(R.id.btn_confirm_apply_bill);

        AppCompatCheckBox cbx1 = dialog.findViewById(R.id.cbx_is_not_pay_bill);
        AppCompatCheckBox cbx2 = dialog.findViewById(R.id.cbx_is_payed_bill);
        AppCompatCheckBox cbx3 = dialog.findViewById(R.id.cbx_is_delay_pay_bill);
        AppCompatCheckBox cbx4 = dialog.findViewById(R.id.cbx_is_not_give_bill);

        cbx1.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_IS_NOT_PAY_BILL));
        cbx2.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_IS_PAYED_BILL));
        cbx3.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_IS_DELAY_PAY_BILL));
        cbx4.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_IS_NOT_GIVE_BILL));

        List<AppCompatCheckBox> checkBoxList = new ArrayList<>();
        checkBoxList.add(cbx1);
        checkBoxList.add(cbx2);
        checkBoxList.add(cbx3);
        checkBoxList.add(cbx4);

        customizeButtonApplyInDialogHaveCheckBox(btnConfirmApply, checkBoxList);

        // Create a method to check the state of all checkboxes
        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeButtonApplyInDialogHaveCheckBox(btnConfirmApply, checkBoxList);
            }
        };

        // Set the listener to all checkboxes
        cbx1.setOnClickListener(checkBoxListener);
        cbx2.setOnClickListener(checkBoxListener);
        cbx3.setOnClickListener(checkBoxListener);
        cbx4.setOnClickListener(checkBoxListener);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirmApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutTypeOfFilterBill.setVisibility(View.VISIBLE);
                List<String> selectedOptions = new ArrayList<>();

                // Check which CheckBoxes are selected and save their state
                boolean filterByBill1 = cbx1.isChecked();
                boolean filterByBill2 = cbx2.isChecked();
                boolean filterByBill3 = cbx3.isChecked();
                boolean filterByBill4 = cbx4.isChecked();

                if (filterByBill1) {
                    selectedOptions.add(cbx1.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_NOT_PAY_BILL, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_NOT_PAY_BILL, false);
                    removeFromListAndSave(cbx1.getText().toString());
                }
                if (filterByBill2) {
                    selectedOptions.add(cbx2.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_PAYED_BILL, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_PAYED_BILL, false);
                    removeFromListAndSave(cbx2.getText().toString());
                }
                if (filterByBill3) {
                    selectedOptions.add(cbx3.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_DELAY_PAY_BILL, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_DELAY_PAY_BILL, false);
                    removeFromListAndSave(cbx3.getText().toString());
                }
                if (filterByBill4) {
                    selectedOptions.add(cbx4.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_NOT_GIVE_BILL, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_NOT_GIVE_BILL, false);
                    removeFromListAndSave(cbx4.getText().toString());
                }

                // If 3 check boxes are unchecked -> Hide layoutTypeOfFilterHomes
                if (!filterByBill1 && !filterByBill2 && !filterByBill3 && !filterByBill4) {
//                    binding.layoutNoData.setVisibility(View.GONE);
//                    binding.layoutTypeOfFilterBill.setVisibility(View.GONE);
                    closeLayoutFilterBills();
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
                        @Override
                        public void onComplete(List<RoomBill> billList) {
                            roomBillAdapter.setBillList(billList);
                        }
                    });
                } else {
                    filterListHomes(); // After put status of checkboxes in preferences, check and add them into the list
                }

                // Add selected options as LinearLayouts with TextView and ImageView to the main LinearLayout
                for (String option : selectedOptions) {
                    // Check if the checkbox is already in the listTypeOfFilterHome
                    boolean alreadyExists = false;
                    for (int i = 0; i < binding.listTypeOfFilterBill.getChildCount(); i++) {
                        View view = binding.listTypeOfFilterBill.getChildAt(i);
                        if (view instanceof LinearLayout) {
                            TextView textView = view.findViewById(R.id.txt_type_of_filter_home);
                            if (textView.getText().toString().equals(option)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }


                    // If the checkbox does not exist, add it to the listTypeOfFilterHome
                    if (!alreadyExists) {

                        // Inflate the layout containing the TextView and ImageView
                        LinearLayout filterItemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.item_filter_home_layout, null);

                        // Get references to the TextView and ImageView
                        TextView txtTypeOfFilterHome = filterItemLayout.findViewById(R.id.txt_type_of_filter_home);
                        ImageView iconCancel = filterItemLayout.findViewById(R.id.btn_cancel_filter_home);

                        // Set the text for the TextView
                        txtTypeOfFilterHome.setText(option);

                        // Optionally set an OnClickListener for the ImageView to remove the filter
                        iconCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Remove the filter
                                binding.listTypeOfFilterBill.removeView(filterItemLayout);

                                // Update SharedPreferences to uncheck the checkbox in the dialog
                                if (option.equals(cbx1.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_NOT_PAY_BILL, false);
                                    cbx1.setChecked(false);
                                } else if (option.equals(cbx2.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_PAYED_BILL, false);
                                    cbx2.setChecked(false);
                                } else if (option.equals(cbx3.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_DELAY_PAY_BILL, false);
                                    cbx3.setChecked(false);
                                } else if (option.equals(cbx4.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_IS_NOT_GIVE_BILL, false);
                                    cbx4.setChecked(false);
                                }

                                if (binding.listTypeOfFilterBill.getChildCount() == 0) {
                                    // If no filter left in the list -> close layout
                                    closeLayoutFilterBills();
                                    // And update list homes as initial
                                    //binding.recyclerView.setVisibility(View.VISIBLE);
                                    roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
                                        @Override
                                        public void onComplete(List<RoomBill> billList) {
                                            roomBillAdapter.setBillList(billList);
                                        }
                                    });
                                } else {
                                    // Update list homes after deleting some check boxes
                                    filterListHomes();
                                }

                            }
                        });

                        // Set layout parameters for filterItemLayout
                        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                (int) getResources().getDimension(R.dimen.filter_item_height) // Assuming filter_item_height is 40dp
                        );
                        int margin = (int) getResources().getDimension(R.dimen.filter_item_margin);
                        params.setMargins(0, margin, margin, 0);
                        filterItemLayout.setLayoutParams(params);


                        // Add the inflated layout to the main LinearLayout
                        binding.listTypeOfFilterBill.addView(filterItemLayout);
                    }
                }
                //dialog.dismiss();
            }
        });
    }

    private void closeLayoutFilterBills() {
        binding.btnFilterBill.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
        binding.frameRoundFilterBill.setBackground(getResources().getDrawable(R.drawable.background_delete_index_normal));
        binding.layoutTypeOfFilterBill.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    private void customizeButtonApplyInDialogHaveCheckBox(Button btnApply, List<AppCompatCheckBox> checkBoxList) {
        boolean isAnyChecked = false;
        for (AppCompatCheckBox checkBox : checkBoxList) {
            if (checkBox.isChecked()) {
                isAnyChecked = true;
                break;
            }
        }
        if (isAnyChecked) {
            btnApply.setEnabled(true);
            btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
        } else {
            btnApply.setEnabled(false);
            btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
        }
    }

    private void removeFromListAndSave(String option) { // Remove from listType
        for (int i = 0; i < binding.listTypeOfFilterBill.getChildCount(); i++) {
            View view = binding.listTypeOfFilterBill.getChildAt(i);
            if (view instanceof LinearLayout) {
                TextView textView = view.findViewById(R.id.txt_type_of_filter_home);
                if (textView.getText().toString().equals(option)) {
                    binding.listTypeOfFilterBill.removeView(view);
                    preferenceManager.removePreference(option);
                    break;
                }
            }
        }
    }

    private void filterListHomes() {
        showButtonLoading(R.id.btn_confirm_apply_bill);

        boolean filterByBill1 = preferenceManager.getBoolean(Constants.KEY_CBX_IS_NOT_PAY_BILL);
        boolean filterByBill2 = preferenceManager.getBoolean(Constants.KEY_CBX_IS_PAYED_BILL);
        boolean filterByBill3 = preferenceManager.getBoolean(Constants.KEY_CBX_IS_DELAY_PAY_BILL);
        boolean filterByBill4 = preferenceManager.getBoolean(Constants.KEY_CBX_IS_NOT_GIVE_BILL);

        List<RoomBill> filteredBills = new ArrayList<>();
        roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
            @Override
            public void onComplete(List<RoomBill> billList) {
                for (RoomBill bill : billList) {
                    boolean isNotPayBill = bill.isNotPayBill();
                    boolean isPayedBill = bill.isPayedBill();
                    boolean isDelayPayBill = bill.isDelayPayBill();
                    boolean isNotGiveBill = bill.isNotGiveBill();

                    if (filterByBill1 && isNotPayBill) {
                        filteredBills.add(bill);
                    } else if (filterByBill2 && isPayedBill) {
                        filteredBills.add(bill);
                    } else if (filterByBill3 && isDelayPayBill) {
                        filteredBills.add(bill);
                    } else if (filterByBill4 && isNotGiveBill) {
                        filteredBills.add(bill);
                    }
                }

                roomBillPresenter.filterBills(filteredBills);
            }
        });


    }

    private void setupDeleteManyBills() {
        binding.layoutDeleteBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnDeleteBill.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                binding.frameRoundDeleteBill.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
                roomBillAdapter.isDeleteChecked(true);
                binding.layoutDeleteManyBills.setVisibility(View.VISIBLE);
            }
        });

        binding.btnDeleteBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnDeleteBill.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                binding.frameRoundDeleteBill.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
                roomBillAdapter.isDeleteChecked(true);
                binding.layoutDeleteManyBills.setVisibility(View.VISIBLE);
            }
        });

        binding.checkboxSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectAllChecked = !isSelectAllChecked;
                roomBillAdapter.isSelectAllChecked(isSelectAllChecked);
            }
        });

        binding.txtDeleteBillHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<RoomBill> billList = roomBillAdapter.getSelectedBills();

                if (billList.isEmpty()) {
                    showToast("Không có hoá đơn nào được chọn");
                } else {
                    boolean cannotDelete = false;
                    for (RoomBill bill : billList) {
                        if (bill.isNotPayBill() || bill.isDelayPayBill() || (!bill.isNotPayBill() && !bill.isDelayPayBill() && !bill.isPayedBill())) {
                            cannotDelete = true;
                            break;
                        }
                    }

                    if (cannotDelete) {
                        showDialog(R.layout.layout_dialog_cannot_delete_bill);
                    } else {
                        openDialogConfirmDeleteListBills(billList);
                    }
                }
            }
        });

        binding.txtCancelDeleteBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLayoutDeleteBills();
            }
        });


    }

    private void openDialogConfirmDeleteListBills(List<RoomBill> billList) {
        setupDialog(R.layout.layout_dialog_confirm_delete_bill);

        dialog.findViewById(R.id.btn_confirm_delete_bill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomBillPresenter.deleteListBills(billList, new RoomBillPresenter.OnDeleteBillCompleteListener() {
                    @Override
                    public void onComplete() {
                        roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
                            @Override
                            public void onComplete(List<RoomBill> billList) {
                                closeLayoutDeleteBills();
                                roomBillAdapter.setBillList(billList);
                            }
                        });
                    }
                });
            }
        });

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void closeLayoutDeleteBills() {
        roomBillAdapter.isDeleteChecked(false);
        roomBillAdapter.isSelectAllChecked(false);
        binding.layoutDeleteManyBills.setVisibility(View.GONE);
        binding.btnDeleteBill.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
        binding.frameRoundDeleteBill.setBackground(getResources().getDrawable(R.drawable.background_delete_index_normal));
    }

    private void setupMonthPicker() {

        binding.btnCancelMonthPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.txtDateTime.setText("");
                binding.btnCancelMonthPicker.setVisibility(View.GONE);
                // Get list bill
                checkAndAddBillIfNeeded();
            }
        });


        binding.imgCalendar.setOnClickListener(v -> {
            visible = true;
            showMonthPicker(); // Open dialog
        });

    }

    private void showMonthPicker() {
        MonthPickerDialogCustom monthPickerDialogCustom = new MonthPickerDialogCustom(requireContext(), currentMonth, currentYear,
                new MonthPickerDialogCustom.OnMonthSelectedListener() {
                    @Override
                    public void onMonthSelected(int month, int year) {
                        date = month + "/" + year; // month = selectedMonthPosition + 1 ==> month == actual value
                        binding.txtDateTime.setText(date);
                        binding.btnCancelMonthPicker.setVisibility(View.VISIBLE);
                        roomBillPresenter.getBillByMonthYear(room, month, year, new RoomBillPresenter.OnGetBillByMonthYearCompleteListener() {
                            @Override
                            public void onComplete(List<RoomBill> billList) {
                                roomBillAdapter.setBillList(billList);
                            }
                        });
                        currentMonth = month - 1; // Cập nhật currentMonth, have to minus 1
                        currentYear = year; // Cập nhật year
                    }

                    @Override
                    public void onCancel() {
                        visible = false;
                    }
                });

        Objects.requireNonNull(monthPickerDialogCustom.getWindow()).setBackgroundDrawableResource(R.drawable.background_dialog_index);
        monthPickerDialogCustom.show();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setAdapter(roomBillAdapter);
    }




    @Override
    public void makeBillClick(RoomBill bill) {
        if (onMakeBillClickListener != null) {
            onMakeBillClickListener.onMakeBillClicked(bill);
        }

    }

    @Override
    public void openPopUp(View view, RoomBill bill, ItemContainerInformationOfBillBinding binding2) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_view_bill) {
                if(onViewBillListener != null) {
                    onViewBillListener.onViewBill(bill);
                }
                return true;
            } else if (itemId == R.id.menu_edit_bill) {
                showToast("edit bill");
                return true;
            } else if (itemId == R.id.menu_delete_bill) {
                if (bill.isNotPayBill() || bill.isDelayPayBill()) {
                    showDialog(R.layout.layout_dialog_cannot_delete_bill);
                    return true;
                }

                if (!bill.isPayedBill() && !bill.isDelayPayBill() && !bill.isNotPayBill()) {;
                    showDialog(R.layout.layout_dialog_cannot_delete_bill);
                    return true;
                }

                openDialogConfirmDeleteBill(bill);
                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                binding2.imgCircleMenu.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            }
        });


        popupMenu.inflate(R.menu.menu_bill_room);
        popupMenu.show();
    }

    private void openDialogConfirmDeleteBill(RoomBill bill) {
        setupDialog(R.layout.layout_dialog_confirm_delete_bill);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btn_confirm_delete_bill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomBillPresenter.deleteBill(bill, new RoomBillPresenter.OnDeleteBillCompleteListener() {
                    @Override
                    public void onComplete() {
                        roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
                            @Override
                            public void onComplete(List<RoomBill> billList) {
                                roomBillAdapter.setBillList(billList);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLayoutNoData() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLayoutNoData() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDialog(int id) {
        setupDialog(id);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


    }

    public interface OnMakeBillClickListener {
        void onMakeBillClicked(RoomBill bill);
    }

    public interface OnViewBillListener {
        void onViewBill(RoomBill bill);
    }

    public void setOnMakeBillClickListener(OnMakeBillClickListener listener) {
        this.onMakeBillClickListener = listener;
    }

    public OnViewBillListener getOnViewBillListener() {
        return onViewBillListener;
    }

    public void setOnViewBillListener(OnViewBillListener onViewBillListener) {
        this.onViewBillListener = onViewBillListener;
    }

    private void setupDialog(int layoutId) {
        dialog.setContentView(layoutId);
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
    }

    @Override
    public void showButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }

    @Override
    public void closeDialog() {
        dialog.dismiss();
    }

    public void removeStatusOfCheckBoxFilterBill() {
        preferenceManager.removePreference(Constants.KEY_CBX_IS_NOT_PAY_BILL);
        preferenceManager.removePreference(Constants.KEY_CBX_IS_PAYED_BILL);
        preferenceManager.removePreference(Constants.KEY_CBX_IS_DELAY_PAY_BILL);
        preferenceManager.removePreference(Constants.KEY_CBX_IS_NOT_GIVE_BILL);
    }




}
