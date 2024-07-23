package edu.poly.nhtr.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentDetailBillBinding;
import edu.poly.nhtr.databinding.FragmentRoomBillBinding;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;

public class DetailBillFragment extends Fragment {

    private FragmentDetailBillBinding binding;
    private Home home;
    private String roomID;
    private RoomBill bill;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDetailBillBinding.inflate(inflater, container, false);

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            bill = (RoomBill) arguments.getSerializable("bill");
            if (bill != null) {
                roomID = bill.getRoomID();
                showToast(bill.getRoomName());
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        binding.btnCancelViewBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại Fragment trước đó trong back stack
                getParentFragmentManager().popBackStack();
            }
        });

        return binding.getRoot();
    }

    public void showToast(String message) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
    }
}