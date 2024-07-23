package edu.poly.nhtr.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import edu.poly.nhtr.Activity.MonthPickerDialogCustom;
import edu.poly.nhtr.Adapter.RevenueOfMonthAdapter;
import edu.poly.nhtr.Adapter.RevenueOfRoomAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentStatisticBinding;
import edu.poly.nhtr.listeners.StatisticListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.RevenueOfMonthModel;
import edu.poly.nhtr.models.RevenueOfRoomModel;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.monthpicker.MonthPickerDialog;
import edu.poly.nhtr.presenters.StatisticPresenter;


public class StatisticFragment extends Fragment implements StatisticListener, SwipeRefreshLayout.OnRefreshListener {
    private FragmentStatisticBinding binding;
    private RevenueOfMonthAdapter revenueOfMonthAdapter;
    private RevenueOfRoomAdapter revenueOfRoomAdapter;
    private String homeID;
    private StatisticPresenter statisticPresenter;
    Map<String, Long> mapForLineChart = new HashMap<>();
    Map<String, Long> mapForBarChart = new HashMap<>();
    private int currentYear;
    private int currentMonth;
    private String date;
    private List<Room> currentRoomList = new ArrayList<>();
    private TextView textView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentStatisticBinding.inflate(getLayoutInflater());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        assert getArguments() != null;
        Home home = (Home) getArguments().getSerializable("home");
        assert home != null;
        homeID = home.getIdHome();
        statisticPresenter = new StatisticPresenter(this, homeID);

        binding.swipeRefreshFragment.setOnRefreshListener(this);

        setupDateTime();
        getValueForLineChart();
        getValueForBarChart();

        setListeners();
        customizeTextViewUnderLine();
        //setupMonthPicker();


        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onRefresh() {
        setupDateTime();
        getValueForBarChart();
        getValueForLineChart();
        customizeTextViewUnderLine();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.swipeRefreshFragment.setRefreshing(false);
            }
        }, 2000);
    }

    private void setupDateTime()
    {
        currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        binding.txtDateTimeForMonth.setText(String.valueOf(currentYear));
        binding.txtDateTimeForRoom.setText(currentMonth + "/" + currentYear);


        // Tạo chuỗi hoàn chỉnh
        String dateTimeForMonth = String.format("Báo cáo doanh thu các tháng năm %s", currentYear);
        binding.txtRevenueOfMonth.setText(dateTimeForMonth);
        String dateTimeForRoom = String.format("Báo cáo doanh thu các phòng tháng %s", currentMonth + "/" + currentYear);
        binding.txtRevenueOfRoom.setText(dateTimeForRoom);
    }

    private void getValueForBarChart()
    {
        statisticPresenter.getListRoomByHome(homeID, new StatisticPresenter.OnGetRoomCompleteListener() {
            @Override
            public void onComplete(List<Room> roomList) {
                currentRoomList = roomList;
                statisticPresenter.getListBillByListRoom(roomList, new StatisticPresenter.OnGetBillCompleteListener() {
                    @Override
                    public void onComplete(List<RoomBill> roomBillList) {
                        Map<String, Long> monthYearTotalMap = new HashMap<>();
                        for (RoomBill roomBill : roomBillList) {
                            String monthYearKey = roomBill.getMonth() + "-" + roomBill.getYear() + "-" + roomBill.getRoomName();
                            long totalOfMoney = roomBill.getTotalOfMoney();

                            monthYearTotalMap.put(monthYearKey, totalOfMoney);

                        }

                        mapForBarChart = monthYearTotalMap;

                        setupBarChart(monthYearTotalMap, currentMonth, currentYear);

                    }
                });

            }
        });
    }

    private void getValueForLineChart()
    {
        statisticPresenter.getListRoomByHome(homeID, new StatisticPresenter.OnGetRoomCompleteListener() {
            @Override
            public void onComplete(List<Room> roomList) {
                statisticPresenter.getListBillByListRoom(roomList, new StatisticPresenter.OnGetBillCompleteListener() {
                    @Override
                    public void onComplete(List<RoomBill> roomBillList) {
                        Map<String, Long> monthYearTotalMap = new HashMap<>();

                        for (RoomBill roomBill : roomBillList) {
                            String monthYearKey = roomBill.getMonth() + "-" + roomBill.getYear();
                            long totalOfMoney = roomBill.getTotalOfMoney();

                            if (monthYearTotalMap.containsKey(monthYearKey)) {
                                monthYearTotalMap.put(monthYearKey, monthYearTotalMap.get(monthYearKey) + totalOfMoney);
                            } else {
                                monthYearTotalMap.put(monthYearKey, totalOfMoney);
                            }
                        }

                        mapForLineChart = monthYearTotalMap;

                        setupLineChart(monthYearTotalMap, currentYear);

                    }
                });
            }
        });
    }

    private void showYearPicker() {
        final Calendar today = Calendar.getInstance();
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(requireContext(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        binding.txtDateTimeForMonth.setText(String.valueOf(selectedYear));
                        String dateTimeForMonth = String.format("Báo cáo doanh thu các tháng năm %s", selectedYear);
                        binding.txtRevenueOfMonth.setText(dateTimeForMonth);
                        setupLineChart(mapForLineChart,selectedYear);
                    }
                }, today.get(Calendar.YEAR), today.get(Calendar.MONTH));

        builder.setActivatedMonth(Calendar.JULY)
                .setMinYear(1990)
                .setMaxYear(2100)
                .setActivatedYear(today.get(Calendar.YEAR))
                .setTitle("Chọn năm")
                .showYearOnly()
                .build().show();
    }


    private void showYearPickerDialog() {
        // Tạo một view từ layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.layout_dialog_year_picker, null);

        final NumberPicker yearPicker = view.findViewById(R.id.yearPicker);

        // Đặt giá trị minValue, maxValue và wrapSelectorWheel trong Java
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(2100);
        yearPicker.setWrapSelectorWheel(false);

        // Đặt giá trị ban đầu cho NumberPicker
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setValue(currentYear);

        // Tạo và hiển thị dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn năm")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedYear = yearPicker.getValue();
                        // Thực hiện hành động với năm đã chọn
                        Toast.makeText(requireContext(), "Năm đã chọn: " + selectedYear, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showMonthPicker() {
        MonthPickerDialogCustom monthPickerDialogCustom = new MonthPickerDialogCustom(requireContext(), currentMonth-1, currentYear,
                new MonthPickerDialogCustom.OnMonthSelectedListener() {
                    @Override
                    public void onMonthSelected(int month, int year) {
                        date = month + "/" + year; // month = selectedMonthPosition + 1 ==> month == actual value
                        binding.txtDateTimeForRoom.setText(date);
                        String dateTimeForRoom = String.format("Báo cáo doanh thu các phòng tháng %s", date);
                        binding.txtRevenueOfRoom.setText(dateTimeForRoom);
                        setupBarChart(mapForBarChart, month, year);

                        currentMonth = month ; // Cập nhật currentMonth, have to minus 1
                        currentYear = year; // Cập nhật year
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        Objects.requireNonNull(monthPickerDialogCustom.getWindow()).setBackgroundDrawableResource(R.drawable.background_dialog_index);
        monthPickerDialogCustom.show();
    }


    private void setupTableRevenueOfMonth() {

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        int year = Integer.parseInt(binding.txtDateTimeForMonth.getText().toString());
        revenueOfMonthAdapter = new RevenueOfMonthAdapter(requireContext(), getRevenueOfMonthList(mapForLineChart, year));
        binding.recyclerView.setAdapter(revenueOfMonthAdapter);

    }

    private void setupTableRevenueOfRoom() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        String date = binding.txtDateTimeForRoom.getText().toString();
        // Tách chuỗi bằng ký tự "/"
        String[] parts = date.split("/");

        // Lấy tháng và năm
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        revenueOfRoomAdapter = new RevenueOfRoomAdapter(requireContext(), getRevenueOfRoomList(mapForBarChart, month, year));
        binding.recyclerView.setAdapter(revenueOfRoomAdapter);

    }


    private void setVisibleForTableRevenueOfMonth(boolean visible) {
        if (visible) {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.txtTitleOfColumn1.setText("Tháng");
            binding.txtBackToLineChart.setVisibility(View.VISIBLE);


            binding.layoutDateTimeForMonth.setVisibility(View.GONE);
            binding.lineChart.setVisibility(View.GONE);
            binding.layoutInformationOfChart.setVisibility(View.GONE);
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.GONE);
        } else {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.GONE);
            binding.txtBackToLineChart.setVisibility(View.GONE);

            binding.layoutDateTimeForMonth.setVisibility(View.VISIBLE);
            binding.lineChart.setVisibility(View.VISIBLE);
            binding.layoutInformationOfChart.setVisibility(View.VISIBLE);
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.VISIBLE);
        }
    }

    private void setVisibleForTableRevenueOfRoom(boolean visible) {
        if (visible) {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.txtTitleOfColumn1.setText("Phòng");
            binding.txtBackToBarChart.setVisibility(View.VISIBLE);

            binding.layoutDateTimeForRoom.setVisibility(View.GONE);
            binding.barChart.setVisibility(View.GONE);
            binding.layoutInformationOfChart.setVisibility(View.GONE);
            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.GONE);
        } else {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.GONE);
            binding.txtBackToBarChart.setVisibility(View.GONE);

            binding.layoutDateTimeForRoom.setVisibility(View.VISIBLE);
            binding.barChart.setVisibility(View.VISIBLE);
            binding.layoutInformationOfChart.setVisibility(View.VISIBLE);
            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.VISIBLE);
        }
    }


    private List<RevenueOfMonthModel> getRevenueOfMonthList(Map<String, Long> monthYearTotalMap, int currentYear) {
        List<RevenueOfMonthModel> revenueOfMonthList = new ArrayList<>();

        // Định dạng số tiền với dấu phân cách ba chữ số
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        // Tạo danh sách tháng từ 1 đến 12
        for (int month = 1; month <= 12; month++) {
            String monthKey = month + "-" + currentYear; // Thay đổi năm nếu cần
            long totalMoney = monthYearTotalMap.getOrDefault(monthKey, 0L);

            // Định dạng số tiền
            String formattedMoney = numberFormat.format(totalMoney);

            // Thay đổi định dạng theo kiểu 6.000.000 (hoặc 6 triệu)
            revenueOfMonthList.add(new RevenueOfMonthModel("T." + month, formattedMoney));
        }

        return revenueOfMonthList;
    }


    private List<RevenueOfRoomModel> getRevenueOfRoomList(Map<String, Long> monthYearTotalMap, int currentMonth, int currentYear) {
        List<RevenueOfRoomModel> revenueOfRoomList = new ArrayList<>();
        // Định dạng số tiền với dấu phân cách ba chữ số
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        List<String> xValues = new ArrayList<>();



        for (Room room : currentRoomList) {
            xValues.add(room.getNameRoom());
        }
        for (int i = 0; i < xValues.size(); i++) {
            String keyMonthYearRoom = currentMonth + "-" + currentYear + "-" + xValues.get(i);
            long totalMoney = monthYearTotalMap.getOrDefault(keyMonthYearRoom, 0L);

            // Định dạng số tiền
            String formattedMoney = numberFormat.format(totalMoney);

            // Thay đổi định dạng theo kiểu 6.000.000 (hoặc 6 triệu)
            revenueOfRoomList.add(new RevenueOfRoomModel(xValues.get(i), formattedMoney));

        }


        return revenueOfRoomList;
    }

    private void customizeTextViewUnderLine() {
        String text = binding.txtViewDetailedValue.getText().toString();
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        binding.txtViewDetailedValue.setText(spannableString);
    }

    private void setListeners() {
        binding.btnViewDetailedRevenueOfMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = binding.txtDateTimeForRoom.getText().toString();
                // Tách chuỗi bằng ký tự "/"
                String[] parts = date.split("/");

                // Lấy tháng và năm
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);

                String dateTimeForRoom = String.format("Báo cáo doanh thu các phòng tháng %s", month + "/" + year);
                binding.txtRevenueOfRoom.setText(dateTimeForRoom);
                setupBarChart(mapForBarChart, month, year);
                setVisible(false);
            }
        });

        binding.btnViewDetailedRevenueOfRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = Integer.parseInt(binding.txtDateTimeForMonth.getText().toString());
                String dateTimeForMonth = String.format("Báo cáo doanh thu các tháng năm %s", year);
                binding.txtRevenueOfMonth.setText(dateTimeForMonth);
                setupLineChart(mapForLineChart, year);
                setVisible(true);
            }
        });

        binding.txtViewDetailedValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.lineChart.getVisibility() == View.VISIBLE) {
                    setupTableRevenueOfMonth();
                    setVisibleForTableRevenueOfMonth(true);
                } else if (binding.barChart.getVisibility() == View.VISIBLE) {
                    setupTableRevenueOfRoom();
                    setVisibleForTableRevenueOfRoom(true);
                }
            }
        });

        binding.txtBackToLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = Integer.parseInt(binding.txtDateTimeForMonth.getText().toString());
                String dateTimeForMonth = String.format("Báo cáo doanh thu các tháng năm %s", year);
                binding.txtRevenueOfMonth.setText(dateTimeForMonth);
                setupLineChart(mapForLineChart, year);
                setVisibleForTableRevenueOfMonth(false);
            }
        });

        binding.txtBackToBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = binding.txtDateTimeForRoom.getText().toString();
                // Tách chuỗi bằng ký tự "/"
                String[] parts = date.split("/");

                // Lấy tháng và năm
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                String dateTimeForRoom = String.format("Báo cáo doanh thu các phòng tháng %s", month + "/" + year);
                binding.txtRevenueOfRoom.setText(dateTimeForRoom);
                setupBarChart(mapForBarChart, month, year);
                setVisibleForTableRevenueOfRoom(false);
            }
        });

        binding.imgCalendarForMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showYearPicker();
            }
        });

        binding.imgCalendarForRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthPicker();
            }
        });
    }

    private void setVisible(boolean visible) {
        if (visible) {
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.lineChart.setVisibility(View.VISIBLE);
            binding.txtRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.layoutDateTimeForMonth.setVisibility(View.VISIBLE);

            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.GONE);
            binding.barChart.setVisibility(View.GONE);
            binding.txtRevenueOfRoom.setVisibility(View.GONE);
            binding.layoutDateTimeForRoom.setVisibility(View.GONE);
        } else {
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.GONE);
            binding.lineChart.setVisibility(View.GONE);
            binding.txtRevenueOfMonth.setVisibility(View.GONE);
            binding.layoutDateTimeForMonth.setVisibility(View.GONE);

            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.VISIBLE);
            binding.barChart.setVisibility(View.VISIBLE);
            binding.txtRevenueOfRoom.setVisibility(View.VISIBLE);
            binding.layoutDateTimeForRoom.setVisibility(View.VISIBLE);
        }
    }


    private void setupBarChart(Map<String, Long> monthYearTotalMap, int currentMonth, int currentYear) {
        List<String> xValues = new ArrayList<>();

        for (Room room : currentRoomList) {
            xValues.add(room.getNameRoom());
        }

        binding.barChart.getAxisRight().setDrawLabels(false);

        ArrayList<BarEntry> entries = new ArrayList<>();

        float maxTotalMoney = 0;

        for (int i = 0; i < xValues.size(); i++) {
            String keyMonthYearRoom = currentMonth + "-" + currentYear + "-" + xValues.get(i);
            long totalMoney = monthYearTotalMap.getOrDefault(keyMonthYearRoom, 0L);
            float convertedTotalMoney = totalMoney / 1_000_000f; // Chia cho 1,000,000 để chuyển đổi sang triệu
            entries.add(new BarEntry(i, convertedTotalMoney));
            if (convertedTotalMoney > maxTotalMoney) {
                maxTotalMoney = convertedTotalMoney;
            }
        }

        YAxis yAxis = binding.barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisLineWidth(0.5f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setTextSize(12f);
        yAxis.setLabelCount(7, true); // Nếu muốn cố định có số 3(giá trị max) thì cho là true, nếu không thì bỏ true
        yAxis.setAxisMaximum(maxTotalMoney + 1); // Đặt giá trị tối đa cho trục Y, thêm một khoảng trống nhỏ

        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value); // Hiển thị với 1 số thập phân và đơn vị triệu
            }
        });

        BarDataSet dataSet = new BarDataSet(entries, "Phòng");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f); // Điều chỉnh kích thước chữ hiển thị trên các điểm dữ liệu
        dataSet.setValueTextColor(getResources().getColor(R.color.colorTextBlack));

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.format("%.2f", barEntry.getY()); // Hiển thị với 2 số thập phân
            }
        });

        BarData barData = new BarData(dataSet);
        binding.barChart.setData(barData);
        binding.barChart.animateY(1000);

        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.invalidate();

        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
        binding.barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChart.getXAxis().setGranularity(1f);
        binding.barChart.getXAxis().setLabelCount(xValues.size());
        binding.barChart.getXAxis().setGranularityEnabled(true);
        binding.barChart.getXAxis().setTextSize(12f);
    }


    private void setupLineChart(Map<String, Long> monthYearTotalMap, int currentYear) {
        Description description = new Description();
        description.setText("Doanh thu tháng");
        description.setPosition(160f, 15f);
        binding.lineChart.setDescription(description);
        binding.lineChart.getAxisRight().setDrawLabels(false);
        binding.lineChart.animateY(1000);

        List<String> xValues = Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12");

        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(12);
        xAxis.setGranularity(1f);
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setTextSize(10f); // Điều chỉnh kích thước nhãn trục X

        YAxis yAxis = binding.lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setLabelCount(10);
        yAxis.enableGridDashedLine(10f, 10f, 0f);
        yAxis.setTextSize(10f); // Điều chỉnh kích thước nhãn trục Y

        YAxis rightAxis = binding.lineChart.getAxisRight();
        rightAxis.setDrawLabels(false);
        rightAxis.enableGridDashedLine(10f, 10f, 0f);

        List<Entry> entries = new ArrayList<>();
        long maxTotalMoney = 0;

        for (int i = 0; i < 12; i++) {
            String monthYearKey = (i + 1) + "-" + currentYear; // Thay đổi năm nếu cần
            long totalMoney = monthYearTotalMap.getOrDefault(monthYearKey, 0L);
            float convertedTotalMoney = totalMoney / 1_000_000f; // Chia cho 1,000,000 để chuyển đổi sang triệu
            entries.add(new Entry(i, convertedTotalMoney));
            if (convertedTotalMoney > maxTotalMoney) {
                maxTotalMoney = (long) convertedTotalMoney;
            }
        }

        yAxis.setAxisMaximum(maxTotalMoney + 1); // Đặt giá trị tối đa cho trục Y, thêm một khoảng trống nhỏ

        // Tạo ValueFormatter để định dạng số liệu trên trục Y
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value); // Hiển thị với 1 số thập phân và đơn vị triệu
            }
        });

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu tháng");
        dataSet.setColor(Color.BLUE);
        dataSet.setDrawFilled(true);
        dataSet.setValueTextSize(10f); // Điều chỉnh kích thước chữ hiển thị trên các điểm dữ liệu
        dataSet.setValueTextColor(getResources().getColor(R.color.colorTextBlack));

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fill_color_of_line_chart);
        dataSet.setFillDrawable(drawable);

        LineData lineData = new LineData(dataSet);
        binding.lineChart.setData(lineData);
        binding.lineChart.invalidate();
    }


    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


}