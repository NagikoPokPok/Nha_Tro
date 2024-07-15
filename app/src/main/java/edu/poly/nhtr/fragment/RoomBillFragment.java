package edu.poly.nhtr.fragment;

import static android.app.ProgressDialog.show;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import edu.poly.nhtr.Adapter.RoomBillAdapter;
import edu.poly.nhtr.R;
import edu.poly.nhtr.databinding.FragmentNotificationBinding;
import edu.poly.nhtr.databinding.FragmentRoomBillBinding;
import edu.poly.nhtr.listeners.RoomBillListener;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.presenters.RoomBillPresenter;

public class RoomBillFragment extends Fragment implements RoomBillListener {

    FragmentRoomBillBinding binding;
    RoomBillPresenter roomBillPresenter;
    RoomBillAdapter roomBillAdapter;
    Dialog dialog;
    String roomID;

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
        View view = binding.getRoot();

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            Room room = (Room) arguments.getSerializable("room");
            if (room != null) {
                roomID = room.getRoomId();
                showToast(roomID);
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }

        return view;
    }



    private void showToast(String message)
    {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}