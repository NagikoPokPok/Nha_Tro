package edu.poly.nhtr.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import edu.poly.nhtr.presenters.IndexPresenter;
import edu.poly.nhtr.presenters.StatisticPresenter;


public class StatisticFragment extends Fragment implements StatisticListener {
    private FragmentStatisticBinding binding;
    private RevenueOfMonthAdapter revenueOfMonthAdapter;
    private RevenueOfRoomAdapter revenueOfRoomAdapter;
    private String homeID;
    private StatisticPresenter statisticPresenter;
    Map<String, Long> mapForLineChart = new HashMap<>();
    private String year;

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

        year = binding.txtDateTime.getText().toString().trim();

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

                        setupLineChart(monthYearTotalMap);

                        // Thay đổi month và year dưới đây để kiểm tra một monthYear cụ thể
                        String specificMonthYear = "8-2024"; // Ví dụ: tháng 7 năm 2023

                        if (monthYearTotalMap.containsKey(specificMonthYear)) {
                            long totalMoney = monthYearTotalMap.get(specificMonthYear);
                            showToast("Month-Year: " + specificMonthYear + ", Total Money: " + totalMoney);
                        } else {
                            showToast("No data for Month-Year: " + specificMonthYear);
                        }
                    }
                });
            }
        });



        setupBarChart();
        setListeners();
        customizeTextViewUnderLine();
        //setupTableRevenueOfMonth();


        // Inflate the layout for this fragment
        return binding.getRoot();
    }



    private void setupTableRevenueOfMonth() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        revenueOfMonthAdapter = new RevenueOfMonthAdapter(requireContext(), getRevenueOfMonthList(mapForLineChart));
        binding.recyclerView.setAdapter(revenueOfMonthAdapter);

    }

    private void setupTableRevenueOfRoom() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        revenueOfRoomAdapter = new RevenueOfRoomAdapter(requireContext(), getRevenueOfRoomList());
        binding.recyclerView.setAdapter(revenueOfRoomAdapter);

    }


    private void setVisibleForTableRevenueOfMonth(boolean visible) {
        if (visible) {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.txtBackToLineChart.setVisibility(View.VISIBLE);


            binding.layoutDateTime.setVisibility(View.GONE);
            binding.lineChart.setVisibility(View.GONE);
            binding.layoutInformationOfChart.setVisibility(View.GONE);
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.GONE);
        } else {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.GONE);
            binding.txtBackToLineChart.setVisibility(View.GONE);

            binding.layoutDateTime.setVisibility(View.VISIBLE);
            binding.lineChart.setVisibility(View.VISIBLE);
            binding.layoutInformationOfChart.setVisibility(View.VISIBLE);
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.VISIBLE);
        }
    }

    private void setVisibleForTableRevenueOfRoom(boolean visible) {
        if (visible) {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.txtBackToBarChart.setVisibility(View.VISIBLE);

            binding.layoutDateTime.setVisibility(View.GONE);
            binding.barChart.setVisibility(View.GONE);
            binding.layoutInformationOfChart.setVisibility(View.GONE);
            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.GONE);
        } else {
            binding.tableLayoutRevenueOfMonth.setVisibility(View.GONE);
            binding.txtBackToBarChart.setVisibility(View.GONE);

            binding.layoutDateTime.setVisibility(View.VISIBLE);
            binding.barChart.setVisibility(View.VISIBLE);
            binding.layoutInformationOfChart.setVisibility(View.VISIBLE);
            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.VISIBLE);
        }
    }


    private List<RevenueOfMonthModel> getRevenueOfMonthList(Map<String, Long> monthYearTotalMap) {
        List<RevenueOfMonthModel> revenueOfMonthList = new ArrayList<>();

        // Định dạng số tiền với dấu phân cách ba chữ số
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        // Tạo danh sách tháng từ 1 đến 12
        for (int month = 1; month <= 12; month++) {
            String monthKey = month + "-" + year; // Thay đổi năm nếu cần
            long totalMoney = monthYearTotalMap.getOrDefault(monthKey, 0L);

            // Định dạng số tiền
            String formattedMoney = numberFormat.format(totalMoney);

            // Thay đổi định dạng theo kiểu 6.000.000 (hoặc 6 triệu)
            revenueOfMonthList.add(new RevenueOfMonthModel("T." + month, formattedMoney));
        }

        return revenueOfMonthList;
    }




    private List<RevenueOfRoomModel> getRevenueOfRoomList() {
        List<RevenueOfRoomModel> revenueOfRoomList = new ArrayList<>();
        revenueOfRoomList.add(new RevenueOfRoomModel("P100", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P101", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P102", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P103", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P104", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P105", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P200", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P201", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P202", 1000000L));
        revenueOfRoomList.add(new RevenueOfRoomModel("P203", 1000000L));

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
                setupBarChart();
                setVisible(false);
            }
        });

        binding.btnViewDetailedRevenueOfRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupLineChart(mapForLineChart);
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
                setupLineChart(mapForLineChart);
                setVisibleForTableRevenueOfMonth(false);
            }
        });

        binding.txtBackToBarChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupBarChart();
                setVisibleForTableRevenueOfRoom(false);
            }
        });
    }

    private void setVisible(boolean visible) {
        if (visible) {
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.VISIBLE);
            binding.lineChart.setVisibility(View.VISIBLE);
            binding.txtRevenueOfMonth.setVisibility(View.VISIBLE);

            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.GONE);
            binding.barChart.setVisibility(View.GONE);
            binding.txtRevenueOfRoom.setVisibility(View.GONE);
        } else {
            binding.btnViewDetailedRevenueOfMonth.setVisibility(View.GONE);
            binding.lineChart.setVisibility(View.GONE);
            binding.txtRevenueOfMonth.setVisibility(View.GONE);

            binding.btnViewDetailedRevenueOfRoom.setVisibility(View.VISIBLE);
            binding.barChart.setVisibility(View.VISIBLE);
            binding.txtRevenueOfRoom.setVisibility(View.VISIBLE);
        }
    }


    private void setupBarChart() {
        List<String> xValues = Arrays.asList("P100", "P101", "P102", "P103", "P104", "P105", "P200", "P201", "P202", "P203");
        binding.barChart.getAxisRight().setDrawLabels(false);

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        entries.add(new BarEntry(0, 1f));
        entries.add(new BarEntry(1, 1f));
        entries.add(new BarEntry(2, 1.5f));
        entries.add(new BarEntry(3, 2f));
        entries.add(new BarEntry(4, 2.2f));
        entries.add(new BarEntry(5, 2.5f));
        entries.add(new BarEntry(6, 2.8f));
        entries.add(new BarEntry(7, 1.7f));
        entries.add(new BarEntry(8, 2.2f));
        entries.add(new BarEntry(9, 1.5f));

        YAxis yAxis = binding.barChart.getAxisLeft();
        yAxis.setAxisMaximum(3f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisLineWidth(0.5f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(7, true); // Nếu muốn cố định có số 3(giá trị max) thì cho là true, nếu không thì bỏ true

        BarDataSet dataSet = new BarDataSet(entries, "Phòng");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(dataSet);
        binding.barChart.setData(barData);
        binding.barChart.animateY(1000);

        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.invalidate();

        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
        binding.barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChart.getXAxis().setGranularity(1f);
        binding.barChart.getXAxis().setLabelCount(10);
        binding.barChart.getXAxis().setGranularityEnabled(true);
    }

    private void setupLineChart(Map<String, Long> monthYearTotalMap) {
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
            String monthYearKey = (i + 1) + "-" + year; // Thay đổi năm nếu cần
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