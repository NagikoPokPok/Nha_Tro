package edu.poly.nhtr.fragment;

import static android.app.ProgressDialog.show;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import edu.poly.nhtr.Adapter.NotificationAdapter;
import edu.poly.nhtr.Adapter.RoomBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentNotificationBinding;
import edu.poly.nhtr.databinding.FragmentRoomBillBinding;
import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;
import edu.poly.nhtr.presenters.RoomBillPresenter;

public class RoomBillFragment extends Fragment implements RoomBillListener {

    FragmentRoomBillBinding binding;
    RoomBillPresenter roomBillPresenter;
    RoomBillAdapter roomBillAdapter;
    Dialog dialog;
    String roomID;
    List<RoomBill> billList = new ArrayList<RoomBill>();
    Room room;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentRoomBillBinding.inflate(getLayoutInflater());
        dialog = new Dialog(requireActivity());


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentRoomBillBinding.inflate(inflater, container, false);
        roomBillPresenter = new RoomBillPresenter(this);
        roomBillAdapter = new RoomBillAdapter(requireContext(), billList, roomBillPresenter, this);
        View view = binding.getRoot();

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            if (room != null) {
                roomID = room.getRoomId();
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        LocalDate localDate = LocalDate.now();
        int dayOfMonth = localDate.getDayOfMonth();

        setupRecyclerView();

        if (dayOfMonth == 1) {
            roomBillPresenter.addBill(room);
        }else {

            roomBillPresenter.getBill(room, new RoomBillPresenter.OnGetBillCompleteListener() {
                @Override
                public void onComplete(List<RoomBill> billList) {
                    roomBillAdapter.setBillList(billList);
                }
            });
        }




        return view;
    }

    private void setupRecyclerView() {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        roomBillAdapter = new RoomBillAdapter(requireActivity(), new ArrayList<>(), roomBillPresenter, this);
        binding.recyclerView.setAdapter(roomBillAdapter);
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
}