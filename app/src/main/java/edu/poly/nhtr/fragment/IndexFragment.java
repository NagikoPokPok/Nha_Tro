package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import edu.poly.nhtr.Activity.MonthPickerDialog;
import edu.poly.nhtr.Adapter.IndexAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentIndexBinding;
import edu.poly.nhtr.databinding.LayoutDialogDetailedIndexBinding;
import edu.poly.nhtr.interfaces.IndexInterface;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Index;
import edu.poly.nhtr.presenters.IndexPresenter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;


import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class IndexFragment extends Fragment implements IndexInterface {
    private IndexPresenter indexPresenter;
    private FragmentIndexBinding binding;
    private List<Index> list_index;
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

    private Dialog dialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentIndexBinding.inflate(getLayoutInflater());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       // Khởi tạo binding trong onCreateView
        binding = FragmentIndexBinding.inflate(inflater, container, false);
        dialog = new Dialog(requireActivity());
        indexPresenter = new IndexPresenter(this);

        assert getArguments() != null;
        Home home = (Home) getArguments().getSerializable("home");
        assert home != null;
        String homeID = home.getIdHome();
        indexPresenter.fetchRoomsAndAddIndex(homeID);
        indexPresenter.fetchIndexesAndStoreInList(homeID);


        setupLayout();
        setupRecyclerView();
        setupPagination();
        setupDeleteRows();
        setupMonthPicker();


        return binding.getRoot();
    }

    public List<Index> getList_index() {
        return list_index;
    }

    public void setList_index(List<Index> list_index) {
        this.list_index = list_index;
    }

    public void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Spannable customizeText(String s)  // Hàm set mau va font chu cho Text
    {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }

    private void setupDialog(Index index) {
        LayoutDialogDetailedIndexBinding binding = LayoutDialogDetailedIndexBinding.inflate(getLayoutInflater());
        dialog.setContentView(binding.getRoot());
        setupDialogWindow(binding.getRoot().getLayoutParams());

        binding.txtTitleDialog.append(index.getNameRoom());

        setupTextViews(binding);
        setupEditTexts(binding, index);

        setupIndexCalculations(binding, index);

        dialog.setCancelable(true);
        dialog.show();
        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void setupIndexCalculations(LayoutDialogDetailedIndexBinding binding, Index index) {
        updateElectricityIndexCalculation(binding);
        updateWaterIndexCalculation(binding);

        addIndexTextWatchers(binding);
    }

    private void updateWaterIndexCalculation(LayoutDialogDetailedIndexBinding binding) {
        String indexWaterOld = Objects.requireNonNull(binding.edtWaterIndexOld.getText()).toString().trim();
        String indexWaterNew = Objects.requireNonNull(binding.edtWaterIndexNew.getText()).toString().trim();
        int indexUsed = Integer.parseInt(indexWaterNew) - Integer.parseInt(indexWaterOld);
        binding.edtWaterIndexCalculated.setText(String.valueOf(indexUsed));
    }

    private void updateElectricityIndexCalculation(LayoutDialogDetailedIndexBinding binding) {
        String indexElectricityOld = Objects.requireNonNull(binding.edtElectricityIndexOld.getText()).toString().trim();
        String indexElectricityNew = Objects.requireNonNull(binding.edtElectricityIndexNew.getText()).toString().trim();
        int indexUsed = Integer.parseInt(indexElectricityNew) - Integer.parseInt(indexElectricityOld);
        binding.edtElectricityIndexCalculated.setText(String.valueOf(indexUsed));
    }

    private void addIndexTextWatchers(LayoutDialogDetailedIndexBinding binding) {
        addTextWatcher(binding.edtElectricityIndexOld, binding.edtElectricityIndexNew, () -> updateElectricityIndexCalculation(binding));
        addTextWatcher(binding.edtElectricityIndexNew, binding.edtElectricityIndexOld, () -> updateElectricityIndexCalculation(binding));
        addTextWatcher(binding.edtWaterIndexOld, binding.edtWaterIndexNew, () -> updateWaterIndexCalculation(binding));
        addTextWatcher(binding.edtWaterIndexNew, binding.edtWaterIndexOld, () -> updateWaterIndexCalculation(binding));
    }

    private void addTextWatcher(TextInputEditText editText, TextInputEditText otherEditText, Runnable calculationMethod) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculationMethod.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
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
        adapter = new IndexAdapter(requireActivity(), new ArrayList<>(), this);
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

    public List<Index> getList() {

        List<Index> index_list = new ArrayList<>();
        for(int i = 0; i < getList_index().size(); i++)
        {
            index_list.add(new Index(getList_index().get(i).getNameRoom(), getList_index().get(i).getElectricityIndexOld(), getList_index().get(i).getElectricityIndexNew()
            , getList_index().get(i).getWaterIndexOld(), getList_index().get(i).getWaterIndexNew()));
        }
        return index_list;
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


}