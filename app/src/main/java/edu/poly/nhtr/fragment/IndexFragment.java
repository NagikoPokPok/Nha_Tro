package edu.poly.nhtr.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.poly.nhtr.Activity.MonthPickerDialogCustom;
import edu.poly.nhtr.Adapter.IndexAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.alarmManager.AlarmService;
import edu.poly.nhtr.databinding.FragmentIndexBinding;
import edu.poly.nhtr.databinding.LayoutDialogDeleteHomeSuccessBinding;
import edu.poly.nhtr.databinding.LayoutDialogDeleteIndexBinding;
import edu.poly.nhtr.databinding.LayoutDialogDetailedIndexBinding;
import edu.poly.nhtr.databinding.LayoutDialogFilterIndexBinding;
import edu.poly.nhtr.databinding.LayoutDialogNoteIndexBinding;
import edu.poly.nhtr.databinding.LayoutDialogSettingNotificationIndexBinding;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.IndexPresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexFragment extends Fragment implements IndexInterface, SwipeRefreshLayout.OnRefreshListener {
    private IndexPresenter indexPresenter;
    private PreferenceManager preferenceManager;
    private FragmentIndexBinding binding;
    private List<Index> list_index;

    private List<Index> currentListIndexes = new ArrayList<>();

    private boolean isNextClicked = false; // Track if Next button has been clicked
    private boolean isCheckBoxClicked = false;

    IndexAdapter adapter;
    int currentPage = 0;
    int totalPages = 12;
    private boolean visible = true; // Check dialog is open or not
    private String date = ""; // Show month/year
    private int currentMonth;
    private int currentYear;
    private String homeID;

    private Dialog dialog;
    private View view;
    private List<Index> filteredIndexList = new ArrayList<>();
    private boolean isElectricityIndexOldAscending = false;
    private boolean isElectricityIndexNewAscending = false;
    private boolean isWaterIndexOldAscending = false;
    private boolean isWaterIndexNewAscending = false;
    private boolean isNameRoomAscending = false;

    private boolean waterIsIndex = false;

    private AlarmService alarmService;
    private String header, body;
    private Room room;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentIndexBinding.inflate(getLayoutInflater());
        dialog = new Dialog(requireActivity());

        assert getArguments() != null;
        Home home = (Home) getArguments().getSerializable("home");
        assert home != null;
        homeID = home.getIdHome();

        //alarmService = new AlarmService(requireContext(), home, header, body);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_index, container, false);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());


        assert getArguments() != null;
        Home home = (Home) getArguments().getSerializable("home");
        assert home != null;
        homeID = home.getIdHome();
        indexPresenter = new IndexPresenter(this, homeID);

        String header = "Nhập chỉ số cho nhà trọ " + home.getNameHome();
        String body = "Hôm nay là ngày bạn cần nhập thông tin chỉ số cho tất cả các phòng ở nhà trọ " + home.getNameHome();

        alarmService = new AlarmService(requireContext(), home, room, header, body);


        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        checkWaterIsIndexOrNot();

        binding.swipeRefreshFragment.setOnRefreshListener(this);


        return binding.getRoot();
    }


    public void checkWaterIsIndexOrNot() {
        indexPresenter.checkWaterIsIndexOrNot(new IndexPresenter.OnCheckWaterIsIndexCompleteListener() {
            @Override
            public void onComplete(boolean isWaterIndex) {
                waterIsIndex = isWaterIndex;


                indexPresenter.fetchRoomsAndAddIndex(homeID, isWaterIndex, task1 -> {
                    indexPresenter.updateWaterIsIndex(homeID, currentMonth + 1, currentYear, isWaterIndex, task2 -> {
                        setupLayout(homeID, currentMonth + 1, currentYear);
                    });

                });


                removeStatusOfCheckBoxFilterIndex();
                setupRecyclerView();


                //setupPagination();
                setupDeleteRows();
                setupMonthPicker();
                setupSortIndexes();
                setupFilterIndexes();
                setupSearchEditText(binding.edtSearchIndex);
                setupAlarmService();

            }
        });
    }

    @Override
    public void onRefresh() {
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        // Get index
        indexPresenter.checkWaterIsIndexOrNot(new IndexPresenter.OnCheckWaterIsIndexCompleteListener() {
            @Override
            public void onComplete(boolean isWaterIndex) {
                waterIsIndex = isWaterIndex;
                indexPresenter.fetchRoomsAndAddIndex(homeID, isWaterIndex, task1 -> {
                    indexPresenter.updateWaterIsIndex(homeID, currentMonth + 1, currentYear, isWaterIndex, task2 -> {
                        setupLayout(homeID, currentMonth + 1, currentYear);
                    });

                });

            }
        });

        //Delete layout delete index
        closeLayoutDeleteManyRows();

        // Delete layout filter index
        closeLayoutFilterIndexes();

        // Set date time default
        String monthYear = (currentMonth + 1) + "/" + currentYear;
        binding.txtDateTime.setText(monthYear);

        // Delete layout search index
        binding.edtSearchIndex.clearFocus();

        removeStatusOfCheckBoxFilterIndex();


        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.swipeRefreshFragment.setRefreshing(false);
            }
        }, 2000);
    }

    private void setupAlarmService() {
        LayoutDialogSettingNotificationIndexBinding binding1 = LayoutDialogSettingNotificationIndexBinding.inflate(getLayoutInflater());
        //binding.setExact.setOnClickListener(v -> setAlarm(alarmService::setExactAlarm, binding1));
        //binding.setRepetitive.setOnClickListener(v -> setAlarm(alarmService::setRepetitiveAlarm, binding1));
        binding.btnSettingNotificationIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettingNotificationDialog();
            }
        });
    }

    private void openSettingNotificationDialog() {

        LayoutDialogSettingNotificationIndexBinding binding1 = LayoutDialogSettingNotificationIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding1.getRoot());
        setupDialogWindow(binding1.getRoot().getLayoutParams());

        binding1.switchOnOffNotification.setChecked(preferenceManager.getBoolean(Constants.SWITCH_ON_OFF_NOTIFICATION_INDEX, homeID));
        if (binding1.switchOnOffNotification.isChecked()) {
            binding1.layoutSelectDay.setVisibility(View.VISIBLE);
            binding1.edtDay.setText(preferenceManager.getString(Constants.KEY_DATE_PUSH_NOTIFICATION_INDEX, homeID));
        } else {
            binding1.layoutSelectDay.setVisibility(View.GONE);
        }

        binding1.edtDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isDateAndTimeSelected(s.toString())) {
                    binding1.layoutDay.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding1.btnAddHome.setOnClickListener(v -> {
            if (preferenceManager.getBoolean(Constants.SWITCH_ON_OFF_NOTIFICATION_INDEX, homeID)) {
                if (isDateAndTimeSelected(Objects.requireNonNull(binding1.edtDay.getText()).toString())) {
                    preferenceManager.putBoolean(Constants.SWITCH_ON_OFF_NOTIFICATION_INDEX, binding1.switchOnOffNotification.isChecked(), homeID);
                    preferenceManager.putString(Constants.KEY_DATE_PUSH_NOTIFICATION_INDEX, binding1.edtDay.getText().toString(), homeID);
                    dialog.dismiss();
                } else {
                    showErrorMessage("Hãy chọn đủ ngày và giờ", R.id.layout_day);
                }
            } else {
                dialog.dismiss();
            }
        });

        binding1.switchOnOffNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setOnNotification(binding1);
                } else {
                    setOffNotification(binding1);
                }
            }
        });

        binding1.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        binding1.btnCalendarSelectDayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarm(alarmService::setRepetitiveAlarm, binding1);
            }
        });


        dialog.setCancelable(true);
        dialog.show();

    }

    private boolean isDateAndTimeSelected(String dateTime) {
        // Basic check to ensure both date and time are present
        return dateTime.matches(".*\\d{2}:\\d{2}$");
    }

    private void setOnNotification(LayoutDialogSettingNotificationIndexBinding binding1) {
        binding1.layoutSelectDay.setVisibility(View.VISIBLE);
    }

    private void setOffNotification(LayoutDialogSettingNotificationIndexBinding binding1) {
        binding1.layoutSelectDay.setVisibility(View.GONE);
        binding1.btnAddHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferenceManager.putBoolean(Constants.SWITCH_ON_OFF_NOTIFICATION_INDEX, false, homeID);
                int requestCode = Integer.parseInt(preferenceManager.getString(Constants.KEY_NOTIFICATION_REQUEST_CODE, homeID));
                alarmService.cancelRepetitiveAlarm(requestCode);
                dialog.dismiss();
            }
        });
    }


    private void setAlarm(AlarmCallback callback, LayoutDialogSettingNotificationIndexBinding binding1) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        new DatePickerDialog(
                requireContext(),
                0,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Định dạng ngày thành hai chữ số
                    String formattedDay = String.format("%02d", dayOfMonth);
                    String formattedMonth = String.format("%02d", month + 1);

                    binding1.edtDay.setText("ngày " + formattedDay + " vào lúc ");

                    new TimePickerDialog(
                            requireContext(),
                            0,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);


                                // Định dạng giờ và phút thành hai chữ số
                                String formattedHour = String.format("%02d", hourOfDay);
                                String formattedMinute = String.format("%02d", minute);

                                String hourMinute = formattedHour + ":" + formattedMinute;
                                binding1.edtDay.append(hourMinute);

                                preferenceManager.putString(Constants.KEY_DATE_PUSH_NOTIFICATION_INDEX, Objects.requireNonNull(binding1.edtDay.getText()).toString().trim()
                                        , homeID);

                                pushAlarm(binding1, callback, calendar);

                                // Gọi callback nếu cần thiết
                                //callback.onAlarmSet(calendar.getTimeInMillis());
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false // Đặt thành true để sử dụng định dạng 24 giờ
                    ).show();

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private int generateRandomRequestCode() {
        Random random = new Random();
        return random.nextInt(1000000); // Giới hạn số ngẫu nhiên trong khoảng 0 đến 9999
    }


    private void pushAlarm(LayoutDialogSettingNotificationIndexBinding binding1, AlarmCallback callback, Calendar calendar) {
        binding1.btnAddHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferenceManager.putBoolean(Constants.SWITCH_ON_OFF_NOTIFICATION_INDEX, true, homeID);
                int requestCode;
                String requestCodeStr = preferenceManager.getString(Constants.KEY_NOTIFICATION_REQUEST_CODE, homeID);
                if(requestCodeStr==null){
                    requestCode = generateRandomRequestCode();
                }else{
                    requestCode = Integer.parseInt(requestCodeStr);
                }
                preferenceManager.putString(Constants.KEY_NOTIFICATION_REQUEST_CODE, String.valueOf(requestCode), homeID);

                callback.onAlarmSet(calendar.getTimeInMillis(), requestCode);
                dialog.dismiss();
            }
        });

    }


    private interface AlarmCallback {
        void onAlarmSet(long timeInMillis, int requestCode);
    }

    private void showRowInTable(boolean waterIsIndex) {
        if (waterIsIndex) {
            binding.btnPrevious.setEnabled(false); // Disable previous button initially
            binding.btnNext.setEnabled(true);
            binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTextBlack)));

            binding.btnNext.setOnClickListener(v -> {
                if (!isNextClicked) {
                    binding.layoutElectricityIndexOld.setVisibility(View.GONE);
                    binding.layoutElectricityIndexNew.setVisibility(View.GONE);
                    binding.layoutWaterIndexOld.setVisibility(View.VISIBLE);
                    binding.layoutWaterIndexNew.setVisibility(View.VISIBLE);
                    isNextClicked = true;
                    adapter.setNextClicked(true);

                    updateButtonsState();
                }
            });

            binding.btnPrevious.setOnClickListener(v -> {
                if (isNextClicked) {
                    binding.layoutWaterIndexOld.setVisibility(View.GONE);
                    binding.layoutWaterIndexNew.setVisibility(View.GONE);
                    binding.layoutElectricityIndexOld.setVisibility(View.VISIBLE);
                    binding.layoutElectricityIndexNew.setVisibility(View.VISIBLE);
                    isNextClicked = false;
                    adapter.setNextClicked(false);
                    updateButtonsState();
                }
            });
        } else {
            binding.btnPrevious.setEnabled(false);
            binding.btnNext.setEnabled(false);
            binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            binding.btnPrevious.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));

            binding.layoutElectricityIndexOld.setVisibility(View.VISIBLE);
            binding.layoutElectricityIndexNew.setVisibility(View.VISIBLE);
            binding.layoutWaterIndexOld.setVisibility(View.GONE);
            binding.layoutWaterIndexNew.setVisibility(View.GONE);
            isNextClicked = false;
            adapter.setNextClicked(false);

        }
    }

    private void setupLayout(String homeID, int month, int year) {

        indexPresenter.checkWaterIsIndexByMonthYearOrNot(homeID, month, year, new IndexPresenter.OnCheckWaterIsIndexByMonthYearCompleteListener() {
            @Override
            public void onComplete(boolean isWaterIndex) {
                waterIsIndex = isWaterIndex;
                if (isAdded2()) {
                    showRowInTable(isWaterIndex);
                }
            }
        });

    }


    public List<Index> getCurrentListIndexes() {
        return currentListIndexes;
    }

    public void setCurrentListIndexes(List<Index> currentListIndexes) {
        this.currentListIndexes = currentListIndexes;
    }

    private void setupFilterIndexes() {
        String dateNow = binding.txtDateTime.getText().toString();
        String[] parts = dateNow.split("/");
        String month = parts[0];
        String year = parts[1];
        binding.btnFilterIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnFilterIndex.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                binding.frameRoundFilterIndex.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
                indexPresenter.getCurrentListIndex(homeID, Integer.parseInt(month), Integer.parseInt(year), new IndexPresenter.OnGetCurrentListIndexes() {
                    @Override
                    public void onComplete(List<Index> indexList) {
                        openFilterIndexDialog(indexList);
                    }
                });
            }
        });
    }

    private void closeLayoutFilterIndexes() {
        binding.btnFilterIndex.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
        binding.frameRoundFilterIndex.setBackground(getResources().getDrawable(R.drawable.background_delete_index_normal));
        binding.layoutTypeOfFilterIndex.setVisibility(View.GONE);
        binding.layoutNoData.setVisibility(View.GONE);
    }

    private void openFilterIndexDialog(List<Index> indexList) {
        String dateNow = binding.txtDateTime.getText().toString();
        String[] parts = dateNow.split("/");
        String month = parts[0];
        String year = parts[1];

        LayoutDialogFilterIndexBinding binding1 = LayoutDialogFilterIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding1.getRoot());
        setupDialogWindow(binding1.getRoot().getLayoutParams());

        if (!waterIsIndex) {
            binding1.txtWaterIndex.setVisibility(View.GONE);
            binding1.cbxFrom0To4M3.setVisibility(View.GONE);
            binding1.cbxFrom4To8M3.setVisibility(View.GONE);
            binding1.cbxFrom8ToMoreM3.setVisibility(View.GONE);
        }
        dialog.setCancelable(true);
        dialog.show();


        AppCompatCheckBox cbxByWater1 = binding1.cbxFrom0To4M3;
        AppCompatCheckBox cbxByWater2 = binding1.cbxFrom4To8M3;
        AppCompatCheckBox cbxByWater3 = binding1.cbxFrom8ToMoreM3;

        int range1Max, range2Max, range3Max;

        // Bước 1: Tìm số lượng phòng lớn nhất
        int maxIndex = 0;
        for (Index index : indexList) {
            int indexOfUsed = Integer.parseInt(index.getElectricityIndexNew()) - Integer.parseInt(index.getElectricityIndexOld());
            if (indexOfUsed > maxIndex) {
                maxIndex = indexOfUsed;
            }
        }

        // Bước 2: Chia số lượng phòng lớn nhất thành ba khoảng

        if (maxIndex == 0) {
            range1Max = range2Max = range3Max = 0;
        } else {
            range1Max = (int) Math.ceil((double) maxIndex / 3.0);  // Kích thước mỗi khoảng
            range2Max = 2 * range1Max;
            range3Max = maxIndex;
        }

        // Bước 3: Thiết lập văn bản và hiển thị các checkbox
        binding1.cbxFrom0To150KWh.setText("Từ " + 0 + " - " + range1Max + " kWh");

        if (range2Max > range1Max) {
            if ((range1Max + 1) != range2Max) {
                binding1.cbxFrom150To250KWh.setText("Từ " + (range1Max + 1) + " - " + range2Max + " kWh");
            } else {
                binding1.cbxFrom150To250KWh.setText("Từ " + (range1Max + 1) + " kWh");
            }
            binding1.cbxFrom150To250KWh.setVisibility(View.VISIBLE);
        } else {
            binding1.cbxFrom150To250KWh.setVisibility(View.GONE);
        }

        if (range3Max > range2Max) {
            if ((range2Max + 1) != range3Max) {
                binding1.cbxFrom250ToMoreKWh.setText("Từ " + (range2Max + 1) + " - " + range3Max + " kWh");
            } else {
                binding1.cbxFrom250ToMoreKWh.setText("Từ " + (range2Max + 1) + " kWh");
            }
            binding1.cbxFrom250ToMoreKWh.setVisibility(View.VISIBLE);
        } else {
            binding1.cbxFrom250ToMoreKWh.setVisibility(View.GONE);
        }


        int range4Max, range5Max, range6Max;

        // Bước 1: Tìm số lượng phòng lớn nhất
        int maxIndexOfWater = 0;
        for (Index index : indexList) {
            int indexOfUsed = Integer.parseInt(index.getWaterIndexNew()) - Integer.parseInt(index.getWaterIndexOld());
            if (indexOfUsed > maxIndexOfWater) {
                maxIndexOfWater = indexOfUsed;
            }
        }

        // Bước 2: Chia số lượng phòng lớn nhất thành ba khoảng

        if (maxIndexOfWater == 0) {
            range4Max = range5Max = range6Max = 0;
        } else {
            range4Max = (int) Math.ceil((double) maxIndexOfWater / 3.0);  // Kích thước mỗi khoảng
            range5Max = 2 * range4Max;
            range6Max = maxIndexOfWater;
        }

        // Bước 3: Thiết lập văn bản và hiển thị các checkbox
        cbxByWater1.setText("Từ " + 0 + " - " + range4Max + " Khối");

        if (range5Max > range4Max) {
            if ((range4Max + 1) != range5Max) {
                cbxByWater2.setText("Từ " + (range4Max + 1) + " - " + range5Max + " Khối");
            } else {
                cbxByWater2.setText("Từ " + (range4Max + 1) + " Khối");
            }
            cbxByWater2.setVisibility(View.VISIBLE);
        } else {
            cbxByWater2.setVisibility(View.GONE);
        }

        if (range6Max > range5Max) {
            if ((range5Max + 1) != range6Max) {
                cbxByWater3.setText("Từ " + (range5Max + 1) + " - " + range6Max + " Khối");
            } else {
                cbxByWater3.setText("Từ " + (range5Max + 1) + " Khối");
            }
            cbxByWater3.setVisibility(View.VISIBLE);
        } else {
            cbxByWater3.setVisibility(View.GONE);
        }


        cbxByWater1.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_BY_WATER_INDEX_1));
        cbxByWater2.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_BY_WATER_INDEX_2));
        cbxByWater3.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_BY_WATER_INDEX_3));


        binding1.cbxFrom0To150KWh.setChecked(preferenceManager.getBoolean("cbxByElectricityIndex1"));
        binding1.cbxFrom150To250KWh.setChecked(preferenceManager.getBoolean("cbxByElectricityIndex2"));
        binding1.cbxFrom250ToMoreKWh.setChecked(preferenceManager.getBoolean("cbxByElectricityIndex3"));


        // Add CheckBoxes to a list
        List<AppCompatCheckBox> checkBoxList = new ArrayList<>();
        checkBoxList.add(binding1.cbxFrom0To150KWh);
        checkBoxList.add(binding1.cbxFrom150To250KWh);
        checkBoxList.add(binding1.cbxFrom250ToMoreKWh);
        if (waterIsIndex) {
            checkBoxList.add(cbxByWater1);
            checkBoxList.add(cbxByWater2);
            checkBoxList.add(cbxByWater3);
        }

        customizeButtonApplyInDialogHaveCheckBox(binding1.btnConfirmApply, checkBoxList);

        // Create a method to check the state of all checkboxes
        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeButtonApplyInDialogHaveCheckBox(binding1.btnConfirmApply, checkBoxList);
            }
        };

        // Set the listener to all checkboxes
        binding1.cbxFrom0To150KWh.setOnClickListener(checkBoxListener);
        binding1.cbxFrom150To250KWh.setOnClickListener(checkBoxListener);
        binding1.cbxFrom250ToMoreKWh.setOnClickListener(checkBoxListener);
        if (waterIsIndex) {
            cbxByWater1.setOnClickListener(checkBoxListener);
            cbxByWater2.setOnClickListener(checkBoxListener);
            cbxByWater3.setOnClickListener(checkBoxListener);
        }

        binding1.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLayoutFilterIndexes();
                dialog.dismiss();
            }
        });

        binding1.btnConfirmApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutTypeOfFilterIndex.setVisibility(View.VISIBLE);
                List<String> selectedOptions = new ArrayList<>();

                // Check which CheckBoxes are selected and save their state
                boolean filterByIndex1 = binding1.cbxFrom0To150KWh.isChecked();
                boolean filterByIndex2 = binding1.cbxFrom150To250KWh.isChecked();
                boolean filterByIndex3 = binding1.cbxFrom250ToMoreKWh.isChecked();
                boolean filterByWaterIndex1 = cbxByWater1.isChecked();
                boolean filterByWaterIndex2 = cbxByWater2.isChecked();
                boolean filterByWaterIndex3 = cbxByWater3.isChecked();


                if (filterByIndex1) {
                    selectedOptions.add(binding1.cbxFrom0To150KWh.getText().toString());
                    preferenceManager.putBoolean("cbxByElectricityIndex1", true);
                } else {
                    preferenceManager.putBoolean("cbxByElectricityIndex1", false);
                    removeFromListAndSave(binding1.cbxFrom0To150KWh.getText().toString());
                }
                if (filterByIndex2) {
                    selectedOptions.add(binding1.cbxFrom150To250KWh.getText().toString());
                    preferenceManager.putBoolean("cbxByElectricityIndex2", true);
                } else {
                    preferenceManager.putBoolean("cbxByElectricityIndex2", false);
                    removeFromListAndSave(binding1.cbxFrom150To250KWh.getText().toString());
                }
                if (filterByIndex3) {
                    selectedOptions.add(binding1.cbxFrom250ToMoreKWh.getText().toString());
                    preferenceManager.putBoolean("cbxByElectricityIndex3", true);
                } else {
                    preferenceManager.putBoolean("cbxByElectricityIndex3", false);
                    removeFromListAndSave(binding1.cbxFrom250ToMoreKWh.getText().toString());
                }
                if (filterByWaterIndex1) {
                    selectedOptions.add(cbxByWater1.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_1, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_1, false);
                    removeFromListAndSave(cbxByWater1.getText().toString());
                }
                if (filterByWaterIndex2) {
                    selectedOptions.add(cbxByWater2.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_2, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_2, false);
                    removeFromListAndSave(cbxByWater2.getText().toString());
                }
                if (filterByWaterIndex3) {
                    selectedOptions.add(cbxByWater3.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_3, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_3, false);
                    removeFromListAndSave(cbxByWater3.getText().toString());
                }

                // If 3 check boxes are unchecked -> Hide layoutTypeOfFilterHomes
                if (!filterByIndex1 && !filterByIndex2 && !filterByIndex3 && !filterByWaterIndex1 && !filterByWaterIndex2 && !filterByWaterIndex3) {
                    closeLayoutFilterIndexes();
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    indexPresenter.fetchIndexesByMonthAndYear(homeID, Integer.parseInt(month), Integer.parseInt(year), "init");
                } else {
                    filterListHomes(range1Max, range2Max, range3Max, range4Max, range5Max, range6Max); // After put status of checkboxes in preferences, check and add them into the list
                }

                // Add selected options as LinearLayouts with TextView and ImageView to the main LinearLayout
                for (String option : selectedOptions) {
                    // Check if the checkbox is already in the listTypeOfFilterHome
                    boolean alreadyExists = false;
                    for (int i = 0; i < binding.listTypeOfFilterIndex.getChildCount(); i++) {
                        View view = binding.listTypeOfFilterIndex.getChildAt(i);
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
                                binding.listTypeOfFilterIndex.removeView(filterItemLayout);

                                // Update SharedPreferences to uncheck the checkbox in the dialog
                                if (option.equals(binding1.cbxFrom0To150KWh.getText().toString())) {
                                    preferenceManager.putBoolean("cbxByElectricityIndex1", false);
                                    binding1.cbxFrom0To150KWh.setChecked(false);
                                } else if (option.equals(binding1.cbxFrom150To250KWh.getText().toString())) {
                                    preferenceManager.putBoolean("cbxByElectricityIndex2", false);
                                    binding1.cbxFrom150To250KWh.setChecked(false);
                                } else if (option.equals(binding1.cbxFrom250ToMoreKWh.getText().toString())) {
                                    preferenceManager.putBoolean("cbxByElectricityIndex3", false);
                                    binding1.cbxFrom250ToMoreKWh.setChecked(false);
                                } else if (option.equals(cbxByWater1.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_1, false);
                                    cbxByWater1.setChecked(false);
                                } else if (option.equals(cbxByWater2.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_2, false);
                                    cbxByWater2.setChecked(false);
                                } else if (option.equals(cbxByWater3.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_BY_WATER_INDEX_3, false);
                                    cbxByWater3.setChecked(false);
                                }

                                if (binding.listTypeOfFilterIndex.getChildCount() == 0) {
                                    // If no filter left in the list -> Set GONE
                                    closeLayoutFilterIndexes();
                                    // And update list homes as initial
                                    //binding.recyclerView.setVisibility(View.VISIBLE);
                                    indexPresenter.fetchIndexesByMonthAndYear(homeID, Integer.parseInt(month), Integer.parseInt(year), "init");
                                } else {
                                    // Update list homes after deleting some check boxes
                                    filterListHomes(range1Max, range2Max, range3Max, range4Max, range5Max, range6Max);
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
                        binding.listTypeOfFilterIndex.addView(filterItemLayout);
                    }
                }
                //dialog.dismiss();
            }
        });


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
        for (int i = 0; i < binding.listTypeOfFilterIndex.getChildCount(); i++) {
            View view = binding.listTypeOfFilterIndex.getChildAt(i);
            if (view instanceof LinearLayout) {
                TextView textView = view.findViewById(R.id.txt_type_of_filter_home);
                if (textView.getText().toString().equals(option)) {
                    binding.listTypeOfFilterIndex.removeView(view);
                    preferenceManager.removePreference(option);
                    break;
                }
            }
        }
    }

    private void filterListHomes(int range1, int range2, int range3, int range4, int range5, int range6) {
        showButtonLoading(R.id.btn_confirm_apply);
        boolean filterByRoom1 = preferenceManager.getBoolean("cbxByElectricityIndex1");
        boolean filterByRoom2 = preferenceManager.getBoolean("cbxByElectricityIndex2");
        boolean filterByRoom3 = preferenceManager.getBoolean("cbxByElectricityIndex3");
        boolean filterByWater1 = preferenceManager.getBoolean(Constants.KEY_CBX_BY_WATER_INDEX_1);
        boolean filterByWater2 = preferenceManager.getBoolean(Constants.KEY_CBX_BY_WATER_INDEX_2);
        boolean filterByWater3 = preferenceManager.getBoolean(Constants.KEY_CBX_BY_WATER_INDEX_3);

        List<Index> filterIndexesByElectricity = new ArrayList<Index>();
        List<Index> filterIndexesByWater = new ArrayList<Index>();
        List<Index> filteredIndexes = new ArrayList<>();

        if (filterByRoom1 || filterByRoom2 || filterByRoom3) {
            for (Index index : currentListIndexes) {
                String indexElectricityOld = index.getElectricityIndexOld();
                String indexElectricityNew = index.getElectricityIndexNew();

                int electricityIndexUsed = Integer.parseInt(indexElectricityNew) - Integer.parseInt(indexElectricityOld);

                if (filterByRoom1 && electricityIndexUsed >= 0 && electricityIndexUsed <= range1) {
                    filterIndexesByElectricity.add(index);
                } else if (filterByRoom2 && electricityIndexUsed >= (range1 + 1) && electricityIndexUsed <= range2) {
                    filterIndexesByElectricity.add(index);
                } else if (filterByRoom3 && electricityIndexUsed >= (range2 + 1) && electricityIndexUsed <= range3) {
                    filterIndexesByElectricity.add(index);
                }
            }
        }

        if (filterByWater1 || filterByWater2 || filterByWater3) {
            for (Index index : currentListIndexes) {
                String indexWaterOld = index.getWaterIndexOld();
                String indexWaterNew = index.getWaterIndexNew();

                int waterIndexUsed = Integer.parseInt(indexWaterNew) - Integer.parseInt(indexWaterOld);

                if (filterByWater1 && waterIndexUsed >= 0 && waterIndexUsed <= range4) {
                    filterIndexesByWater.add(index);
                } else if (filterByWater2 && waterIndexUsed >= (range4+1) && waterIndexUsed <= range5) {
                    filterIndexesByWater.add(index);
                } else if (filterByWater3 && waterIndexUsed >= (range5+1) && waterIndexUsed <= range6) {
                    filterIndexesByWater.add(index);
                }
            }

        }

        // Kết hợp kết quả lọc
        if ((filterByRoom1 || filterByRoom2 || filterByRoom3) && (filterByWater1 || filterByWater2 || filterByWater3)) {
            if (!filterIndexesByElectricity.isEmpty() && !filterIndexesByWater.isEmpty()) {
                Set<Index> setElectricity = new HashSet<>(filterIndexesByElectricity);
                Set<Index> setWater = new HashSet<>(filterIndexesByWater);
                setElectricity.retainAll(setWater); // Giao của hai tập hợp
                filteredIndexes.addAll(setElectricity);
            }
            // Nếu một trong hai danh sách rỗng, kết quả cuối cùng sẽ là rỗng
        } else {
            if (!filterIndexesByElectricity.isEmpty()) {
                filteredIndexes.addAll(filterIndexesByElectricity);
            }
            if (!filterIndexesByWater.isEmpty()) {
                filteredIndexes.addAll(filterIndexesByWater);
            }
        }


        indexPresenter.filterIndexes(filteredIndexes);

    }

    private void setDefaultBackground(ImageButton img) {
        img.setBackground(getResources().getDrawable(R.drawable.ic_two_arrows));
        img.setRotation(90);
        img.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
    }

    private void setAscendingBackground(ImageButton img) {
        img.setBackground(getResources().getDrawable(R.drawable.ic_arrow_right));
        img.setRotation(270);
        img.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
    }

    private void setDescendingBackground(ImageButton img) {
        img.setBackground(getResources().getDrawable(R.drawable.ic_arrow_right));
        img.setRotation(90);
        img.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
    }

    private void setupSortIndexes() {
        binding.imgTwoArrowsElectricityIndexOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllIcons();
                toggleSorting(binding.imgTwoArrowsElectricityIndexOld, "electricityIndexOld");
            }
        });

        binding.imgTwoArrowsElectricityIndexNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllIcons();
                toggleSorting(binding.imgTwoArrowsElectricityIndexNew, "electricityIndexNew");
            }
        });

        binding.imgTwoArrowsWaterIndexOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllIcons();
                toggleSorting(binding.imgTwoArrowsWaterIndexOld, "waterIndexOld");
            }
        });

        binding.imgTwoArrowsWaterIndexNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllIcons();
                toggleSorting(binding.imgTwoArrowsWaterIndexNew, "waterIndexNew");
            }
        });

        binding.imgTwoArrowsNameRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllIcons();
                toggleSorting(binding.imgTwoArrowsNameRoom, "nameRoom");
            }
        });
    }

    private void resetAllIcons() {
        setDefaultBackground(binding.imgTwoArrowsElectricityIndexOld);
        setDefaultBackground(binding.imgTwoArrowsElectricityIndexNew);
        setDefaultBackground(binding.imgTwoArrowsWaterIndexOld);
        setDefaultBackground(binding.imgTwoArrowsWaterIndexNew);
        setDefaultBackground(binding.imgTwoArrowsNameRoom);
    }

    private void toggleSorting(ImageButton img, String indexType) {
        boolean isAscending;
        switch (indexType) {
            case "electricityIndexOld":
                isElectricityIndexOldAscending = !isElectricityIndexOldAscending;
                isAscending = isElectricityIndexOldAscending;
                break;
            case "electricityIndexNew":
                isElectricityIndexNewAscending = !isElectricityIndexNewAscending;
                isAscending = isElectricityIndexNewAscending;
                break;
            case "waterIndexOld":
                isWaterIndexOldAscending = !isWaterIndexOldAscending;
                isAscending = isWaterIndexOldAscending;
                break;
            case "waterIndexNew":
                isWaterIndexNewAscending = !isWaterIndexNewAscending;
                isAscending = isWaterIndexNewAscending;
                break;
            case "nameRoom":
                isNameRoomAscending = !isNameRoomAscending;
                isAscending = isNameRoomAscending;
                break;
            default:
                throw new IllegalArgumentException("Unknown index type");
        }

        if (isAscending) {
            setAscendingBackground(img);
        } else {
            setDescendingBackground(img);
        }

        String dateNow = binding.txtDateTime.getText().toString();
        String[] parts = dateNow.split("/");
        String month = parts[0];
        String year = parts[1];

        indexPresenter.fetchIndexesByMonthAndYear(homeID, Integer.parseInt(month), Integer.parseInt(year),
                indexType + (isAscending ? "Ascending" : "Descending"));
    }

    public List<Index> getList_index() {
        return list_index;
    }

    public void setList_index(List<Index> list_index) {
        this.list_index = list_index;
    }

    private void setupSearchEditText(TextInputEditText edtSearch) {

        edtSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.layoutSearchIndex.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    binding.layoutSearchIndex.setBoxStrokeColor(getResources().getColor(R.color.colorGray));
                }
            }
        });
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterIndexes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    private void filterIndexes(String query) {
        filteredIndexList.clear();
        if (query.isEmpty()) {
            filteredIndexList.addAll(list_index);
        } else {
            for (Index index : list_index) {
                if (index.getNameRoom().toLowerCase().contains(query.toLowerCase())) {
                    filteredIndexList.add(index);
                }
            }
        }
        adapter.setIndexList(filteredIndexList);
    }


    private Spannable customizeText(String s)  // Hàm set mau va font chu cho Text
    {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }

    public void showErrorMessage(String message, int id) {
        TextInputLayout layout_name_home = dialog.findViewById(id);
        layout_name_home.setError(message);
    }

    private void addTextWatcher(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String index = Objects.requireNonNull(editText.getText()).toString().trim();
                if (!index.isEmpty()) {

                    layout.setErrorEnabled(false);
                    layout.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void setupDialog(Index index) {
        LayoutDialogDetailedIndexBinding binding = LayoutDialogDetailedIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        setupDialogWindow(binding.getRoot().getLayoutParams());

        binding.txtTitleDialog.append(index.getNameRoom());

        if (index.getWaterIsIndex()) {
            binding.line.setVisibility(View.VISIBLE);
            binding.titleWaterIndex.setVisibility(View.VISIBLE);
            binding.layoutIndexWater.setVisibility(View.VISIBLE);
            binding.layoutWaterIndexUsed.setVisibility(View.VISIBLE);
        } else {
            binding.line.setVisibility(View.GONE);
            binding.titleWaterIndex.setVisibility(View.GONE);
            binding.layoutIndexWater.setVisibility(View.GONE);
            binding.layoutWaterIndexUsed.setVisibility(View.GONE);
        }

        setupTextViews(binding);
        setupEditTexts(binding, index);

        setupIndexCalculations(binding, index);


        addTextWatcher(binding.edtElectricityIndexOld, binding.layoutEdtElectricityIndexOld);
        addTextWatcher(binding.edtElectricityIndexNew, binding.layoutEdtElectricityIndexNew);
        if (index.getWaterIsIndex()) {
            addTextWatcher(binding.edtWaterIndexOld, binding.layoutEdtWaterIndexOld);
            addTextWatcher(binding.edtWaterIndexNew, binding.layoutEdtWaterIndexNew);
        }

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        binding.btnSaveIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String indexID = index.getIndexID();
                final String nameRoom = index.getNameRoom();
                final String electricityIndexOld = Objects.requireNonNull(binding.edtElectricityIndexOld.getText()).toString();
                final String electricityIndexNew = Objects.requireNonNull(binding.edtElectricityIndexNew.getText()).toString();
                final String waterIndexOld = Objects.requireNonNull(binding.edtWaterIndexOld.getText()).toString();
                final String waterIndexNew = Objects.requireNonNull(binding.edtWaterIndexNew.getText()).toString();
                String electricityIndexUsed = Objects.requireNonNull(binding.edtElectricityIndexCalculated.getText()).toString();
                String waterIndexUsed = Objects.requireNonNull(binding.edtWaterIndexCalculated.getText()).toString();

                if (electricityIndexOld.length() != 6) {
                    showErrorMessage("Hãy nhập đủ 6 chữ số", R.id.layout_edt_electricity_index_old);
                    return;
                } else if (electricityIndexNew.length() != 6) {
                    showErrorMessage("Hãy nhập đủ 6 chữ số", R.id.layout_edt_electricity_index_new);
                    return;
                } else if (waterIndexOld.length() != 6) {
                    showErrorMessage("Hãy nhập đủ 6 chữ số", R.id.layout_edt_water_index_old);
                    return;
                } else if (waterIndexNew.length() != 6) {
                    showErrorMessage("Hãy nhập đủ 6 chữ số", R.id.layout_edt_water_index_new);
                    return;
                }

                if (Integer.parseInt(electricityIndexUsed) <= 0) {
                    showErrorMessage("Giá trị không được âm", R.id.layout_electricity_index_calculated);
                    return;
                }
                if (index.getWaterIsIndex()) {
                    if (Integer.parseInt(waterIndexUsed) <= 0) {
                        showErrorMessage("Giá trị không được âm", R.id.layout_water_index_calculated);
                        return;
                    }
                }

                // Lấy tháng và năm
                int month = index.getMonth(); // Tháng trong Java Calendar bắt đầu từ 0, cần cộng thêm 1
                int year = index.getYear();

                // Thực hiện truy vấn để lấy roomID từ tên phòng
                db.collection(Constants.KEY_COLLECTION_ROOMS)
                        .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                        .whereEqualTo(Constants.KEY_NAME_ROOM, nameRoom)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    DocumentSnapshot roomDoc = querySnapshot.getDocuments().get(0); // Giả sử chỉ có một phòng có tên như vậy
                                    String roomID = roomDoc.getId();

                                    Index indexNew = new Index(homeID, indexID, nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew, month, year, waterIsIndex);
                                    indexNew.setRoomID(roomID);

                                    indexPresenter.saveIndex(indexNew);
                                } else {
                                    Log.w("Firestore", "No room found with name: " + nameRoom);
                                }
                            } else {
                                Log.w("Firestore", "Error getting documents: ", task.getException());
                            }
                        });


            }
        });


        dialog.setCancelable(true);
        dialog.show();


    }

    private void setupIndexCalculations(LayoutDialogDetailedIndexBinding binding, Index index) {
        updateElectricityIndexCalculation(binding);
        if (index.getWaterIsIndex()) {
            updateWaterIndexCalculation(binding);
        }

        addIndexTextWatchers(binding, index);
    }

    private void updateElectricityIndexCalculation(LayoutDialogDetailedIndexBinding binding) {
        String indexElectricityOld = Objects.requireNonNull(binding.edtElectricityIndexOld.getText()).toString().trim();
        String indexElectricityNew = Objects.requireNonNull(binding.edtElectricityIndexNew.getText()).toString().trim();
        if (!indexElectricityOld.isEmpty() && !indexElectricityNew.isEmpty()) {
            int indexUsed = Integer.parseInt(indexElectricityNew) - Integer.parseInt(indexElectricityOld);
            binding.edtElectricityIndexCalculated.setText(String.valueOf(indexUsed));
            if (indexUsed <= 0 && (!binding.edtElectricityIndexOld.getText().toString().equals("000000") || !binding.edtElectricityIndexNew.getText().toString().equals("000000"))) {
                binding.layoutElectricityIndexCalculated.setError("Giá trị không được âm");
            } else {
                binding.layoutElectricityIndexCalculated.setErrorEnabled(false);
            }
        }
    }

    private void updateWaterIndexCalculation(LayoutDialogDetailedIndexBinding binding) {
        String indexWaterOld = Objects.requireNonNull(binding.edtWaterIndexOld.getText()).toString().trim();
        String indexWaterNew = Objects.requireNonNull(binding.edtWaterIndexNew.getText()).toString().trim();
        if (!indexWaterOld.isEmpty() && !indexWaterNew.isEmpty()) {
            int indexUsed = Integer.parseInt(indexWaterNew) - Integer.parseInt(indexWaterOld);
            binding.edtWaterIndexCalculated.setText(String.valueOf(indexUsed));
            if (indexUsed <= 0 && (!binding.edtWaterIndexOld.getText().toString().equals("000000") || !binding.edtWaterIndexNew.getText().toString().equals("000000"))) {
                binding.layoutWaterIndexCalculated.setError("Giá trị không được âm");
            } else {
                binding.layoutWaterIndexCalculated.setErrorEnabled(false);
            }
        }
    }

    private void addIndexTextWatchers(LayoutDialogDetailedIndexBinding binding, Index index) {
        addTextWatcher(binding.edtElectricityIndexOld, binding.edtElectricityIndexNew, () -> updateElectricityIndexCalculation(binding));
        addTextWatcher(binding.edtElectricityIndexNew, binding.edtElectricityIndexOld, () -> updateElectricityIndexCalculation(binding));
        if (index.getWaterIsIndex()) {
            addTextWatcher(binding.edtWaterIndexOld, binding.edtWaterIndexNew, () -> updateWaterIndexCalculation(binding));
            addTextWatcher(binding.edtWaterIndexNew, binding.edtWaterIndexOld, () -> updateWaterIndexCalculation(binding));
        }
    }

    private void addTextWatcher(TextInputEditText editText, TextInputEditText otherEditText, Runnable calculationMethod) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if the length is less than 6 and not completely empty
                if (s.length() < 6 && s.length() > 0) {
                    calculationMethod.run();
                } else if (s.length() == 6) {
                    calculationMethod.run();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if the length is less than 6 and not completely empty
                if (s.length() == 0) {
                    // If the length is 0, set it back to the previous value
                    editText.setText("0"); // or any other default value you want
                    editText.setSelection(editText.getText().length());
                }
            }
        });
    }


    private void setupDialogWindow(ViewGroup.LayoutParams params) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);

            // Set margin programmatically
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                int margin = (int) getResources().getDimension(R.dimen.dialog_margin);
                marginParams.setMargins(margin, margin, margin, margin);
            }
        }
    }

    private void setupTextViews(LayoutDialogDetailedIndexBinding binding) {
        binding.txtElectricityIndexOld.append(customizeText(" *"));
        binding.txtElectricityIndexNew.append(customizeText(" *"));
        binding.txtWaterIndexOld.append(customizeText(" *"));
        binding.txtWaterIndexNew.append(customizeText(" *"));
    }

    private void setupEditTexts(LayoutDialogDetailedIndexBinding binding, Index index) {
        binding.edtElectricityIndexOld.setText(index.getElectricityIndexOld());
        binding.edtElectricityIndexNew.setText(index.getElectricityIndexNew());
        binding.edtWaterIndexOld.setText(index.getWaterIndexOld());
        binding.edtWaterIndexNew.setText(index.getWaterIndexNew());

        setFocusChangeListener(binding.edtElectricityIndexOld, binding.layoutEdtElectricityIndexOld);
        setFocusChangeListener(binding.edtElectricityIndexNew, binding.layoutEdtElectricityIndexNew);
        setFocusChangeListener(binding.edtWaterIndexOld, binding.layoutEdtWaterIndexOld);
        setFocusChangeListener(binding.edtWaterIndexNew, binding.layoutEdtWaterIndexNew);
    }

    private void setFocusChangeListener(TextInputEditText editText, TextInputLayout layout) {
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                layout.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
            }
        });
    }


    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new IndexAdapter(requireActivity(), new ArrayList<>(), this, indexPresenter);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupPagination() {
//        binding.btnNextPage.setOnClickListener(v -> {
//            if (currentPage < totalPages - 1) {
//                currentPage++;
//                updateButtonPageState();
//                scrollToSelectedButton(binding.linearScroll.getChildAt(currentPage));
//            }
//        });
//
//        binding.btnPreviousPage.setOnClickListener(v -> {
//            if (currentPage > 0) {
//                currentPage--;
//                updateButtonPageState();
//                scrollToSelectedButton(binding.linearScroll.getChildAt(currentPage));
//            }
//        });

//        for (int j = 0; j < totalPages; j++) {
//            final int k = j;
//            final Button btnPage = new Button(requireContext());
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT);
//            lp.setMargins(10, 0, 10, 0);
//            btnPage.setTextColor(Color.WHITE);
//            btnPage.setTextSize(13.0f);
//            btnPage.setId(j);
//            btnPage.setText(String.valueOf(j + 1));
//            btnPage.setBackground(getResources().getDrawable(R.drawable.background_page_number_index));
//
//            binding.linearScroll.addView(btnPage, lp);
//            btnPage.setOnClickListener(v -> {
//                currentPage = k;
//                updateButtonPageState();
//                scrollToSelectedButton(btnPage);
//            });
//        }
        //updateButtonPageState();
    }

    private void setupDeleteRows() {
        binding.btnDelete.setOnClickListener(v -> {
            binding.btnDelete.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            binding.frameRoundDeleteIndex.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
            binding.layoutDeleteManyRows.setVisibility(View.VISIBLE);
            adapter.isDeleteClicked(true);
        });

        binding.checkboxSelectAll.setOnClickListener(v -> {
            if (isCheckBoxClicked) {
                binding.checkboxSelectAll.setChecked(false);
                adapter.isCheckBoxClicked(false);
                isCheckBoxClicked = false;
            } else {
                binding.checkboxSelectAll.setChecked(true);
                adapter.isCheckBoxClicked(true);
                isCheckBoxClicked = true;
            }
        });

        binding.txtDeleteIndexHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogConfirmDeleteManyIndexes();
            }
        });

        binding.txtCancelDeleteIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLayoutDeleteManyRows();
            }
        });
    }

    private void showDialogConfirmDeleteManyIndexes() {
        LayoutDialogDeleteIndexBinding binding = LayoutDialogDeleteIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.txtConfirmDelete.setText("Bạn chắc chắn muốn xoá dữ liệu chỉ số của các phòng này?");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        binding.btnConfirmDeleteIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Index> selectedIndexes = adapter.getSelectedIndexes();
                List<Index> indexTemp = new ArrayList<>();

                if (selectedIndexes.isEmpty()) {
                    showToast("No indexes selected");
                    return;
                }

                AtomicInteger completedCount = new AtomicInteger(0);
                int totalQueries = selectedIndexes.size();

                for (Index index : selectedIndexes) {
                    // Thực hiện truy vấn để lấy roomID từ tên phòng
                    db.collection(Constants.KEY_COLLECTION_ROOMS)
                            .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                            .whereEqualTo(Constants.KEY_NAME_ROOM, index.getNameRoom())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot roomDoc = querySnapshot.getDocuments().get(0); // Giả sử chỉ có một phòng có tên như vậy
                                        String roomID = roomDoc.getId();

                                        Index indexNew = new Index(homeID, index.getIndexID(), index.getNameRoom(), index.getElectricityIndexOld()
                                                , index.getElectricityIndexNew(), index.getWaterIndexOld(), index.getWaterIndexNew()
                                                , index.getMonth(), index.getYear(), waterIsIndex);
                                        indexNew.setRoomID(roomID);
                                        indexTemp.add(indexNew);
                                    } else {
                                        Log.w("Firestore", "No room found with name: " + index.getNameRoom());
                                    }
                                } else {
                                    Log.w("Firestore", "Error getting documents: ", task.getException());
                                }

                                if (completedCount.incrementAndGet() == totalQueries) {
                                    // Tất cả các truy vấn đã hoàn thành
                                    indexPresenter.deleteSelectedIndexes(indexTemp);
                                }
                            });
                }

            }
        });
        setUpDialogConfirmation();
    }


    private void setupMonthPicker() {


        binding.imgCalendar.setOnClickListener(v -> {
            visible = true;
            showMonthPicker(); // Open dialog
        });

        String monthYear = (currentMonth + 1) + "/" + currentYear;
        binding.txtDateTime.setText(monthYear);
    }

    private void showMonthPicker() {
        MonthPickerDialogCustom monthPickerDialogCustom = new MonthPickerDialogCustom(requireContext(), currentMonth, currentYear,
                new MonthPickerDialogCustom.OnMonthSelectedListener() {
                    @Override
                    public void onMonthSelected(int month, int year) {
                        date = month + "/" + year; // month = selectedMonthPosition + 1 ==> month == actual value
                        visible = false;
                        binding.txtDateTime.setText(date);
                        setupLayout(homeID, month, year);
                        indexPresenter.fetchIndexesByMonthAndYear(homeID, month, year, "init");
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

//    private void updateButtonPageState() {
//        binding.btnPreviousPage.setEnabled(currentPage > 0);
//        binding.btnNextPage.setEnabled(currentPage < totalPages - 1);
//
//        binding.btnPreviousPage.setTextColor(getResources().getColor(
//                binding.btnPreviousPage.isEnabled() ? R.color.colorButton : R.color.colorGray));
//        binding.btnNextPage.setTextColor(getResources().getColor(
//                binding.btnNextPage.isEnabled() ? R.color.colorButton : R.color.colorGray));
//
//        for (int i = 0; i < binding.linearScroll.getChildCount(); i++) {
//            Button btnPage = (Button) binding.linearScroll.getChildAt(i);
//            if (i == currentPage) {
//                btnPage.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
//            } else {
//                btnPage.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
//            }
//        }
//    }

//    private void scrollToSelectedButton(View button) {
//        binding.scroll.post(() -> {
//            int scrollX = button.getLeft() - (binding.scroll.getWidth() - button.getWidth()) / 2;
//            binding.scroll.smoothScrollTo(scrollX, 0);
//        });
//    }


    private void updateButtonsState() {
        if (!isNextClicked) {
            binding.btnPrevious.setEnabled(false);
            binding.btnPrevious.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            binding.btnNext.setEnabled(true);
            binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTextBlack)));
        } else {
            binding.btnPrevious.setEnabled(true);
            binding.btnPrevious.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTextBlack)));

            LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
            if (layoutManager != null) {
                int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                if (lastVisiblePosition == adapter.getItemCount() - 1) {
                    binding.btnNext.setEnabled(false);
                    binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
                } else {
                    binding.btnNext.setEnabled(true);
                    binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTextBlack)));
                }
            }
        }
    }

    @Override
    public void showDialogDetailedIndex(Index index) {
        setupDialog(index);
    }

    @Override
    public void setIndexList(List<Index> indexList) {
        list_index = indexList;
        if (adapter != null) {
            adapter.setIndexList(indexList);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeDialog() {
        dialog.dismiss();
    }

    @Override
    public void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutNoData.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
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
    public void showDialogConfirmDeleteIndex(Index index) {
        LayoutDialogDeleteIndexBinding binding = LayoutDialogDeleteIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.txtConfirmDelete.append(index.getNameRoom() + " ?");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        binding.btnConfirmDeleteIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String indexID = index.getIndexID();
                final String nameRoom = index.getNameRoom();
                final String electricityIndexOld = index.getElectricityIndexOld();
                final String electricityIndexNew = index.getElectricityIndexNew();
                final String waterIndexOld = index.getWaterIndexOld();
                final String waterIndexNew = index.getWaterIndexNew();

                // Lấy tháng và năm
                int month = index.getMonth(); // Tháng trong Java Calendar bắt đầu từ 0, cần cộng thêm 1
                int year = index.getYear();


                // Thực hiện truy vấn để lấy roomID từ tên phòng
                db.collection(Constants.KEY_COLLECTION_ROOMS)
                        .whereEqualTo(Constants.KEY_HOME_ID, homeID)
                        .whereEqualTo(Constants.KEY_NAME_ROOM, nameRoom)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    DocumentSnapshot roomDoc = querySnapshot.getDocuments().get(0); // Giả sử chỉ có một phòng có tên như vậy
                                    String roomID = roomDoc.getId();

                                    Index indexNew = new Index(homeID, indexID, nameRoom, electricityIndexOld, electricityIndexNew, waterIndexOld, waterIndexNew, month, year, waterIsIndex);
                                    indexNew.setRoomID(roomID);

                                    indexPresenter.deleteIndex(indexNew);
                                } else {
                                    Log.w("Firestore", "No room found with name: " + nameRoom);
                                }
                            } else {
                                Log.w("Firestore", "Error getting documents: ", task.getException());
                            }
                        });


            }
        });
        setUpDialogConfirmation();


    }

    @Override
    public void closeLayoutDeleteManyRows() {
        adapter.isDeleteClicked(false);

        binding.checkboxSelectAll.setChecked(false);
        adapter.isCheckBoxClicked(false);
        binding.btnDelete.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
        binding.frameRoundDeleteIndex.setBackground(getResources().getDrawable(R.drawable.background_delete_index_normal));
        binding.layoutDeleteManyRows.setVisibility(View.GONE);
    }

    @Override
    public void showDialogActionSuccess(String textSuccess) {
        LayoutDialogDeleteHomeSuccessBinding binding = LayoutDialogDeleteHomeSuccessBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());

        binding.txtDeleteHomeSuccess.setText(textSuccess);

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setUpDialogConfirmation();
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
    public void getListIndexes(List<Index> indexList) {
        setCurrentListIndexes(indexList);
    }

    @Override
    public void setWaterIndex(boolean isUsed) {
        setWaterIsIndex(isUsed);
    }

    @Override
    public void showDialogNoteIndexStatus() {
        LayoutDialogNoteIndexBinding binding = LayoutDialogNoteIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        setUpDialogConfirmation();

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public boolean isAdded2() {
        return isAdded();
    }

    @Override
    public void setupLayoutForNextMonth(String homeID, int month, int year) {
        setupLayout(homeID, month, year);
        indexPresenter.fetchIndexesByMonthAndYear(homeID, month, year, "init");
    }

    public String getHomeID() {
        return homeID;
    }

    public boolean isWaterIsIndex() {
        return waterIsIndex;
    }

    public void setWaterIsIndex(boolean waterIsIndex) {
        this.waterIsIndex = waterIsIndex;
    }

    private void setUpDialogConfirmation() {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
        }
        dialog.setCancelable(true);
        dialog.show();
    }

    public void removeStatusOfCheckBoxFilterIndex() {
        preferenceManager.removePreference("cbxByElectricityIndex1");
        preferenceManager.removePreference("cbxByElectricityIndex2");
        preferenceManager.removePreference("cbxByElectricityIndex3");
        preferenceManager.removePreference(Constants.KEY_CBX_BY_WATER_INDEX_1);
        preferenceManager.removePreference(Constants.KEY_CBX_BY_WATER_INDEX_2);
        preferenceManager.removePreference(Constants.KEY_CBX_BY_WATER_INDEX_3);
    }


}