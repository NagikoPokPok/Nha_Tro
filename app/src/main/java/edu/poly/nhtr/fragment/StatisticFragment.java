package edu.poly.nhtr.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentStatisticBinding;


public class StatisticFragment extends Fragment {
    private FragmentStatisticBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentStatisticBinding.inflate(getLayoutInflater());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Description description = new Description();
        description.setText("Doanh thu tháng");
        description.setPosition(160f, 15f);
        binding.lineChart.setDescription(description);
        binding.lineChart.getAxisRight().setDrawLabels(false);

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

        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}