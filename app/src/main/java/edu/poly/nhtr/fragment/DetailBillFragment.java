package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.poly.nhtr.Adapter.PlusOrMinusMoneyInDetailBillAdapter;
import edu.poly.nhtr.Adapter.ServiceInDetailBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentDetailBillBinding;
import edu.poly.nhtr.listeners.DetailBillListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.PlusOrMinusMoney;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.presenters.DetailBillPresenter;
import edu.poly.nhtr.utilities.Constants;

public class DetailBillFragment extends Fragment implements DetailBillListener {

    private FragmentDetailBillBinding binding;
    private Home home;
    private String roomID;
    private RoomBill bill;
    private DetailBillPresenter detailBillPresenter;
    private Dialog dialog;
    List<PlusOrMinusMoney> plusMoneyList = new ArrayList<>();
    List<PlusOrMinusMoney> minusMoneyList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailBillBinding.inflate(inflater, container, false);
        detailBillPresenter = new DetailBillPresenter(this);
        dialog = new Dialog(requireContext());

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            bill = (RoomBill) arguments.getSerializable("bill");
            if (bill != null) {
                roomID = bill.getRoomID();
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        detailBillPresenter.getRoomPrice(bill.getRoomID(), new DetailBillPresenter.OnGetRoomPriceCompleteListener() {
            @Override
            public void onComplete(Long price) {
                setText(binding.txtPriceOfRoom, formatMoney(price));
            }
        });

        setInfoOfBill(bill);

        if (bill.isPayedBill) {
            binding.txtStatusOfBill.setVisibility(View.VISIBLE);
        } else if (bill.isDelayPayBill) {
            binding.txtStatusOfBill.setText("Hoá đơn đã quá hạn thanh toán");
            binding.txtStatusOfBill.setTextColor(getResources().getColor(R.color.colorRed));
            binding.txtStatusOfBill.setVisibility(View.VISIBLE);
        } else {
            binding.txtStatusOfBill.setVisibility(View.GONE);
        }


        setListeners();

        return binding.getRoot();
    }

    private void setInfoOfBill(RoomBill bill) {

        // Định dạng ngày tháng
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Thiết lập thông tin hóa đơn
        setText(binding.txtMonthYearOfBill, bill.getMonth() + "/" + bill.getYear());
        setText(binding.txtDateMakeBill, formatDate(bill.getDateCreateBill(), dateFormat));
        setText(binding.txtDateDuePay, formatDate(bill.getDatePayBill(), dateFormat));
//        setText(binding.numberOfDaysLived, String.valueOf(bill.getNumberOfDaysLived()));
        setText(binding.numberOfDaysLived, bill.getTimeLived());

        setText(binding.txtTotalMoneyOfRoom, formatMoney(bill.getMoneyOfRoom()));
        setText(binding.txtTotalMoneyOfService, formatMoney(bill.getMoneyOfService()));
        setText(binding.txtTotalMoneyOfAddOrMinus, formatMoney(bill.getMoneyOfAddOrMinus()));
        setText(binding.txtTotalMoneyOfBill, formatMoney(bill.getTotalOfMoney()));

        //Set data for service money
        detailBillPresenter.getRoomServiceAndQuantity(bill, new DetailBillPresenter.OnGetRoomServiceAndQuantityListener() {
            @Override
            public void onGetRoomServiceAndQuantity(List<RoomService> roomServices) {
                binding.roomServiceRecyclerView.setVisibility(View.GONE);
                ServiceInDetailBillAdapter adapter = new ServiceInDetailBillAdapter(roomServices);
                binding.roomServiceRecyclerView.setAdapter(adapter);
                binding.roomServiceRecyclerView.setVisibility(View.VISIBLE);

//                ViewGroup.LayoutParams params = binding.roomServiceRecyclerView.getLayoutParams();
//                params.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Đảm bảo chiều cao tự điều chỉnh
//                binding.roomServiceRecyclerView.setLayoutParams(params);

                updateRecyclerViewHeight(binding.roomServiceRecyclerView);

            }
        });

        //Set data plus or minus money
        if (bill.getPlusOrMinusMoneyList() == null) {
            binding.layoutMoneyPlusOrMinus.setVisibility(View.GONE);
            binding.view1.setVisibility(View.GONE);
        } else {
            binding.layoutMoneyPlusOrMinus.setVisibility(View.VISIBLE);
            binding.view1.setVisibility(View.VISIBLE);
            for (PlusOrMinusMoney item : bill.getPlusOrMinusMoneyList()) {
                if (item.getPlus()) plusMoneyList.add(item);
                else minusMoneyList.add(item);
            }
            if (!plusMoneyList.isEmpty()) {
                PlusOrMinusMoneyInDetailBillAdapter plusAdapter = new PlusOrMinusMoneyInDetailBillAdapter(plusMoneyList);
                binding.plusRecyclerView.setAdapter(plusAdapter);
                binding.plusRecyclerView.setVisibility(View.VISIBLE);

                String totalPlusMoney = "Tổng tiền cộng: " + formatMoney(bill.getTotalMoneyPlus());
                binding.txtTotalPlusMoney.setText(totalPlusMoney);
                binding.txtTotalPlusMoney.setVisibility(View.VISIBLE);
            }
            if (!minusMoneyList.isEmpty()) {
                PlusOrMinusMoneyInDetailBillAdapter minusAdapter = new PlusOrMinusMoneyInDetailBillAdapter(minusMoneyList);
                binding.minusRecyclerView.setAdapter(minusAdapter);
                binding.minusRecyclerView.setVisibility(View.VISIBLE);

                String totalMinusMoney = "Tổng tiền trừ: " + formatMoney(bill.getTotalMoneyMinus());
                binding.txtTotalMinusMoney.setText(totalMinusMoney);
                binding.txtTotalMinusMoney.setVisibility(View.VISIBLE);
            }
//            PlusOrMinusMoneyInDetailBillAdapter adapter = new PlusOrMinusMoneyInDetailBillAdapter(bill.getPlusOrMinusMoneyList());
//            binding.plusOrMinusRecyclerView.setAdapter(adapter);
        }
    }

    private void updateRecyclerViewHeight(final RecyclerView recyclerView) {
        final RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = recyclerView.getAdapter();
        if (adapter == null) return;

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int totalHeight = 0;
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    // Lấy ViewHolder từ adapter
                    RecyclerView.ViewHolder viewHolder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
                    adapter.bindViewHolder(viewHolder, i);

                    // Đảm bảo các phần tử được làm mới và đo chiều cao chính xác
                    View itemView = viewHolder.itemView;
                    itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    totalHeight += itemView.getMeasuredHeight();
                }

                // Cập nhật chiều cao của RecyclerView
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                params.height = totalHeight;
                recyclerView.setLayoutParams(params);
            }
        });
    }

    private void setText(TextView textView, String text) {
        textView.setText(text);
    }

    private String formatDate(Date date, SimpleDateFormat dateFormat) {
        return dateFormat.format(date);
    }

    private String formatMoney(double amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(amount) + " VNĐ";
    }


    private void setListeners() {

//        binding.btnSendBill.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                String phoneNumber = "0123456789";
//                String message = "Hello from my app!";
//
//                SmsManager smsManager = SmsManager.getDefault();
//                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
//
//                Toast.makeText(getActivity(),
//                        "Tin nhắn đã được gửi!", Toast.LENGTH_SHORT).show();
//            }
//        });

        binding.btnSendBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openConfirmGiveBillDialog();

            }
        });


        binding.imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại Fragment trước đó trong back stack
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnMarkBillIsPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openConfirmPayedBill();
            }
        });
    }

    private void openConfirmPayedBill() {
        setupDialog(R.layout.layout_dialog_confirm_payed_bill);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirmPayedBill = dialog.findViewById(R.id.btn_confirm_payed_bill);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirmPayedBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showButtonLoading(R.id.btn_confirm_payed_bill);
                detailBillPresenter.updateStatusOfBillWhenPayedBill(bill, new DetailBillPresenter.OnUpdateStatusOfBill() {
                    @Override
                    public void onComplete() {
                        hideButtonLoading(R.id.btn_confirm_payed_bill);
                        dialog.dismiss();
                        getParentFragmentManager().popBackStack();
                    }
                });
            }
        });
    }

    private void openConfirmGiveBillDialog() {
        setupDialog(R.layout.layout_dialog_confirm_give_bill);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirmGiveBill = dialog.findViewById(R.id.btn_confirm_give_bill);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirmGiveBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showButtonLoading(R.id.btn_confirm_give_bill);
                detailBillPresenter.updateStatusOfBillWhenGiveBill(bill, new DetailBillPresenter.OnUpdateStatusOfBill() {
                    @Override
                    public void onComplete() {
                        giveBill();
                    }
                });


            }
        });
    }

    private void giveBill() {
        detailBillPresenter.getPhoneNumber(roomID, new DetailBillPresenter.OnGetPhoneNumber() {
            @Override
            public void onComplete(String phoneNumber) {
                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy số điện thoại chính cho phòng này.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String phone = "0" + phoneNumber;
                String monthYear = binding.txtMonthYearOfBill.getText().toString();
                String roomPrice = binding.txtPriceOfRoom.getText().toString();
                String numberOfDays = binding.numberOfDaysLived.getText().toString();
                String status = binding.txtStatusOfBill.getText().toString();
                String totalServiceCost = binding.txtTotalMoneyOfService.getText().toString();
                String totalBill = binding.txtTotalMoneyOfBill.getText().toString();

                // Initialize messages for plus and minus money
                StringBuilder plusMoneyMessage = new StringBuilder();
                StringBuilder minusMoneyMessage = new StringBuilder();

                // Iterate through the plus money list and build the message
                for (PlusOrMinusMoney plusMoney : plusMoneyList) {
                    plusMoneyMessage.append("+ ")
                            .append(plusMoney.getMoney())
                            .append(" đ: ")
                            .append(plusMoney.getReason())
                            .append("\n");
                }

                // Iterate through the minus money list and build the message
                for (PlusOrMinusMoney minusMoney : minusMoneyList) {
                    minusMoneyMessage.append("- ")
                            .append(minusMoney.getMoney())
                            .append(" đ: ")
                            .append(minusMoney.getReason())
                            .append("\n");
                }

                String message;
                if (bill.isDelayPayBill) {
                    message = "CẢNH BÁO: Bạn đã trễ thanh toán hoá đơn cho tháng " + monthYear + ". Vui lòng thanh toán hoá đơn!"
                            + "\nThông tin hóa đơn tháng " + monthYear
                            + "\nTiền phòng: " + roomPrice
                            + "\nSố ngày ở: " + numberOfDays
                            + "\nTổng tiền dịch vụ: " + totalServiceCost
                            + "\nCác khoản cộng thêm:\n" + plusMoneyMessage
                            + "\nCác khoản trừ:\n" + minusMoneyMessage
                            + "\nTổng tiền: " + totalBill;
                } else {
                    message = "Thông tin hóa đơn tháng " + monthYear
                            + "\nTiền phòng: " + roomPrice
                            + "\nSố ngày ở: " + numberOfDays
                            + "\nTổng tiền dịch vụ: " + totalServiceCost
                            + "\nCác khoản cộng thêm:\n" + plusMoneyMessage
                            + "\nCác khoản trừ:\n" + minusMoneyMessage
                            + "\nTổng tiền: " + totalBill;
                }

                hideButtonLoading(R.id.btn_confirm_give_bill);
                dialog.dismiss();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone));
                intent.putExtra("sms_body", message);
                startActivity(intent);
            }
        });
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

    public void showButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.INVISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
    }

    public void hideButtonLoading(int id) {
        dialog.findViewById(id).setVisibility(View.VISIBLE);
        dialog.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
    }


    public void showToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}