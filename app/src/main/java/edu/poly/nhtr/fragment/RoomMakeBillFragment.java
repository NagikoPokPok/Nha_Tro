package edu.poly.nhtr.fragment;

import android.icu.text.Collator;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.Adapter.ServiceInMakeBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomMakeBillBinding;
import edu.poly.nhtr.listeners.RoomMakeBillListener;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.presenters.RoomMakeBillPresenter;


public class RoomMakeBillFragment extends Fragment implements RoomMakeBillListener {

    private String roomId;
    private FragmentRoomMakeBillBinding binding;
    private Room room;
    private MainGuest mainGuest;
    private List<RoomService> roomServiceList;
    private RoomBill bill;
    private RoomMakeBillPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentRoomMakeBillBinding.inflate(getLayoutInflater());
        presenter = new RoomMakeBillPresenter(this);

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            bill = (RoomBill) arguments.getSerializable("bill");
            if (bill != null) {
                roomId = bill.getRoomID();
                showToast(roomId);
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
        room = presenter.getRoomFromFirebase(roomId);
        presenter.getMainGuest(roomId, new RoomMakeBillPresenter.OnGetContractFromFirebaseListener() {

            @Override
            public void onGetContractFromFirebase(MainGuest mainGuest1) {
                mainGuest = mainGuest1;
                setDateTimeAndRoomPrice();
            }
        });
        presenter.getListRoomService(roomId, new RoomMakeBillPresenter.OnGetRoomServiceFromFirebaseListener() {
            @Override
            public void onGetRoomServiceFromFirebase(List<RoomService> roomServices) {
                if (roomServiceList == null) roomServiceList = new ArrayList<>();
                roomServiceList.clear();
                roomServiceList.addAll(roomServices);
                roomServices.sort(Comparator.comparing(RoomService :: getServiceName, Collator.getInstance(new Locale("vi", "VN"))));
                setOtherData();
            }
        });
    }

    private void setListener() {
        binding.btnCancelMakeBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại Fragment trước đó trong back stack
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnMakeBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private void setOtherData() {
        // set adapter
        ServiceInMakeBillAdapter adapter = new ServiceInMakeBillAdapter(roomServiceList);
        binding.serviceInBillRecyclerView.setAdapter(adapter);

        // Set into money of service
        int totalOfService = 0;
        for (RoomService roomService : roomServiceList){
            totalOfService += (roomService.getService().getPrice() * roomService.getQuantity());
        }
        binding.txtTotalServiceFee.setText(toStringFromInt(totalOfService));

        // Set total money of bill
        int totalMoney = 0;
        totalMoney = totalOfService + Integer.parseInt(binding.txtIntoRoomMoney.getText().toString());

        binding.txtTotalMoney.setText(toStringFromInt(totalMoney));
    }

    private void setDateTimeAndRoomPrice() {
        // Lấy ngày hiện tại
        LocalDate date = LocalDate.now();

        // Định dạng ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Ngày tạo-xuất hóa đơn
        String createBillDate = date.format(formatter);
        String monthYear = "Tháng " + date.getMonthValue() + ", " + date.getYear();

        binding.txtMonthYear.setText(monthYear);
        binding.txtCreateBillDate.setText(createBillDate);

        String txt_createContractDate = mainGuest.getCreateDate();
        String txt_expirationContractDate = mainGuest.getExpirationDate();
        String payDay = mainGuest.getPayDate();

        LocalDate createContractDate = LocalDate.parse(txt_createContractDate, formatter);
        LocalDate expirationContractDate = LocalDate.parse(txt_expirationContractDate, formatter);

        // khởi tạo xuất hóa đơn
        LocalDate payDate;
        if (Integer.parseInt(payDay) > date.getDayOfMonth())
            payDate = LocalDate.of(date.getYear(), date.getDayOfMonth(), Integer.parseInt(payDay));
        else if (date.getMonthValue() == 12)
            payDate = LocalDate.of(date.getYear()+1, 1, Integer.parseInt(payDay));
        else
            payDate = LocalDate.of(date.getYear(), date.getMonthValue()+1, Integer.parseInt(payDay));

        // Nếu tg kết thúc hợp đồng không quá 5 ngày sau ngày thanh toán sẽ gộp bill
        if (ChronoUnit.DAYS.between(payDate, expirationContractDate) <=5)
            payDate = expirationContractDate;

        // Ngày bắt đầu tính
        LocalDate startDate;
        if (ChronoUnit.DAYS.between(payDate, createContractDate) <= (createContractDate.lengthOfMonth() +5))
            startDate = createContractDate;
        else
            startDate = payDate.minusMonths(1);

        int monthHire = 0, dayHire = 0;

        if (startDate.getDayOfMonth() == payDate.getDayOfMonth())
            monthHire = 1;
        else if (startDate.getDayOfMonth() < payDate.getDayOfMonth()) {
            monthHire = payDate.getMonthValue() - startDate.getMonthValue();
            dayHire = payDate.getDayOfMonth() - startDate.getDayOfMonth();
        }else
            dayHire = (int) ChronoUnit.DAYS.between(startDate, payDate);

//        int[] startDate = new int[]{Integer.parseInt(Arrays.toString(mainGuest.getCreateDate().split("/")))};
//
//        monthHire = date.getMonthValue() - startDate[1];
//        if (monthHire > 1){
//            startDate[0] = Integer.parseInt(payDay);
//            if (startDate[0] >= date.getDayOfMonth()) startDate[1] = date.getMonthValue()-1;
//            else startDate[1] = date.getMonthValue();
//            startDate[2] = date.getYear();
//
//            if (startDate[1] == 0) {
//                startDate[1] = 12;
//                startDate[2] -= 1;
//            } else if (startDate[1] == 13) {
//                startDate[1] = 1;
//                startDate[2] +=1;
//            }
//        }
//        LocalDate dateStart = LocalDate.of(startDate[2], startDate[1], startDate[0]);
//        if (startDate[0] <= date.getDayOfMonth() ){
//            dayHire = date.getDayOfMonth() - startDate[0];
//        }else {
//            monthHire -= 1;
//            dayHire = (int) ChronoUnit.DAYS.between(dateStart, date);
//        }


//        binding.txtPayDate.setText(datePay);
        String txtDayHire = dayHire + " ngày";
        String txtMonthHire = monthHire + " tháng, ";
        binding.txtDayHire.setText(txtDayHire);
        binding.txtMonthHire.setText(txtMonthHire);



        //Set price of room
        String priceOfRoom = mainGuest.getRoomPrice()+"";
        binding.txtRoomPrice.setText(priceOfRoom);

        //Set into money of room
        int intoMoneyOfRoom = (int) (mainGuest.getRoomPrice()*( monthHire +dayHire/30.0));
//                (Integer.parseInt(binding.txtMonthHire.getText().toString().split(" ")[0]) +  Integer.parseInt(binding.txtDayHire.getText().toString().split(" ")[1]) /30));
        String intoMoneyRoom = intoMoneyOfRoom+"";
        binding.txtIntoRoomMoney.setText(intoMoneyRoom);
    }

    private String toStringFromInt(int value) {
        return value + "";
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
}