package edu.poly.nhtr.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import edu.poly.nhtr.R;
import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomMakeBillFragment;
import edu.poly.nhtr.models.Home;
import edu.poly.nhtr.models.Room;
import edu.poly.nhtr.models.RoomBill;

public class RoomBillContainerFragment extends Fragment implements RoomBillFragment.OnMakeBillClickListener {

    private RoomBillFragment roomBillFragment;
    private Room room;
    private Home home;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_bill_container, container, false);

        // Retrieve the room object from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            room = (Room) arguments.getSerializable("room");
            home = (Home) arguments.getSerializable("home");
            if (room != null && home!=null) {
                showRoomBillFragment();
            } else {
                showToast("Room object is null");
            }
        } else {
            showToast("Arguments are null");
        }


        return view;
    }

    private void showRoomBillFragment() {
        roomBillFragment = new RoomBillFragment();
        roomBillFragment.setOnMakeBillClickListener(this); // Register listener

        // Create and set bundle
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        bundle.putSerializable("home", home);
        roomBillFragment.setArguments(bundle);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.frame_container_frame, roomBillFragment)
                .commit();
    }


    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMakeBillClicked(RoomBill bill) {
        // Create RoomMakeBillFragment and pass data
        RoomMakeBillFragment roomMakeBillFragment = new RoomMakeBillFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("bill", bill);
        roomMakeBillFragment.setArguments(bundle);

        // Replace RoomBillFragment with RoomMakeBillFragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.frame_container_frame, roomMakeBillFragment)
                .addToBackStack(null)
                .commit();
    }
}

