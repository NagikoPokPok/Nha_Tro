package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import edu.poly.nhtr.Activity.MainRoomActivity;
import edu.poly.nhtr.Adapter.HomeAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentHomeBinding;
import edu.poly.nhtr.databinding.ItemContainerHomesBinding;
import edu.poly.nhtr.listeners.HomeListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.presenters.HomePresenter;
import edu.poly.nhtr.utilities.Constants;
import edu.poly.nhtr.utilities.PreferenceManager;

public class HomeFragment extends Fragment implements HomeListener, SwipeRefreshLayout.OnRefreshListener {

    private View view;
    private List<Home> currentListHomes = new ArrayList<>();
    private PreferenceManager preferenceManager;
    private FragmentHomeBinding binding;
    private HomePresenter homePresenter;
    private Dialog dialog;
    private HomeAdapter homeAdapter;
    private boolean isSelectAllChecked = false;
    private boolean isLoadingFinished = false;

    public List<Home> getCurrentListHomes() {
        return currentListHomes;
    }

    public void setCurrentListHomes(List<Home> currentListHomes) {
        this.currentListHomes = currentListHomes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(requireActivity().getApplicationContext());
        dialog = new Dialog(requireActivity());

        // Khai bao presenter
        homePresenter = new HomePresenter(this);


        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        binding.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.edtSearchHome.clearFocus();
                binding.rootLayout.requestFocus();
            }
        });

        homePresenter.getListHomes(new HomePresenter.OnGetHomesCompleteListener() {
            @Override
            public void onComplete(List<Home> homeList) {
                currentListHomes = homeList;

            }
        });

        homeAdapter = new HomeAdapter(currentListHomes, this, this);


        editFonts();
        // Load home information
        homePresenter.getHomes("init");

        // Refresh layout
        binding.swipeRefreshFragment.setOnRefreshListener(this);


        //Set preference
        preferenceManager = new PreferenceManager(requireContext().getApplicationContext());

        // Remove the status of radio button
        preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);

        // Remove the status of check box
        removeStatusOfCheckBoxFilterHome();

        // Set up RecyclerView layout manager
        binding.homesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));


        // Load user's information
        loadUserDetails();

        getToken();


        setListeners();

        // Xử lý Dialog Thêm nhà trọ
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding.btnAddHome.setOnClickListener(view -> {
            openAddHomeDialog(Gravity.CENTER);
        });

        // Xử lý nút 3 chấm menu in Frame Top
        binding.imgMenuEditDelete.setOnClickListener(this::openMenu);

        setupLayoutDeleteHomes();


        customizeLayoutSearch();// Customize layout search
        setListenersForTools(); // Set listeners for sort and filter


        //mainLogic();

    }

    private void setupLayoutDeleteHomes() {
        binding.checkboxSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSelectAllChecked = !isSelectAllChecked;
                homeAdapter.isSelectAllChecked(isSelectAllChecked);
            }
        });

    }

    @Override
    public void onRefresh() {
        isLoadingFinished = false;
        // Load home information
        homePresenter.getHomes("init");
        hideLayoutDeleteHomes();
        binding.edtSearchHome.clearFocus();
        binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
        binding.layoutTypeOfSortHome.setVisibility(View.GONE);
        removeStatusOfCheckBoxFilterHome();
        preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);

        // Sử dụng Handler để kiểm tra trạng thái tải
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoadingFinished) {
                    binding.swipeRefreshFragment.setRefreshing(false);
                } else {
                    // Kiểm tra lại sau một khoảng thời gian ngắn nếu cần thiết
                    new Handler(Looper.getMainLooper()).postDelayed(this, 500);
                }
            }
        }, 500); // Thời gian kiểm tra ban đầu
    }


    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }


    private void updateToken(String token) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Unable to update token");
                    }
                });
    }

    private void setListenersForTools() {
        binding.btnSortHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSortHomeDialog();
            }
        });

        binding.imgSortHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSortHomeDialog();
            }
        });

        binding.btnFilterHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.getListHomes(new HomePresenter.OnGetHomesCompleteListener() {
                    @Override
                    public void onComplete(List<Home> homeList) {
                        currentListHomes = homeList;
                        openFilterHomeDialog(homeList);
                    }
                });
            }
        });

        binding.imgFilterHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.getListHomes(new HomePresenter.OnGetHomesCompleteListener() {
                    @Override
                    public void onComplete(List<Home> homeList) {
                        currentListHomes = homeList;
                        openFilterHomeDialog(homeList);
                    }
                });
            }
        });

    }


    private void removeFromListAndSave(String option) { // Remove from listType
        for (int i = 0; i < binding.listTypeOfFilterHome.getChildCount(); i++) {
            View view = binding.listTypeOfFilterHome.getChildAt(i);
            if (view instanceof LinearLayout) {
                TextView textView = view.findViewById(R.id.txt_type_of_filter_home);
                if (textView.getText().toString().equals(option)) {
                    binding.listTypeOfFilterHome.removeView(view);
                    preferenceManager.removePreference(option);
                    break;
                }
            }
        }
    }

    private void filterListHomes(int range1, int range2, int range3, long range4, long range5, long range6) {
        showLoadingOfFunctions(R.id.btn_confirm_apply);

        boolean filterByRoom1 = preferenceManager.getBoolean("cbxByRoom1");
        boolean filterByRoom2 = preferenceManager.getBoolean("cbxByRoom2");
        boolean filterByRoom3 = preferenceManager.getBoolean("cbxByRoom3");
        boolean filterByRevenue1 = preferenceManager.getBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_1);
        boolean filterByRevenue2 = preferenceManager.getBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_2);
        boolean filterByRevenue3 = preferenceManager.getBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_3);

        List<Home> filteredHomesByRooms = new ArrayList<>();
        List<Home> filteredHomesByRevenue = new ArrayList<>();
        List<Home> filteredHomes = new ArrayList<>();

        // Lọc theo phòng
        if (filterByRoom1 || filterByRoom2 || filterByRoom3) {
            for (Home home : currentListHomes) {
                if (filterByRoom1 && home.numberOfRooms >= 0 && home.numberOfRooms <= range1) {
                    filteredHomesByRooms.add(home);
                } else if (filterByRoom2 && home.numberOfRooms >= (range1 + 1) && home.numberOfRooms <= range2) {
                    filteredHomesByRooms.add(home);
                } else if (filterByRoom3 && home.numberOfRooms >= (range2 + 1) && home.numberOfRooms <= range3) {
                    filteredHomesByRooms.add(home);
                }
            }
        }

        // Lọc theo doanh thu
        if (filterByRevenue1 || filterByRevenue2 || filterByRevenue3) {
            for (Home home : currentListHomes) {
                if (filterByRevenue1 && home.revenueOfMonth >= 0 && home.revenueOfMonth <= range4) {
                    filteredHomesByRevenue.add(home);
                } else if (filterByRevenue2 && home.revenueOfMonth >= (range4 + 1) && home.revenueOfMonth <= range5) {
                    filteredHomesByRevenue.add(home);
                } else if (filterByRevenue3 && home.revenueOfMonth >= (range5 + 1) && home.revenueOfMonth <= range6) {
                    filteredHomesByRevenue.add(home);
                }
            }
        }

        // Kết hợp kết quả lọc
        if ((filterByRoom1 || filterByRoom2 || filterByRoom3) && (filterByRevenue1 || filterByRevenue2 || filterByRevenue3)) {
            if (!filteredHomesByRooms.isEmpty() && !filteredHomesByRevenue.isEmpty()) {
                Set<Home> setRooms = new HashSet<>(filteredHomesByRooms);
                Set<Home> setRevenue = new HashSet<>(filteredHomesByRevenue);
                setRooms.retainAll(setRevenue); // Giao của hai tập hợp
                filteredHomes.addAll(setRooms);
            }
            // Nếu một trong hai danh sách rỗng, kết quả cuối cùng sẽ là rỗng
        } else {
            if (!filteredHomesByRooms.isEmpty()) {
                filteredHomes.addAll(filteredHomesByRooms);
            }
            if (!filteredHomesByRevenue.isEmpty()) {
                filteredHomes.addAll(filteredHomesByRevenue);
            }
        }

        homePresenter.filterHome(filteredHomes);
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

    private void openFilterHomeDialog(List<Home> currentListHomes) {

        if (binding.edtSearchHome.isFocused()) {
            binding.edtSearchHome.clearFocus();
            binding.edtSearchHome.setText("");
            homePresenter.getHomes("init");
        } else if (binding.layoutTypeOfSortHome.getVisibility() == View.VISIBLE) {
            // Clear the selected RadioButton ID from SharedPreferences
            preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
            binding.layoutTypeOfSortHome.setVisibility(View.GONE);
            homePresenter.getHomes("init");
        }


        setupDialog(R.layout.layout_dialog_filter_home, Gravity.CENTER);


        AppCompatCheckBox cbxByRoom1 = dialog.findViewById(R.id.cbx_from_0_to_5_rooms);
        AppCompatCheckBox cbxByRoom2 = dialog.findViewById(R.id.cbx_from_6_to_10_rooms);
        AppCompatCheckBox cbxByRoom3 = dialog.findViewById(R.id.cbx_from_10_to_more_rooms);

        AppCompatCheckBox cbxByRevenue1 = dialog.findViewById(R.id.cbx_from_0_to_3_millions);
        AppCompatCheckBox cbxByRevenue2 = dialog.findViewById(R.id.cbx_from_3_to_7_millions);
        AppCompatCheckBox cbxByRevenue3 = dialog.findViewById(R.id.cbx_from_7_to_more_millions);

        Button btnApply = dialog.findViewById(R.id.btn_confirm_apply);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);


        // Bước 1: Tìm số lượng phòng lớn nhất
        int maxRooms = 0;
        for (Home home : currentListHomes) {
            int numberOfRooms = home.getNumberOfRooms();
            if (numberOfRooms > maxRooms) {
                maxRooms = numberOfRooms;
            }
        }

        // Bước 2: Chia số lượng phòng lớn nhất thành ba khoảng
        int range1Max, range2Max, range3Max;
        if (maxRooms == 0) {
            range1Max = range2Max = range3Max = 0;
        } else {
            range1Max = (int) Math.ceil((double) maxRooms / 3.0);  // Kích thước mỗi khoảng
            range2Max = 2 * range1Max;
            range3Max = maxRooms;
        }

        // Bước 3: Thiết lập văn bản và hiển thị các checkbox
        cbxByRoom1.setText("Từ " + 0 + " - " + range1Max + " phòng");

        if (range2Max > range1Max) {
            if ((range1Max + 1) != range2Max) {
                cbxByRoom2.setText("Từ " + (range1Max + 1) + " - " + range2Max + " phòng");
            } else {
                cbxByRoom2.setText("Từ " + (range1Max + 1) + " phòng");
            }
            cbxByRoom2.setVisibility(View.VISIBLE);
        } else {
            cbxByRoom2.setVisibility(View.GONE);
        }

        if (range3Max > range2Max) {
            if ((range2Max + 1) != range3Max) {
                cbxByRoom3.setText("Từ " + (range2Max + 1) + " - " + range3Max + " phòng");
            } else {
                cbxByRoom3.setText("Từ " + (range2Max + 1) + " phòng");
            }
            cbxByRoom3.setVisibility(View.VISIBLE);
        } else {
            cbxByRoom3.setVisibility(View.GONE);
        }

        long maxRevenue = 0;
        for (Home home : currentListHomes) {
            long revenueOfMonth = home.getRevenueOfMonth();
            if (revenueOfMonth > maxRevenue) {
                maxRevenue = revenueOfMonth;
            }
        }

        long range4Max, range5Max, range6Max;
        if (maxRevenue == 0) {
            range4Max = range5Max = range6Max = 0;
        } else {
            range4Max = (long) Math.ceil((double) maxRevenue / 3.0);  // Kích thước mỗi khoảng
            range5Max = 2 * range4Max;
            range6Max = maxRevenue;
        }

        // Bước 3: Thiết lập văn bản và hiển thị các checkbox
        cbxByRevenue1.setText(String.format(Locale.US, "Từ 0 - %.2f triệu", range4Max / 1000000F));

        if (range5Max > range4Max) {
            if ((range4Max + 1) != range5Max) {
                cbxByRevenue2.setText(String.format(Locale.US, "Từ %.2f - %.2f triệu", (range4Max + 1) / 1000000F, range5Max / 1000000F));
            } else {
                cbxByRevenue2.setText(String.format(Locale.US, "Từ %.2f triệu", (range4Max + 1) / 1000000F));
            }
            cbxByRevenue2.setVisibility(View.VISIBLE);
        } else {
            cbxByRevenue2.setVisibility(View.GONE);
        }

        if (range6Max > range5Max) {
            if ((range5Max + 1) != range6Max) {
                cbxByRevenue3.setText(String.format(Locale.US, "Từ %.2f - %.2f triệu", (range5Max + 1) / 1000000F, range6Max / 1000000F));
            } else {
                cbxByRevenue3.setText(String.format(Locale.US, "Từ %.2f triệu", (range5Max + 1) / 1000000F));
            }
            cbxByRevenue3.setVisibility(View.VISIBLE);
        } else {
            cbxByRevenue3.setVisibility(View.GONE);
        }


        cbxByRoom1.setChecked(preferenceManager.getBoolean("cbxByRoom1"));
        cbxByRoom2.setChecked(preferenceManager.getBoolean("cbxByRoom2"));
        cbxByRoom3.setChecked(preferenceManager.getBoolean("cbxByRoom3"));

        cbxByRevenue1.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_1));
        cbxByRevenue2.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_2));
        cbxByRevenue3.setChecked(preferenceManager.getBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_3));

        // Add CheckBoxes to a list
        List<AppCompatCheckBox> checkBoxList = new ArrayList<>();
        checkBoxList.add(cbxByRoom1);
        checkBoxList.add(cbxByRoom2);
        checkBoxList.add(cbxByRoom3);
        checkBoxList.add(cbxByRevenue1);
        checkBoxList.add(cbxByRevenue2);
        checkBoxList.add(cbxByRevenue3);


        customizeButtonApplyInDialogHaveCheckBox(btnApply, checkBoxList);


        // Create a method to check the state of all checkboxes
        View.OnClickListener checkBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customizeButtonApplyInDialogHaveCheckBox(btnApply, checkBoxList);
            }
        };

        // Set the listener to all checkboxes
        cbxByRoom1.setOnClickListener(checkBoxListener);
        cbxByRoom2.setOnClickListener(checkBoxListener);
        cbxByRoom3.setOnClickListener(checkBoxListener);
        cbxByRevenue1.setOnClickListener(checkBoxListener);
        cbxByRevenue2.setOnClickListener(checkBoxListener);
        cbxByRevenue3.setOnClickListener(checkBoxListener);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutTypeOfFilterHome.setVisibility(View.VISIBLE);
                List<String> selectedOptions = new ArrayList<>();

                // Check which CheckBoxes are selected and save their state
                boolean filterByRoom1 = cbxByRoom1.isChecked();
                boolean filterByRoom2 = cbxByRoom2.isChecked();
                boolean filterByRoom3 = cbxByRoom3.isChecked();
                boolean filterByRevenue1 = cbxByRevenue1.isChecked();
                boolean filterByRevenue2 = cbxByRevenue2.isChecked();
                boolean filterByRevenue3 = cbxByRevenue3.isChecked();

                if (filterByRoom1) {
                    selectedOptions.add(cbxByRoom1.getText().toString());
                    preferenceManager.putBoolean("cbxByRoom1", true);
                } else {
                    preferenceManager.putBoolean("cbxByRoom1", false);
                    removeFromListAndSave(cbxByRoom1.getText().toString());
                }
                if (filterByRoom2) {
                    selectedOptions.add(cbxByRoom2.getText().toString());
                    preferenceManager.putBoolean("cbxByRoom2", true);
                } else {
                    preferenceManager.putBoolean("cbxByRoom2", false);
                    removeFromListAndSave(cbxByRoom2.getText().toString());
                }
                if (filterByRoom3) {
                    selectedOptions.add(cbxByRoom3.getText().toString());
                    preferenceManager.putBoolean("cbxByRoom3", true);
                } else {
                    preferenceManager.putBoolean("cbxByRoom3", false);
                    removeFromListAndSave(cbxByRoom3.getText().toString());
                }

                if (filterByRevenue1) {
                    selectedOptions.add(cbxByRevenue1.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_1, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_1, false);
                    removeFromListAndSave(cbxByRevenue1.getText().toString());
                }
                if (filterByRevenue2) {
                    selectedOptions.add(cbxByRevenue2.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_2, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_2, false);
                    removeFromListAndSave(cbxByRevenue2.getText().toString());
                }
                if (filterByRevenue3) {
                    selectedOptions.add(cbxByRevenue3.getText().toString());
                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_3, true);
                } else {
                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_3, false);
                    removeFromListAndSave(cbxByRevenue3.getText().toString());
                }

                // If 3 check boxes are unchecked -> Hide layoutTypeOfFilterHomes
                if (!filterByRoom1 && !filterByRoom2 && !filterByRoom3 && !filterByRevenue1 && !filterByRevenue2 && !filterByRevenue3) {
                    binding.layoutNoData.setVisibility(View.GONE);
                    binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
                    homePresenter.getHomes("init");
                } else {
                    filterListHomes(range1Max, range2Max, range3Max, range4Max, range5Max, range6Max); // After put status of checkboxes in preferences, check and add them into the list
                }

                // Add selected options as LinearLayouts with TextView and ImageView to the main LinearLayout
                for (String option : selectedOptions) {
                    // Check if the checkbox is already in the listTypeOfFilterHome
                    boolean alreadyExists = false;
                    for (int i = 0; i < binding.listTypeOfFilterHome.getChildCount(); i++) {
                        View view = binding.listTypeOfFilterHome.getChildAt(i);
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
                                binding.listTypeOfFilterHome.removeView(filterItemLayout);

                                // Update SharedPreferences to uncheck the checkbox in the dialog
                                if (option.equals(cbxByRoom1.getText().toString())) {
                                    preferenceManager.putBoolean("cbxByRoom1", false);
                                    cbxByRoom1.setChecked(false);
                                } else if (option.equals(cbxByRoom2.getText().toString())) {
                                    preferenceManager.putBoolean("cbxByRoom2", false);
                                    cbxByRoom2.setChecked(false);
                                } else if (option.equals(cbxByRoom3.getText().toString())) {
                                    preferenceManager.putBoolean("cbxByRoom3", false);
                                    cbxByRoom3.setChecked(false);
                                } else if (option.equals(cbxByRevenue1.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_1, false);
                                    cbxByRevenue1.setChecked(false);
                                } else if (option.equals(cbxByRevenue2.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_2, false);
                                    cbxByRevenue2.setChecked(false);
                                } else if (option.equals(cbxByRevenue3.getText().toString())) {
                                    preferenceManager.putBoolean(Constants.KEY_CBX_REVENUE_OF_MONTH_3, false);
                                    cbxByRevenue3.setChecked(false);
                                }

                                if (binding.listTypeOfFilterHome.getChildCount() == 0) {
                                    // If no filter left in the list -> Set GONE
                                    binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
                                    binding.layoutNoData.setVisibility(View.GONE);
                                    // And update list homes as initial
                                    homePresenter.getHomes("init");
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
                        binding.listTypeOfFilterHome.addView(filterItemLayout);
                    }
                }
                //dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void getListHomes(List<Home> listHomes) {
        setCurrentListHomes(listHomes);
    }

    @Override
    public void showLayoutDeleteHomes() {
        binding.layoutDeleteManyHomes.setVisibility(View.VISIBLE);
    }

    @Override
    public void putListSelected(List<Home> listHomes) {
        binding.txtDeleteHomeHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDeleteButtonClick(listHomes);
            }
        });

        binding.txtCancelDeleteHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeAdapter.cancelDeleteAll();
            }
        });
    }

    private void handleDeleteButtonClick(List<Home> listHomes) {
        if (listHomes.isEmpty()) {
            showToast("Không có nhà nào được chọn!");
        } else {
            if (cannotDeleteHomes(listHomes)) {
                openCannotDeleteListHomesDialog();
            } else {
                openDeleteListHomeDialog(listHomes);
            }
        }
    }

    private void openCannotDeleteListHomesDialog() {
        setupDialog(R.layout.layout_dialog_cannot_delete_bill, Gravity.CENTER);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView txtTitle = dialog.findViewById(R.id.txt_title_of_cannot_delete);
        TextView txtBody = dialog.findViewById(R.id.txt_body_of_cannot_delete);
        txtTitle.setText("Không thể xoá nhà trọ ");
        txtBody.setText("Nhà trọ này hiện đang có người ở, bạn không thể xóa ngay lập tức !");

    }

    private boolean cannotDeleteHomes(List<Home> listHomes) {
        for (Home home : listHomes) {
            if (home.numberOfRooms != home.numberOfRoomsAvailable) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void hideLayoutDeleteHomes() {
        binding.layoutDeleteManyHomes.setVisibility(View.GONE);
    }

    private void openSortHomeDialog() {
        if (binding.edtSearchHome.isFocused()) {
            binding.edtSearchHome.clearFocus();
            binding.edtSearchHome.setText("");
            homePresenter.getHomes("init");
        } else if (binding.layoutTypeOfFilterHome.getVisibility() == View.VISIBLE) {
            removeStatusOfCheckBoxFilterHome();
            binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
            homePresenter.getHomes("init");
        }

        setupDialog(R.layout.layout_dialog_sort_home, Gravity.CENTER);

        RadioGroup radioGroup = dialog.findViewById(R.id.radio_group_sort_home);
        Button btnApply = dialog.findViewById(R.id.btn_confirm_apply);

        // Disable btnApply and set background color to gray initially
        btnApply.setEnabled(false);
        btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));

        // Listen for changes in the RadioGroup
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    // Enable btnApply and change background color to blue
                    btnApply.setEnabled(true);
                    btnApply.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
                }
            }
        });


        // Save the status of radio buttons
        int selectedRadioButtonId = preferenceManager.getInt(Constants.KEY_SELECTED_RADIO_BUTTON);

        // If a RadioButton was selected before, check it
        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = dialog.findViewById(selectedRadioButtonId);
            if (selectedRadioButton != null) {
                selectedRadioButton.setChecked(true);
            }
        }

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.txtTitleSortFilterHome.setText("Sắp xếp theo:  ");
                binding.layoutTypeOfSortHome.setVisibility(View.VISIBLE);

                // Get the selected RadioButton ID
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.radio_btn_number_room_asc) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_number_room_asc);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterHome.setText(selectedText);
                    homePresenter.sortHomes("number_room_asc");

                } else if (selectedId == R.id.radio_btn_number_room_desc) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_number_room_desc);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterHome.setText(selectedText);
                    homePresenter.sortHomes("number_room_desc");

                } else if (selectedId == R.id.radio_btn_revenue_asc) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_revenue_asc);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterHome.setText(selectedText);
                    homePresenter.sortHomes("revenue_asc");

                } else if (selectedId == R.id.radio_btn_revenue_desc) {
                    RadioButton selectedRadioButton = dialog.findViewById(R.id.radio_btn_revenue_desc);
                    String selectedText = selectedRadioButton.getText().toString();
                    binding.txtTypeOfSortFilterHome.setText(selectedText);
                    homePresenter.sortHomes("revenue_desc");

                } else {
                    showToast("No option selected");
                }

                // Save the selected RadioButton ID to SharedPreferences
                preferenceManager.putInt(Constants.KEY_SELECTED_RADIO_BUTTON, selectedId);
            }
        });

        binding.btnCancelSortHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.layoutTypeOfSortHome.setVisibility(View.GONE);
                preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
                homePresenter.getHomes("init");
            }
        });


        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void customizeLayoutSearch() {

        binding.layoutSearchHome.setEndIconDrawable(R.drawable.ic_search_orange);
        binding.layoutSearchHome.setEndIconVisible(true);
        binding.edtSearchHome.setHint("Tìm kiếm nhà trọ ...");

        binding.edtSearchHome.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (binding.layoutTypeOfSortHome.getVisibility() == View.VISIBLE) {
                        // Xoa sort khi click vao search
                        preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
                        binding.layoutTypeOfSortHome.setVisibility(View.GONE);
                        homePresenter.getHomes("init");
                    } else if (binding.layoutTypeOfFilterHome.getVisibility() == View.VISIBLE) {
                        // Clear the status of check boxes
                        removeStatusOfCheckBoxFilterHome();
                        binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
                        homePresenter.getHomes("init");
                    }

                    binding.layoutSearchHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    binding.imgSortHome.setEnabled(true);
                    binding.imgFilterHome.setEnabled(true);
                    binding.btnSortHome.setEnabled(true);
                    binding.btnFilterHome.setEnabled(true);
                }
            }
        });


        binding.edtSearchHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Xoa sort khi click vao search
                preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
                binding.layoutTypeOfSortHome.setVisibility(View.GONE);
                // Clear the status of check boxes
                removeStatusOfCheckBoxFilterHome();
                binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
                homePresenter.searchHome(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.layoutSearchHome.setEndIconOnClickListener(v -> {
            String searchNameHome = Objects.requireNonNull(Objects.requireNonNull(binding.edtSearchHome.getText()).toString().trim());
            homePresenter.searchHome(searchNameHome);

            if (searchNameHome.isEmpty()) {
                binding.edtSearchHome.clearFocus();
            }
        });
    }

    @Override
    public void noHomeData() {
        binding.homesRecyclerView.setVisibility(View.INVISIBLE);
        binding.layoutNoData.setVisibility(View.VISIBLE);
    }

    public void hideOtherComponents(String action) {
        if (binding.edtSearchHome.isFocused()) {
            binding.edtSearchHome.clearFocus();
            binding.edtSearchHome.setText("");
            homePresenter.getHomes(action);
        }
        // Clear the status of check boxes
        else if (binding.layoutTypeOfSortHome.getVisibility() == View.VISIBLE) {
            // Xoa sort khi click vao search
            preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
            binding.layoutTypeOfSortHome.setVisibility(View.GONE);
            homePresenter.getHomes(action);
        } else if (binding.layoutTypeOfFilterHome.getVisibility() == View.VISIBLE) {
            // Clear the status of check boxes
            removeStatusOfCheckBoxFilterHome();
            binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
            homePresenter.getHomes(action);
        }
    }


    private void openMenu(View view) {
        // Hide other components
        hideOtherComponents("init");

        // Get current list of homes
        homePresenter.getListHomes(new HomePresenter.OnGetHomesCompleteListener() {
            @Override
            public void onComplete(List<Home> homeList) {
                currentListHomes = homeList;
            }
        });

        // Set popup menus
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.inflate(R.menu.menu_select_homes_to_delete);
        popupMenu.show();
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_select_home_to_delete) {

                homeAdapter = new HomeAdapter(getCurrentListHomes(), this, this);
                getCurrentListHomes().sort(Comparator.comparing(obj -> obj.dateObject));
                updateRecyclerView(homeAdapter, 0);
                binding.homesRecyclerView.setAdapter(homeAdapter);

                binding.homesRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        RecyclerView.ViewHolder viewHolder = binding.homesRecyclerView.findViewHolderForAdapterPosition(0);
                        if (viewHolder instanceof HomeAdapter.HomeViewHolder) {
                            HomeAdapter.HomeViewHolder homeViewHolder = (HomeAdapter.HomeViewHolder) viewHolder;
                            homeAdapter.performClick(homeViewHolder);
                        }
                    }
                });

                return true;
            }
            return false;
        });

    }

    private void editFonts() {
        //Set three fonts into one textview
        Spannable text1 = new SpannableString("Bạn chưa có nhà trọ\n Hãy nhấn nút ");
        Typeface interLightTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text1.setSpan(new TypefaceSpan(interLightTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.setText(text1);

        Spannable text2 = new SpannableString("+");
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        text2.setSpan(new TypefaceSpan(interBoldTypeface), 0, text2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text2);

        Spannable text3 = new SpannableString(" để thêm nhà trọ.");
        Typeface interLightTypeface2 = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_light.ttf");
        text3.setSpan(new TypefaceSpan(interLightTypeface2), 0, text3.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtNotification.append(text3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);


        homePresenter.getListHomes(new HomePresenter.OnGetHomesCompleteListener() {
            @Override
            public void onComplete(List<Home> homeList) {
                currentListHomes = homeList;
            }
        });
        homeAdapter = new HomeAdapter(getCurrentListHomes(), this, this);
        return binding.getRoot();
    }

    private void setListeners() {
        // Kiểm tra tài khoản đăng nhập là tài khoản Email hay Google
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            List<? extends UserInfo> providerData = currentUser.getProviderData();
            // Lặp qua danh sách các tài khoản cấp thông tin xác thực
            for (UserInfo userInfo : providerData) {
                String providerId = userInfo.getProviderId();
                if (providerId.equals("google.com")) {
                    // TH đăng nhập bằng tài khoản Google
                    getInfoFromGoogle();
                    return; // Thoát khỏi vòng lặp khi thấy đúng tài khoản Google
                }
            }
            // Nếu là tài khoản Email thì tải thông tin người dùng từ SharedPreferences
            loadUserDetails();
        } else {
            // Không có người dùng nào đang đăng nhập, tải thông tin từ SharedPreferences
            loadUserDetails();
        }
    }

    private Bitmap getConversionImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int width = 150;
        int height = 150;
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void loadUserDetails() {
        String encodedImg = preferenceManager.getString(Constants.KEY_IMAGE);
        binding.name.setText(preferenceManager.getString(Constants.KEY_NAME));
        if (encodedImg != null && !encodedImg.isEmpty()) {
            try {
                Bitmap profileImage = getConversionImage(encodedImg);
                binding.imgProfile.setImageBitmap(profileImage);
                binding.imgAva.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
                binding.imgAva.setVisibility(View.VISIBLE); // Nếu không có ảnh thì để mặc định
                Toast.makeText(requireActivity().getApplicationContext(), "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định và ẩn ảnh người dùng
            binding.imgAva.setVisibility(View.VISIBLE);
        }
    }


    // Lấy ảnh đại diện và tên từ Google
    private void getInfoFromGoogle() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            String userName = account.getDisplayName();
            binding.name.setText(userName);

            String photoUrl = Objects.requireNonNull(account.getPhotoUrl()).toString();
            new HomeFragment.DownloadImageTask(binding.imgProfile, binding.imgAva).execute(photoUrl);
        }
    }


    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;
        private final ImageView imgAva;

        public DownloadImageTask(ImageView imageView, ImageView imgAva) {
            this.imageView = imageView;
            this.imgAva = imgAva;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", Objects.requireNonNull(e.getMessage()));
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
                imgAva.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }


    private Spannable customizeText(String s)  // Hàm set mau va font chu cho Text
    {
        Typeface interBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "font/inter_bold.ttf");
        Spannable text1 = new SpannableString(s);
        text1.setSpan(new TypefaceSpan(interBoldTypeface), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setSpan(new ForegroundColorSpan(Color.RED), 0, text1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return text1;
    }


    private void openAddHomeDialog(int gravity) {

        hideOtherComponents("init");
        binding.edtSearchHome.setText("");


        binding.edtSearchHome.clearFocus();

        setupDialog(R.layout.layout_dialog_add_home, Gravity.CENTER);

        //Anh xa view cho dialog
        TextView nameHome = dialog.findViewById(R.id.txt_name_home);
        TextView addressHome = dialog.findViewById(R.id.txt_address_home);
        TextView title = dialog.findViewById(R.id.txt_title_dialog);
        EditText edtNameHome = dialog.findViewById(R.id.edt_name_home);
        EditText edtAddress = dialog.findViewById(R.id.edt_address_home);
        Button btnAddHome = dialog.findViewById(R.id.btn_add_home);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextInputLayout layoutNameHome = dialog.findViewById(R.id.layout_name_home);
        TextInputLayout layoutAddressHome = dialog.findViewById(R.id.layout_address_home);


        // Set dấu * đỏ cho TextView
        nameHome.append(customizeText(" *"));
        addressHome.append(customizeText(" *"));

        //Set thông tin cho dialog
        title.setText("Tạo mới nhà trọ");
        edtNameHome.setHint("Ví dụ: Nhà trọ MyHome");
        edtAddress.setHint("Ví dụ: 254 Nguyễn Văn Linh");
        btnAddHome.setText("Tạo");


        edtNameHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edtNameHome.getText().toString().trim();
                if (!name.isEmpty()) {
                    layoutNameHome.setErrorEnabled(false);
                    layoutNameHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = edtAddress.getText().toString().trim();
                if (!address.isEmpty()) {
                    layoutAddressHome.setErrorEnabled(false);
                    layoutAddressHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        // Xử lý/ hiệu chỉnh màu nút button add home
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateButtonState(edtNameHome, edtAddress, btnAddHome);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edtNameHome.addTextChangedListener(textWatcher);
        edtAddress.addTextChangedListener(textWatcher);


        // Xử lý sự kiện cho button
        btnAddHome.setOnClickListener(v -> {
            String name = edtNameHome.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            Home home = new Home(name, address);
            homePresenter.addHome(home);

        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }


    // Cập nhật màu cho button
    private void updateButtonState(EditText edtNameHome, EditText edtAddress, Button btn) {
        String name = edtNameHome.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        if (name.isEmpty() || address.isEmpty()) {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_clicked));
        } else {
            btn.setBackground(getResources().getDrawable(R.drawable.custom_button_add));
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getInfoUserFromGoogleAccount() {
        // Lấy thông tin người dùng từ tài khoản Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        String currentUserId = "";
        if (account != null) {
            currentUserId = account.getId();
        } else {
            currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
        }
        return currentUserId;
    }

    @Override
    public void putHomeInfoInPreferences(String nameHome, String address, DocumentReference documentReference) {
        preferenceManager.putString(Constants.KEY_HOME_ID, documentReference.getId());
        preferenceManager.putString(Constants.KEY_NAME_HOME, nameHome);
        preferenceManager.putString(Constants.KEY_ADDRESS_HOME, address);
    }

    @Override
    public void dialogClose() {
        dialog.dismiss();
    }



    @Override
    public void hideLoading() {
        binding.homesRecyclerView.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.INVISIBLE);

        // Đặt cờ trạng thái tải về true
        isLoadingFinished = true;
    }

    @Override
    public void showLoading() {
        binding.homesRecyclerView.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    public void addHome(List<Home> homes, String action) {
        HomeAdapter homesAdapter = new HomeAdapter(homes, this, this);

        switch (action) {
            case "init":
            case "search":
                homes.sort(Comparator.comparing(obj -> obj.dateObject));
                updateRecyclerView(homesAdapter, 0);
                break;
            case "sort":
                updateRecyclerView(homesAdapter, 0);
                break;
            case "add":
                homesAdapter.addHome(homes);
                updateRecyclerView(homesAdapter, homesAdapter.getLastActionPosition());
                break;
            case "update":
                int updatePosition = homePresenter.getPosition();
                homesAdapter.updateHome(updatePosition);
                updateRecyclerView(homesAdapter, homesAdapter.getLastActionPosition());
                break;
            case "delete":
                int deletePosition = homePresenter.getPosition();
                if (deletePosition == 1) {
                    updateRecyclerView(homesAdapter, 0);
                } else {
                    homesAdapter.removeHome(deletePosition);
                    updateRecyclerView(homesAdapter, homesAdapter.getLastActionPosition());
                }
                break;
        }

        showHomesRecyclerView();
    }

    private void updateRecyclerView(HomeAdapter homesAdapter, int position) {
        binding.homesRecyclerView.setAdapter(homesAdapter);
        //binding.layoutNoData.setVisibility(View.GONE);
        homesAdapter.notifyDataSetChanged();
        binding.homesRecyclerView.smoothScrollToPosition(position);
    }

    private void showHomesRecyclerView() {
        binding.layoutNoData.setVisibility(View.GONE);
        binding.txtNotification.setVisibility(View.GONE);
        binding.imgAddHome.setVisibility(View.GONE);
        binding.homesRecyclerView.setVisibility(View.VISIBLE);
        binding.frmMenuTools.setVisibility(View.VISIBLE);
    }


    @Override
    public void addHomeFailed() {
        binding.txtNotification.setVisibility(View.VISIBLE);
        binding.imgAddHome.setVisibility(View.VISIBLE);
        binding.homesRecyclerView.setVisibility(View.INVISIBLE);
        binding.frmMenuTools.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean isAdded2() {
        return isAdded();
    }

    @Override
    public void onHomeClicked(Home home) {
        binding.edtSearchHome.clearFocus();
        Intent intent = new Intent(requireContext(), MainRoomActivity.class);
        intent.putExtra("home", home);
        startActivity(intent);
    }

    @Override
    public void openPopup(View view, Home home, ItemContainerHomesBinding binding2) {
        binding.layoutSearchHome.clearFocus();
        openMenuForEachHome(view, home, binding2);
    }

    private void openMenuForEachHome(View view, Home home, ItemContainerHomesBinding binding2) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                // Thực hiện hành động cho mục chỉnh sửa
                openUpdateHomeDialog(Gravity.CENTER, home);
                return true;
            } else if (itemId == R.id.menu_delete) {

                if (home.numberOfRooms == home.numberOfRoomsAvailable) {
                    // Thực hiện hành động cho mục xóa
                    openDeleteHomeDialog(home);
                } else {
                    openCannotDeleteHomeDialog(home);

                }


                return true;
            }
            return false;
        });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                binding2.frmImage2.setVisibility(View.INVISIBLE);
                binding2.frmImage.setVisibility(View.VISIBLE);
            }
        });

        popupMenu.inflate(R.menu.menu_edit_delete);
        popupMenu.show();
    }

    private void openCannotDeleteHomeDialog(Home home) {
        setupDialog(R.layout.layout_dialog_cannot_delete_bill, Gravity.CENTER);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView txtTitle = dialog.findViewById(R.id.txt_title_of_cannot_delete);
        TextView txtBody = dialog.findViewById(R.id.txt_body_of_cannot_delete);
        txtTitle.setText("Không thể xoá nhà trọ " + home.getNameHome());
        txtBody.setText("Nhà trọ này hiện đang có người ở, bạn không thể xóa ngay lập tức !");
    }

    private void openDeleteHomeDialog(Home home) {

        setupDialog(R.layout.layout_dialog_delete_home, Gravity.CENTER);

        // Ánh xạ ID
        TextView txt_confirm_delete = dialog.findViewById(R.id.txt_confirm_delete);
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_delete_home = dialog.findViewById(R.id.btn_delete_home);

        // Hiệu chỉnh TextView
        String text = " " + home.getNameHome() + " ?";
        txt_confirm_delete.append(text);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_delete_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.deleteHome(home);
            }
        });
    }

    @Override
    public void openDialogSuccess(int id) {

        setupDialog(id, Gravity.CENTER);

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //hideOtherComponents("update");

                if (binding.edtSearchHome.isFocused()) {
                    binding.edtSearchHome.clearFocus();
                    binding.edtSearchHome.setText("");
                }
                // Clear the status of check boxes
                else if (binding.layoutTypeOfSortHome.getVisibility() == View.VISIBLE) {
                    // Xoa sort khi click vao search
                    preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);
                    binding.layoutTypeOfSortHome.setVisibility(View.GONE);
                } else if (binding.layoutTypeOfFilterHome.getVisibility() == View.VISIBLE) {
                    // Clear the status of check boxes
                    removeStatusOfCheckBoxFilterHome();
                    binding.layoutTypeOfFilterHome.setVisibility(View.GONE);
                }
            }
        });
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
    public void openConfirmUpdateHome(int gravity, String newNameHome, String newAddressHome, Home home) {
        setupDialog(R.layout.layout_dialog_confirm_update_home, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_confirm_update_home = dialog.findViewById(R.id.btn_confirm_update_home);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(v -> dialog.dismiss());

        btn_confirm_update_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homePresenter.updateSuccess(newNameHome, newAddressHome, home);
            }
        });


    }

    @Override
    public void showErrorMessage(String message, int id) {
        TextInputLayout layout_name_home = dialog.findViewById(id);
        layout_name_home.setError(message);

    }

    private void openUpdateHomeDialog(int gravity, Home home) {

        setupDialog(R.layout.layout_dialog_update_home, Gravity.CENTER);

        // Ánh xạ ID
        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_update_home = dialog.findViewById(R.id.btn_add_home);
        EditText edt_new_name_home = dialog.findViewById(R.id.edt_name_home);
        EditText edt_new_address_home = dialog.findViewById(R.id.edt_address_home);
        TextView title = dialog.findViewById(R.id.txt_title_dialog);
        TextView txt_name_home = dialog.findViewById(R.id.txt_name_home);
        TextView txt_address_home = dialog.findViewById(R.id.txt_address_home);
        TextInputLayout layoutNameHome = dialog.findViewById(R.id.layout_name_home);
        TextInputLayout layoutAddressHome = dialog.findViewById(R.id.layout_address_home);


        //Hiện thông tin lên edt
        edt_new_name_home.setText(home.getNameHome());
        edt_new_address_home.setText(home.getAddressHome());
        title.setText("Chỉnh sửa thông tin nhà trọ");
        btn_update_home.setText("Cập nhật");
        txt_name_home.append(customizeText(" *"));
        txt_address_home.append(customizeText(" *"));
        btn_update_home.setBackground(getResources().getDrawable(R.drawable.custom_button_add));

        edt_new_name_home.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = edt_new_name_home.getText().toString().trim();
                if (!name.isEmpty()) {
                    layoutNameHome.setErrorEnabled(false);
                    layoutNameHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edt_new_address_home.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = edt_new_address_home.getText().toString().trim();
                if (!address.isEmpty()) {
                    layoutAddressHome.setErrorEnabled(false);
                    layoutAddressHome.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));
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
                updateButtonState(edt_new_name_home, edt_new_address_home, btn_update_home);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        // Thêm TextWatcher cho cả hai EditText
        edt_new_name_home.addTextChangedListener(textWatcher);
        edt_new_address_home.addTextChangedListener(textWatcher);

        // Xử lý sự kiện cho Button
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        btn_update_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy dữ liệu
                String newNameHome = edt_new_name_home.getText().toString().trim();
                String newAddressHome = edt_new_address_home.getText().toString().trim();
                homePresenter.updateHome(newNameHome, newAddressHome, home);
            }
        });
    }

    private void setupDialog(int layoutId, int gravity) {
        dialog.setContentView(layoutId);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = gravity;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(Gravity.CENTER == gravity);
            dialog.show();
        }
    }

    public void openDeleteListHomeDialog(List<Home> listHomes) {
        dialog.setContentView(R.layout.layout_dialog_delete_home);
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

        Button btn_cancel = dialog.findViewById(R.id.btn_cancel);
        Button btn_delete_home = dialog.findViewById(R.id.btn_delete_home);


        btn_cancel.setOnClickListener(v -> dialog.dismiss());

        btn_delete_home.setOnClickListener(v -> homePresenter.deleteListHomes(listHomes));
    }




    @Override
    public void onPause() { // When move to the different fragment / activity
        super.onPause();
        // Clear the selected RadioButton ID from SharedPreferences
        preferenceManager.removePreference(Constants.KEY_SELECTED_RADIO_BUTTON);

        // Clear the status of check boxes
        removeStatusOfCheckBoxFilterHome();

    }

    public void removeStatusOfCheckBoxFilterHome() {
        preferenceManager.removePreference("cbxByRoom1");
        preferenceManager.removePreference("cbxByRoom2");
        preferenceManager.removePreference("cbxByRoom3");
        preferenceManager.removePreference(Constants.KEY_CBX_REVENUE_OF_MONTH_1);
        preferenceManager.removePreference(Constants.KEY_CBX_REVENUE_OF_MONTH_2);
        preferenceManager.removePreference(Constants.KEY_CBX_REVENUE_OF_MONTH_3);
    }


}
