package edu.poly.nhtr.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Activity.MainDetailedRoomActivity;
import edu.poly.nhtr.Adapter.RoomBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentRoomBillBinding;
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

        setupRecyclerView();
        checkAndAddBillIfNeeded();

        return view;
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
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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

    public interface OnMakeBillClickListener {
        void onMakeBillClicked(RoomBill bill);
    }

    public void setOnMakeBillClickListener(OnMakeBillClickListener listener) {
        this.onMakeBillClickListener = listener;
    }


}
