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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.poly.nhtr.Adapter.RevenueOfMonthAdapter;
import edu.poly.nhtr.Adapter.RevenueOfRoomAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentStatisticBinding;
import edu.poly.nhtr.models.RevenueOfMonthModel;
import edu.poly.nhtr.models.RevenueOfRoomModel;


public class StatisticFragment extends Fragment {
    private FragmentStatisticBinding binding;
    private RevenueOfMonthAdapter revenueOfMonthAdapter;
    private RevenueOfRoomAdapter revenueOfRoomAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentStatisticBinding.inflate(getLayoutInflater());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        setupLineChart();
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
        revenueOfMonthAdapter = new RevenueOfMonthAdapter(requireContext(), getRevenueOfMonthList());
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


    private List<RevenueOfMonthModel> getRevenueOfMonthList() {
        List<RevenueOfMonthModel> revenueOfMonthList = new ArrayList<>();
        revenueOfMonthList.add(new RevenueOfMonthModel("T1", 2000000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T2", 2000000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T3", 2700000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T4", 2800000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T5", 2500000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T6", 3000000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T7", 2800000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T8", 2700000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T9", 2000000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T10", 3000000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T11", 2600000L));
        revenueOfMonthList.add(new RevenueOfMonthModel("T12", 2600000L));

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
                setupLineChart();
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
                setupLineChart();
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

    private void setupLineChart() {
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
        ;
        xAxis.enableGridDashedLine(10f, 10f, 0f);


        YAxis yAxis = binding.lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(4.0f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setLabelCount(10);
        // Thiết lập đường lưới nét đứt cho trục Y
        yAxis.enableGridDashedLine(10f, 10f, 0f); // (khoảng cách nét, khoảng cách đứt, phase)

        // Nếu muốn thiết lập cho trục Y bên phải (nếu có sử dụng)
        YAxis rightAxis = binding.lineChart.getAxisRight();
        rightAxis.setDrawLabels(false); // Ẩn nhãn trục Y bên phải
        rightAxis.enableGridDashedLine(10f, 10f, 0f); // (khoảng cách nét, khoảng cách đứt, phase)

        List<Entry> entries1 = new ArrayList<>();
        entries1.add(new Entry(0, 2.0f));
        entries1.add(new Entry(1, 2.0f));
        entries1.add(new Entry(2, 2.7f));
        entries1.add(new Entry(3, 2.8f));
        entries1.add(new Entry(4, 2.5f));
        entries1.add(new Entry(5, 3.0f));
        entries1.add(new Entry(6, 2.8f));
        entries1.add(new Entry(7, 2.7f));
        entries1.add(new Entry(8, 2.0f));
        entries1.add(new Entry(9, 3.0f));
        entries1.add(new Entry(10, 2.6f));
        entries1.add(new Entry(11, 2.6f));


        LineDataSet dataSet1 = new LineDataSet(entries1, "Doanh thu tháng");
        dataSet1.setColor(Color.BLUE);
        dataSet1.setDrawFilled(true);  // Bật chế độ tô màu phía dưới đường line

        // Đặt drawable tùy chỉnh cho vùng phía dưới đường line
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fill_color_of_line_chart);
        dataSet1.setFillDrawable(drawable);


        LineData lineData = new LineData(dataSet1);
        binding.lineChart.setData(lineData);
        binding.lineChart.invalidate();
    }
}