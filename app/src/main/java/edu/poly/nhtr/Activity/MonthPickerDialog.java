package edu.poly.nhtr.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import edu.poly.nhtr.R;

public class MonthPickerDialog extends Dialog {

    public interface OnMonthSelectedListener {
        void onMonthSelected(int month, int year);
        void onCancel();
    }

    private final OnMonthSelectedListener listener;
    private int selectedMonthPosition;
    private int selectedYear;
    private final String[] months;
    private ArrayAdapter<String> adapter;
    private TextView yearTextView;

    ImageView decreaseYearButton, increaseYearButton;

    public MonthPickerDialog(@NonNull Context context, int currentMonth, int currentYear, OnMonthSelectedListener listener) {
        super(context);
        this.listener = listener;
        this.selectedMonthPosition = currentMonth;
        this.selectedYear = currentYear;
        this.months = context.getResources().getStringArray(R.array.months);
        setContentView(R.layout.layout_dialog_month_picker);
        setupUI();
    }

    private void setupUI() {
        yearTextView = findViewById(R.id.year_text);
        GridView monthsGridView = findViewById(R.id.monthsGridView);
        decreaseYearButton = findViewById(R.id.decreaseYearButton);
        increaseYearButton = findViewById(R.id.increaseYearButton);
        TextView cancelButton = findViewById(R.id.cancel_button);
        TextView okButton = findViewById(R.id.ok_button);

        yearTextView.setText(String.valueOf(selectedYear));
        updateYearButtonsState();

        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, months) {
            @NonNull
            @Override
            public View getView(int position,  View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.gridview_item_month, parent, false);
                }

                TextView textView = convertView.findViewById(R.id.text_month);
                FrameLayout backgroundView = convertView.findViewById(R.id.background_view);

                textView.setText(months[position]);
                textView.setTextColor(position == selectedMonthPosition ? Color.WHITE : Color.BLACK);
                backgroundView.setVisibility(position == selectedMonthPosition ? View.VISIBLE : View.INVISIBLE);

                if (position == selectedMonthPosition) {
                    animateSelection(backgroundView);
                }

                return convertView;
            }
        };

        monthsGridView.setAdapter(adapter);

        // Handle click on month grid item
        monthsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedMonthPosition = position;
                adapter.notifyDataSetChanged(); // Update UI on selection
            }
        });

        decreaseYearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedYear > 2020) {
                    selectedYear--;
                    yearTextView.setText(String.valueOf(selectedYear));
                    updateYearButtonsState();
                }
            }
        });

        increaseYearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedYear < 2030) {
                    selectedYear++;
                    yearTextView.setText(String.valueOf(selectedYear));
                    updateYearButtonsState();
                }
            }
        });



        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMonthSelected(selectedMonthPosition + 1, selectedYear); // month is 0-based, so that we have to plus 1
                dismiss();
            }
        });
    }

    private void updateYearButtonsState() {
        if (selectedYear <= 2020) {
            decreaseYearButton.setEnabled(false);
            decreaseYearButton.setColorFilter(Color.GRAY);
        } else {
            decreaseYearButton.setEnabled(true);
            decreaseYearButton.setColorFilter(Color.BLACK);
        }

        if (selectedYear >= 2030) {
            increaseYearButton.setEnabled(false);
            increaseYearButton.setColorFilter(Color.GRAY);
        } else {
            increaseYearButton.setEnabled(true);
            increaseYearButton.setColorFilter(Color.BLACK);
        }
    }

//    public void setSelectedDate(int month, int year) {
//        this.selectedMonthPosition = month - 1; // Because index of month in array months begin 0 ==> selectedMonthPosition = value of month - 1
//        this.selectedYear = year;
//        adapter.notifyDataSetChanged(); // Update interface of adapter
//    }

//    private void animateSelection(View view) {
//        // Scale up animation
//        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.0f);
//        scaleX.setDuration(300);  // Duration in milliseconds
//        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
//
//        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.0f);
//        scaleY.setDuration(300);  // Duration in milliseconds
//        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
//
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(scaleX, scaleY);
//        animatorSet.start();
//    }

    private void animateSelection(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.0f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }
}
