package edu.poly.nhtr.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.Adapter.PlusOrMinusMoneyInDetailBillAdapter;
import edu.poly.nhtr.Adapter.ServiceInDetailBillAdapter;
import edu.poly.nhtr.databinding.FragmentDetailBillBinding;
import edu.poly.nhtr.listeners.DetailBillListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.presenters.DetailBillPresenter;

public class DetailBillFragment extends Fragment implements DetailBillListener{

    private FragmentDetailBillBinding binding;
    private Home home;
    private String roomID;
    private RoomBill bill;
    private DetailBillPresenter detailBillPresenter;


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
        if (bill.getPlusOrMinusMoneyList() == null){
            binding.layoutMoneyPlusOrMinus.setVisibility(View.GONE);
            binding.view1.setVisibility(View.GONE);
        }
        else {
            binding.layoutMoneyPlusOrMinus.setVisibility(View.VISIBLE);
            binding.view1.setVisibility(View.VISIBLE);
            PlusOrMinusMoneyInDetailBillAdapter adapter = new PlusOrMinusMoneyInDetailBillAdapter(bill.getPlusOrMinusMoneyList());
            binding.plusOrMinusRecyclerView.setAdapter(adapter);
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
        binding.btnSendBill.setOnClickListener(new View.OnClickListener() {
            String edt ="hello, how are u";
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("smsto:0919300585"));
                i.putExtra("smsbody",edt);
                startActivity(i);
            }
        });
        binding.btnCancelViewBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại Fragment trước đó trong back stack
                getParentFragmentManager().popBackStack();
            }
        });
    }

    public void showToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}