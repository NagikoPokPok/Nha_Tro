package edu.poly.nhtr.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.Collator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.Adapter.PlusOrMinusMoneyAdapter;
import edu.poly.nhtr.Adapter.ServiceInMakeBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomMakeBillBinding;
import edu.poly.nhtr.listeners.RoomMakeBillListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.PlusOrMinusMoney;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.presenters.RoomMakeBillPresenter;


public class RoomMakeBillFragment extends Fragment implements RoomMakeBillListener {

    private String roomId;
    private FragmentRoomMakeBillBinding binding;
    private MainGuest mainGuest;
    private List<RoomService> roomServiceList;
    private RoomBill bill;
    private RoomMakeBillPresenter presenter;
    private PlusOrMinusMoneyAdapter plusOrMinusMoneyAdapter;
    private Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentRoomMakeBillBinding.inflate(getLayoutInflater());
        presenter = new RoomMakeBillPresenter(this);
        dialog = new Dialog(requireContext());

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            bill = (RoomBill) arguments.getSerializable("bill");
            if (bill != null) {
                roomId = bill.getRoomID();
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        roomId = bill.getRoomID();



        setData();
        setListener();
    }

    private void setData() {
        presenter.getMainGuest(roomId, mainGuest1 -> {
            mainGuest = mainGuest1;
            setDateTimeAndRoomPrice();

            presenter.getListRoomService(roomId, roomServices -> {
                if (roomServiceList == null) roomServiceList = new ArrayList<>();
                roomServiceList.clear();
                roomServiceList.addAll(roomServices);
                roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));

                presenter.setQuantityToServiceWithIndex(roomServices, bill, this::setOtherData);
            });
        });

    }

    private void setListener() {
        binding.btnCancelMakeBill.setOnClickListener(v -> {
            // Quay lại Fragment trước đó trong back stack
            getParentFragmentManager().popBackStack();
        });

        binding.btnMakeBill.setOnClickListener(v -> openConfirmMakeBillDialog());
    }

    private void openConfirmMakeBillDialog() {
        setupDialog(R.layout.layout_dialog_confirm_make_bill);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirmMakeBill = dialog.findViewById(R.id.btn_confirm_make_bill);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirmMakeBill.setOnClickListener(v -> {
            showButtonLoading(R.id.btn_confirm_make_bill);
            bill.setPlusOrMinusMoneyList(plusOrMinusMoneyAdapter.getPlusOrMinusMoneyList());
            presenter.updateBill(bill);
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




    private void setOtherData() {
        // set adapter for service
        ServiceInMakeBillAdapter serviceInMakeBillAdapter = new ServiceInMakeBillAdapter(roomServiceList, this);
        binding.serviceInBillRecyclerView.setAdapter(serviceInMakeBillAdapter);
        if (roomServiceList.isEmpty())
            showToast("Không có dữ liệu");
        else binding.serviceInBillRecyclerView.setVisibility(View.VISIBLE);



        // Set into money of service
        int totalOfService = 0;
        for (RoomService roomService : roomServiceList){
            totalOfService += (roomService.getService().getPrice() * roomService.getQuantity());
        }
        binding.txtTotalServiceFee.setText(toMoneyForm(totalOfService));
        bill.moneyOfService = totalOfService;
        bill.moneyOfRoom = Integer.parseInt(binding.txtIntoRoomMoney.getText().toString());


        // Set total money of bill
        long totalMoney = totalOfService + bill.moneyOfRoom;
        setTotalMoney(totalMoney);

        // set adapter for plus or minus money
        List<PlusOrMinusMoney> plusOrMinusMoneyList = new ArrayList<>();
        if (!bill.getPlusOrMinusMoneyList().isEmpty()) plusOrMinusMoneyList = bill.getPlusOrMinusMoneyList();
        plusOrMinusMoneyAdapter = new PlusOrMinusMoneyAdapter(plusOrMinusMoneyList, this, () -> {
            // Set total money plus or minus
            setTotalMoney(totalMoney);
        });
        binding.plusOrMinusRecyclerView.setAdapter(plusOrMinusMoneyAdapter);

        setVisibleOfPlusOrMinusRecycler();

        // set listener for button plus and minus
        binding.btnPlusMoney.setOnClickListener(v -> {
            plusOrMinusMoneyAdapter.addPlusOrMinusMoney(true);
            binding.btnPlusMoney.setChecked(false);
            setVisibleOfPlusOrMinusRecycler();
        });

        binding.btnMinusMoney.setOnClickListener(v -> {
            plusOrMinusMoneyAdapter.addPlusOrMinusMoney(false);
            binding.btnMinusMoney.setChecked(false);
            setVisibleOfPlusOrMinusRecycler();
        });


    }

    public void setVisibleOfPlusOrMinusRecycler() {
        if (plusOrMinusMoneyAdapter.getItemCount() == 0){
            binding.txtNullPlusOrMinus.setVisibility(View.VISIBLE);
            binding.plusOrMinusRecyclerView.setVisibility(View.GONE);
            binding.txtCountPlus.setVisibility(View.GONE);
            binding.txtCountMinus.setVisibility(View.GONE);
        }else {
            binding.txtNullPlusOrMinus.setVisibility(View.GONE);
            binding.plusOrMinusRecyclerView.setVisibility(View.VISIBLE);
            binding.txtCountPlus.setVisibility(View.VISIBLE);
            binding.txtCountMinus.setVisibility(View.VISIBLE);

            binding.txtCountMinus.setText(toMoneyForm(plusOrMinusMoneyAdapter.getMinus()));
            binding.txtCountPlus.setText(toMoneyForm(plusOrMinusMoneyAdapter.getPlus()));
            
        }
    }

    private void setTotalMoney(long totalMoney) {
        long total = totalMoney;
        if (plusOrMinusMoneyAdapter != null){
            total += plusOrMinusMoneyAdapter.getTotalMoney();
            bill.moneyOfAddOrMinus = plusOrMinusMoneyAdapter.getTotalMoney();
            bill.setTotalMoneyPlus(plusOrMinusMoneyAdapter.getTotalMoneyPlus());
            bill.setTotalMoneyMinus(plusOrMinusMoneyAdapter.getTotalMoneyMinus());
        }
        binding.txtTotalMoney.setText(toMoneyForm(total));
        bill.totalOfMoney = total;
    }

    private void setDateTimeAndRoomPrice() {
        // Lấy ngày hiện tại
        LocalDate date = LocalDate.now();

        // Định dạng ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Ngày tạo-xuất hóa đơn
        String createBillDate = date.format(formatter);
        String monthYear = "Tháng " + bill.month + ", " + bill.year;

        binding.txtMonthYear.setText(monthYear);
        binding.txtCreateBillDate.setText(createBillDate);

        bill.dateCreateBill = java.sql.Date.valueOf(String.valueOf(date));

        String txt_GuestDateIn = mainGuest.getGuestDateIn();
        String txt_expirationContractDate = mainGuest.getExpirationDate();
        String payDay = String.valueOf(mainGuest.getPayDate());

        LocalDate guestDateIn = LocalDate.parse(txt_GuestDateIn, formatter);
        LocalDate expirationContractDate = LocalDate.parse(txt_expirationContractDate, formatter);

        // khởi tạo xuất hóa đơn
        LocalDate payDate = LocalDate.of(bill.getYear(), bill.getMonth(), Integer.parseInt(payDay));
        if (Integer.parseInt(payDay) > guestDateIn.getDayOfMonth())
            payDate = payDate.plusMonths(1);
        else if (Integer.parseInt(payDay) <= guestDateIn.getDayOfMonth()){
//            if (ChronoUnit.DAYS.between(guestDateIn, payDate) <=5)
//                payDate = payDate.plusMonths(2);
//            else
                payDate = payDate.plusMonths(1);
        }
        if (ChronoUnit.DAYS.between(guestDateIn, payDate) <= 5) payDate.plusMonths(1);

        // Nếu tg kết thúc hợp đồng không quá 5 ngày sau ngày thanh toán sẽ gộp bill
        if (ChronoUnit.DAYS.between(payDate, expirationContractDate) <=5)
            payDate = expirationContractDate;


        // Ngày bắt đầu tính
        LocalDate startDate;
        if (ChronoUnit.DAYS.between(guestDateIn, payDate) <= (guestDateIn.lengthOfMonth() + 5)){
            startDate = guestDateIn;
        }else {
            startDate = payDate.minusMonths(1);
            startDate = startDate.withDayOfMonth(Integer.parseInt(payDay));
        }


        LocalDate dateEndToPay = payDate.plusDays(mainGuest.getDaysUntilDueDate());

        binding.txtPayDate.setText(dateEndToPay.format(formatter));

        binding.txtDateStart.setText(startDate.format(formatter));
        binding.txtDateEnd.setText(payDate.format(formatter));

        bill.numberOfDaysToPayBill = Integer.parseInt(payDay);
        bill.datePayBill = java.sql.Date.valueOf(String.valueOf(payDate));


        // Amount of day and month
        int monthHire = 0, dayHire = 0;

        if (startDate.getDayOfMonth() == payDate.getDayOfMonth())
            monthHire = 1;
        else if (startDate.getDayOfMonth() < payDate.getDayOfMonth()) {
            monthHire = payDate.getMonthValue() - startDate.getMonthValue();
            dayHire = payDate.getDayOfMonth() - startDate.getDayOfMonth();
        }else
            dayHire = (int) ChronoUnit.DAYS.between(startDate, payDate);


        String txtDayHire = dayHire + " ngày";
        String txtMonthHire = monthHire + " tháng, ";
        binding.txtDayHire.setText(txtDayHire);
        binding.txtMonthHire.setText(txtMonthHire);

        bill.setTimeLived(txtMonthHire+txtDayHire);

        //Set price of room
        String priceOfRoom = mainGuest.getRoomPrice()+"";
        binding.txtRoomPrice.setText(priceOfRoom);

        //Set into money of room
        long intoMoneyOfRoom = (long) (mainGuest.getRoomPrice()*( monthHire +dayHire/30.0));
//                (Integer.parseInt(binding.txtMonthHire.getText().toString().split(" ")[0]) +  Integer.parseInt(binding.txtDayHire.getText().toString().split(" ")[1]) /30));
        if(intoMoneyOfRoom>=0) binding.txtIntoRoomMoney.setText(toMoneyForm(intoMoneyOfRoom));
        else binding.txtIntoRoomMoney.setText("Không thể lập hóa đơn khi ngày bắt đầu tính ở sau ngày kết thúc");
    }

    private String toStringFromInt(int value) {
        return value + "";
    }
    private String toStringFromLong(long value){
        return value + "";
    }
    private String toMoneyForm(long value){
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        return numberFormat.format(value) ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_room_make_bill, container, false);

        return binding.getRoot();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this.requireActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void makeBillSuccessfully() {
        // Quay lại Fragment trước đó trong back stack
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void refreshPlusOrMinusMoney() {
        setVisibleOfPlusOrMinusRecycler();
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