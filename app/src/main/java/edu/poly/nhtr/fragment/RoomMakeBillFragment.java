package edu.poly.nhtr.fragment;

import android.icu.text.Collator;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomMakeBillBinding;
import edu.poly.nhtr.listeners.RoomMakeBillListener;
import edu.poly.nhtr.models.Guest;
import edu.poly.nhtr.models.MainGuest;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.models.RoomService;
import edu.poly.nhtr.presenters.RoomMakeBillPresenter;
import edu.poly.nhtr.utilities.Constants;


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

        room = presenter.getRoomFromFirebase(roomId);
        presenter.getMainGuest(roomId, new RoomMakeBillPresenter.OnGetContractFromFirebaseListener() {

            @Override
            public void onGetContractFromFirebase(MainGuest mainGuest1) {
                mainGuest = mainGuest1;
                setDateTime();
            }
        });
        presenter.getListRoomService(roomId, new RoomMakeBillPresenter.OnGetRoomServiceFromFirebaseListener() {
            @Override
            public void onGetRoomServiceFromFirebase(List<RoomService> roomServices) {
                setData();
            }
        });

    }




    private void setData() {
        //Set price of room
        String priceOfRoom = mainGuest.getRoomPrice()+"";
        binding.txtRoomPrice.setText(priceOfRoom);

        //Set into money of room
        double intoMoneyOfRoom = mainGuest.getRoomPrice()*(Integer.parseInt(binding.txtMonthHire.getText().toString()) + (double) Integer.parseInt(binding.txtDayHire.getText().toString()) /30);
        String intoMoneyRoom = intoMoneyOfRoom+"";
        binding.txtIntoRoomMoney.setText(intoMoneyRoom);


    }

    private void setDateTime() {
        // Lấy ngày hiện tại
        Date date = new Date();

        // Định dạng ngày
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String createBillDate = formatter.format(date);
        String monthYear = "Tháng " + date.getMonth() + ", " + date.getYear();
        String datePay = mainGuest.getPayDate();

        binding.txtMonthYear.setText(monthYear);
        binding.txtCreateBillDate.setText(createBillDate);
        binding.txtPayDate.setText(datePay);

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