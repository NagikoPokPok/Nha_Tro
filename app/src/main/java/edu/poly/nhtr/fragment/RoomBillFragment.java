package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import edu.poly.nhtr.Activity.MonthPickerDialog;
import edu.poly.nhtr.Adapter.RoomBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomBillBinding;
import edu.poly.nhtr.databinding.ItemContainerInformationOfBillBinding;
import edu.poly.nhtr.databinding.LayoutDialogConfirmDeleteBillBinding;
import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.presenters.RoomBillPresenter;

public class RoomBillFragment extends Fragment implements RoomBillListener {

    private FragmentRoomBillBinding binding;
    private RoomBillPresenter roomBillPresenter;
    private RoomBillAdapter roomBillAdapter;
    private List<RoomBill> billList = new ArrayList<>();
    private Room room;
    private OnMakeBillClickListener onMakeBillClickListener;
    private int currentMonth;
    private int currentYear;
    private boolean visible = true;
    private String date = "";
    private boolean isSelectAllChecked = false;
    private Dialog dialog;

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
        dialog = new Dialog(requireContext());
        View view = binding.getRoot();

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            if (room != null) {
                roomBillPresenter.getBill(room, billList -> roomBillAdapter.setBillList(billList));
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentYear = Calendar.getInstance().get(Calendar.YEAR);

        setupRecyclerView();
        checkAndAddBillIfNeeded();
        setupMonthPicker();
        setupDeleteManyBills();


        return view;
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
        binding.txtDateTime.setText(String.valueOf(currentYear));


        binding.imgCalendar.setOnClickListener(v -> {
            visible = true;
            showMonthPicker(); // Open dialog
        });

    }

    private void showMonthPicker() {
        MonthPickerDialog monthPickerDialog = new MonthPickerDialog(requireContext(), currentMonth, currentYear,
                new MonthPickerDialog.OnMonthSelectedListener() {
                    @Override
                    public void onMonthSelected(int month, int year) {
                        date = month + "/" + year; // month = selectedMonthPosition + 1 ==> month == actual value
                        binding.txtDateTime.setText(date);
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

        Objects.requireNonNull(monthPickerDialog.getWindow()).setBackgroundDrawableResource(R.drawable.background_dialog_index);
        monthPickerDialog.show();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recyclerView.setAdapter(roomBillAdapter);
    }

    private void checkAndAddBillIfNeeded() {
        LocalDate localDate = LocalDate.now();
        int dayOfMonth = localDate.getDayOfMonth();
        if (dayOfMonth == 1) {
            roomBillPresenter.addBill(room);
        } else {
            roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
                @Override
                public void onComplete(List<RoomBill> billList) {
                    roomBillAdapter.setBillList(billList);
                }
            });
        }
    }


    @Override
    public void setBillList(List<RoomBill> billList) {
        this.billList = billList;
        if (roomBillAdapter != null) {
            roomBillAdapter.setBillList(this.billList);
        }
    }


    @Override
    public void makeBillClick(RoomBill bill) {
        if (onMakeBillClickListener != null) {
            showToast("Click");
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
                showToast("view bill");
                return true;
            } else if (itemId == R.id.menu_edit_bill) {
                showToast("edit bill");
                return true;
            } else if (itemId == R.id.menu_delete_bill) {
                if (bill.isNotPayBill() || bill.isDelayPayBill()) {
                    showToast("Không thể xoá hoá đơn này.");
                    showDialog(R.layout.layout_dialog_cannot_delete_bill);
                    return true;
                }

                if (!bill.isPayedBill() && !bill.isDelayPayBill() && !bill.isNotPayBill()) {
                    showToast("Không thể xoá hoá đơn này.");
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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

    public void setOnMakeBillClickListener(OnMakeBillClickListener listener) {
        this.onMakeBillClickListener = listener;
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


}
