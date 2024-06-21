package edu.poly.nhtr.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.poly.nhtr.Activity.MonthPickerDialog;
import edu.poly.nhtr.Adapter.IndexAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentHomeBinding;
import edu.poly.nhtr.databinding.FragmentIndexBinding;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Index;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class IndexFragment extends Fragment implements IndexInterface {
    private FragmentIndexBinding binding;
    protected long backpressTime;

    private boolean isNextClicked = false; // Track if Next button has been clicked
    private boolean isCheckBoxClicked = false;

    IndexAdapter adapter;
    int currentPage = 0;
    int totalPages = 12;
    private boolean visible = true; // Check dialog is open or not
    private String date = ""; // Show month/year
    private int currentMonth;
    private int currentYear;
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentIndexBinding.inflate(getLayoutInflater());



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       // Khởi tạo binding trong onCreateView
        binding = FragmentIndexBinding.inflate(inflater, container, false);

        setupLayout();
        setupRecyclerView();
        setupPagination();
        setupDeleteRows();
        setupMonthPicker();


        return binding.getRoot();
    }

    private void setupLayout() {
        binding.btnPrevious.setEnabled(false); // Disable previous button initially
        binding.btnNext.setEnabled(true);

        binding.btnNext.setOnClickListener(v -> {
            if (!isNextClicked) {
                binding.layoutElectricityIndexOld.setVisibility(View.GONE);
                binding.layoutElectricityIndexNew.setVisibility(View.GONE);
                binding.layoutWaterIndexOld.setVisibility(View.VISIBLE);
                binding.layoutWaterIndexNew.setVisibility(View.VISIBLE);
                isNextClicked = true;
                adapter.setNextClicked(isNextClicked);
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
                adapter.setNextClicked(isNextClicked);
                updateButtonsState();
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new IndexAdapter(requireActivity(), getList(), this);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupPagination() {
        binding.btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateButtonPageState();
                scrollToSelectedButton(binding.linearScroll.getChildAt(currentPage));
            }
        });

        binding.btnPreviousPage.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                updateButtonPageState();
                scrollToSelectedButton(binding.linearScroll.getChildAt(currentPage));
            }
        });

        for (int j = 0; j < totalPages; j++) {
            final int k = j;
            final Button btnPage = new Button(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(10, 0, 10, 0);
            btnPage.setTextColor(Color.WHITE);
            btnPage.setTextSize(13.0f);
            btnPage.setId(j);
            btnPage.setText(String.valueOf(j + 1));
            btnPage.setBackground(getResources().getDrawable(R.drawable.background_page_number_index));

            binding.linearScroll.addView(btnPage, lp);
            btnPage.setOnClickListener(v -> {
                currentPage = k;
                updateButtonPageState();
                scrollToSelectedButton(btnPage);
            });
        }
        updateButtonPageState();
    }

    private void setupDeleteRows() {
        binding.layoutDelete.setOnClickListener(v -> {
            binding.btnDelete.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            binding.txtDelete.setTextColor(getResources().getColorStateList(R.color.colorPrimary));
            binding.layoutDelete.setBackground(getResources().getDrawable(R.drawable.background_delete_index_pressed));
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
    }

    private void setupMonthPicker() {
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        binding.imgCalendar.setOnClickListener(v -> {
            visible = true;
            showMonthPicker(); // Open dialog
        });

        String monthYear = (currentMonth + 1) + "/" + currentYear;
        binding.txtDateTime.setText(monthYear);
    }

    private void showMonthPicker() {
        MonthPickerDialog monthPickerDialog = new MonthPickerDialog(requireContext(), currentMonth, currentYear,
                new MonthPickerDialog.OnMonthSelectedListener() {
                    @Override
                    public void onMonthSelected(int month, int year) {
                        date = month + "/" + year; // month = selectedMonthPosition + 1 ==> month == actual value
                        visible = false;
                        binding.txtDateTime.setText(date);
                        currentMonth = month - 1; // Cập nhật currentMonth, have to minus 1
                        currentYear = year; // Cập nhật year
                    }

                    @Override
                    public void onCancel() {
                        visible = false;
                    }
                });

        Objects.requireNonNull(monthPickerDialog.getWindow()).setBackgroundDrawableResource(R.drawable.background_dialog_index);
        monthPickerDialog.show();
    }

    private void updateButtonPageState() {
        binding.btnPreviousPage.setEnabled(currentPage > 0);
        binding.btnNextPage.setEnabled(currentPage < totalPages - 1);

        binding.btnPreviousPage.setTextColor(getResources().getColor(
                binding.btnPreviousPage.isEnabled() ? R.color.colorButton : R.color.colorGray));
        binding.btnNextPage.setTextColor(getResources().getColor(
                binding.btnNextPage.isEnabled() ? R.color.colorButton : R.color.colorGray));

        for (int i = 0; i < binding.linearScroll.getChildCount(); i++) {
            Button btnPage = (Button) binding.linearScroll.getChildAt(i);
            if (i == currentPage) {
                btnPage.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            } else {
                btnPage.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
            }
        }
    }

    private void scrollToSelectedButton(View button) {
        binding.scroll.post(() -> {
            int scrollX = button.getLeft() - (binding.scroll.getWidth() - button.getWidth()) / 2;
            binding.scroll.smoothScrollTo(scrollX, 0);
        });
    }

    private List<Index> getList() {
        List<Index> payment_list = new ArrayList<>();
        payment_list.add(new Index("1", "123456", "645646", "000333", "009999"));
        payment_list.add(new Index("2", "123456", "645646", "000333", "009999"));
        payment_list.add(new Index("3", "123456", "645646", "000333", "009999"));
        payment_list.add(new Index("4", "123456", "645646", "000333", "009999"));
        payment_list.add(new Index("5", "123456", "645646", "000333", "009999"));

        return payment_list;
    }

    private void updateButtonsState() {
        if (!isNextClicked) {
            binding.btnPrevious.setEnabled(false);
            binding.btnPrevious.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
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
                    binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                } else {
                    binding.btnNext.setEnabled(true);
                    binding.btnNext.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorTextBlack)));
                }
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        if (backpressTime + 1000 > System.currentTimeMillis()) {
//            super.onBackPressed();
//            return;
//        } else {
//            Toast.makeText(this, "Press Back Again to Exit", Toast.LENGTH_SHORT).show();
//            binding.btnDelete.setBackgroundTintList(getResources().getColorStateList(R.color.colorGray));
//            binding.txtDelete.setTextColor(getResources().getColorStateList(R.color.colorGray));
//            binding.layoutDelete.setBackground(getResources().getDrawable(R.drawable.background_delete_normal));
//            binding.layoutDeleteManyRows.setVisibility(View.GONE);
//            adapter.isDeleteClicked(false);
//        }
//        backpressTime = System.currentTimeMillis();
//    }
}